package hr.tvz.hotel.service;

import hr.tvz.hotel.db.ReservationDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.ReservationStatus;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.Schedulable;
import hr.tvz.hotel.exceptions.ReservationNotAvailableException;
import hr.tvz.hotel.persistence.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja rezervacijama, uključujući
 * provjeru raspoloživosti sobe za zadano razdoblje pomoću sučelja
 * {@link Schedulable}.
 */
public class ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDao reservationDao;
    private final ChangeLogManager changeLogManager;
    private final EntityCollection<Reservation> reservations = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje rezervacijama i
     * učitava postojeće rezervacije iz baze podataka.
     *
     * @param reservationDao   DAO za pristup rezervacijama u bazi podataka
     * @param changeLogManager upravitelj poviješću promjena
     */
    public ReservationService(ReservationDao reservationDao, ChangeLogManager changeLogManager) {
        this.reservationDao = reservationDao;
        this.changeLogManager = changeLogManager;
        refresh();
    }

    /**
     * Ponovno učitava rezervacije iz baze podataka u memorijsku kolekciju.
     */
    public final void refresh() {
        reservations.clear();
        reservationDao.findAll().forEach(reservations::add);
    }

    /**
     * Vraća sve trenutno učitane rezervacije.
     *
     * @return popis svih rezervacija
     */
    public List<Reservation> findAll() {
        return reservations.getAll();
    }

    /**
     * Pretražuje rezervacije prema zadanom uvjetu.
     *
     * @param predicate uvjet pretrage, lambda izraz
     * @return popis rezervacija koje zadovoljavaju uvjet
     */
    public List<Reservation> search(Predicate<Reservation> predicate) {
        return reservations.filter(predicate);
    }

    /**
     * Vraća rezervacije sortirane prema zadanom komparatoru.
     *
     * @param comparator redoslijed sortiranja, lambda izraz
     * @return sortirani popis rezervacija
     */
    public List<Reservation> sortedBy(Comparator<Reservation> comparator) {
        return reservations.sorted(comparator);
    }

    /**
     * Kreira novu rezervaciju nakon provjere raspoloživosti sobe za
     * zadano razdoblje.
     *
     * @param guest     gost rezervacije
     * @param room      soba koja se rezervira
     * @param checkIn   datum dolaska
     * @param checkOut  datum odlaska
     * @param changedBy rola korisnika, kreator rezervacije
     * @return kreirana rezervacija
     * @throws ReservationNotAvailableException: soba je već rezervirana za dio zadanog razdoblja
     */
    public Reservation createReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, Role changedBy)
            throws ReservationNotAvailableException {
        Schedulable requestedPeriod = new Schedulable() {
            @Override
            public LocalDate getStartDate() {
                return checkIn;
            }

            @Override
            public LocalDate getEndDate() {
                return checkOut;
            }
        };
        boolean overlapping = reservations.filter(r -> r.getRoom().equals(room) && r.getStatus() != ReservationStatus.CANCELLED)
                .stream()
                .anyMatch(r -> r.overlaps(requestedPeriod));
        if (overlapping) {
            LOGGER.warn("Soba {} nedostupna za {} - {}.", room.getRoomNumber(), checkIn, checkOut);
            throw new ReservationNotAvailableException(
                    "Soba " + room.getRoomNumber() + " nije raspoloživa za razdoblje " + checkIn + " - " + checkOut + ".");
        }
        Reservation reservation = new Reservation.Builder()
                .guest(guest).room(room).checkInDate(checkIn).checkOutDate(checkOut).build();
        Long id = reservationDao.insert(reservation);
        Reservation saved = new Reservation.Builder()
                .id(id).guest(guest).room(room).checkInDate(checkIn).checkOutDate(checkOut)
                .status(reservation.getStatus()).totalPrice(reservation.getTotalPrice()).build();
        reservations.add(saved);
        logChange(id, "sve", null, saved.toString(), changedBy);
        LOGGER.info("Kreirana nova rezervacija: {}", saved);
        return saved;
    }

    /**
     * Mijenja status postojeće rezervacije, bilježi promjenu u
     * povijest promjena.
     *
     * @param reservation rezervacija za promjenu statusa
     * @param newStatus   novi status rezervacije
     * @param changedBy   rola korisnika, izvršitelj promjene
     */
    public void changeStatus(Reservation reservation, ReservationStatus newStatus, Role changedBy) {
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(newStatus);
        reservationDao.updateStatus(reservation);
        logChange(reservation.getId(), "status", oldStatus.name(), newStatus.name(), changedBy);
        LOGGER.info("Status rezervacije {}: {} -> {}", reservation.getId(), oldStatus, newStatus);
    }

    /**
     * Briše rezervaciju, bilježi promjenu u povijest promjena.
     *
     * @param reservation rezervacija za brisanje
     * @param changedBy   rola korisnika, izvršitelj promjene
     */
    public void deleteReservation(Reservation reservation, Role changedBy) {
        reservationDao.delete(reservation.getId());
        reservations.remove(reservation);
        logChange(reservation.getId(), "sve", reservation.toString(), null, changedBy);
        LOGGER.info("Obrisana rezervacija: {}", reservation);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLogManager.append(new ChangeRecord("Reservation", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
