package hr.tvz.hotel.service;

import hr.tvz.hotel.db.ReservationDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.ReservationStatus;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Relation;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.Schedulable;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import hr.tvz.hotel.exceptions.ReservationNotAvailableException;
import hr.tvz.hotel.files.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementira poslovnu logiku upravljanja rezervacijama, uključujući
 * provjeru raspoloživosti sobe za zadano razdoblje pomoću sučelja
 * {@link Schedulable}.
 *
 * @version 1.0
 */
public class ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDao reservationDao;
    private final ChangeLog changeLog;
    private final EntityCollection<Reservation> reservations = new EntityCollection<>();
    private InvoiceService invoiceService;

    /**
     * Kreira novu instancu servisa za upravljanje rezervacijama i
     * učitava postojeće rezervacije iz baze.
     *
     * @param reservationDao DAO za pristup rezervacijama u bazi
     * @param changeLog upravitelj poviješću promjena
     */
    public ReservationService(ReservationDao reservationDao, ChangeLog changeLog) {
        this.reservationDao = reservationDao;
        this.changeLog = changeLog;
        refresh();
    }

    /**
     * Postavlja uslugu za račune, potrebnu za kaskadno brisanje računa
     * povezanih s rezervacijom.
     *
     * @param invoiceService usluga za upravljanje računima
     */
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Vraća rezervacije povezane sa zadanim gostom.
     *
     * @param guest gost čije se rezervacije traže
     * @return popis rezervacija tog gosta
     */
    public List<Reservation> findByGuest(Guest guest) {
        return reservations.filter(r -> r.getGuest().equals(guest));
    }

    /**
     * Vraća rezervacije povezane sa zadanom sobom.
     *
     * @param room soba čije se rezervacije traže
     * @return popis rezervacija te sobe
     */
    public List<Reservation> findByRoom(Room room) {
        return reservations.filter(r -> r.getRoom().equals(room));
    }

    /**
     * Ponovno učitava rezervacije iz baze podataka u memoriju.
     */
    public final void refresh() {
        reservations.clear();
        try {
            reservationDao.findAll().forEach(reservations::add);
        } catch (EntityNotFoundException e) {
            LOGGER.error("rezervacije: gost ili soba fali", e);
        }
    }

    /**
     * Vraća skup veza između gostiju i soba koje su rezervirali.
     *
     * @return skup veza gost-soba, bez ponavljanja
     */
    public Set<Relation<Guest, Room>> findGuestRoomRelations() {
        return reservations.toSet().stream()
                .map(r -> new Relation<>(r.getGuest(), r.getRoom(), "REZERVIRAO"))
                .collect(Collectors.toSet());
    }

    /**
     * Računa popunjenost po mjesecima za zadanu godinu: broj rezervacija
     * i broj gostiju čije se razdoblje preklapa sa svakim mjesecom.
     * Rezervacija koja se proteže kroz više mjeseci broji se u svakom
     * mjesecu kojeg dodiruje.
     *
     * @param year godina za koju se računa popunjenost
     * @return mapa mjeseca na popunjenost tog mjeseca, za svih 12 mjeseci
     */
    public Map<Month, Occupancy> occupancyByMonth(int year) {
        Map<Month, Occupancy> result = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            YearMonth ym = YearMonth.of(year, month);
            Schedulable monthPeriod = new Schedulable() {
                @Override
                public LocalDate getStartDate() {
                    return ym.atDay(1);
                }

                @Override
                public LocalDate getEndDate() {
                    return ym.atEndOfMonth();
                }
            };
            List<Reservation> inMonth = reservations.filter(r ->
                    r.getStatus() != ReservationStatus.CANCELLED && r.overlaps(monthPeriod));
            long guests = inMonth.stream().map(Reservation::getGuest).distinct().count();
            result.put(month, new Occupancy(inMonth.size(), (int) guests));
        }
        return result;
    }

    /**
     * Popunjenost jednog mjeseca: broj rezervacija i broj gostiju.
     *
     * @param reservations broj rezervacija koje se preklapaju s mjesecom
     * @param guests broj različitih gostiju u tom mjesecu
     */
    public record Occupancy(int reservations, int guests) {
    }

    /**
     * Vraća sve učitane rezervacije.
     *
     * @return popis svih rezervacija
     */
    public List<Reservation> findAll() {
        return reservations.getAll();
    }

    /**
     * Pretražuje rezervacije prema uvjetu.
     *
     * @param predicate uvjet pretrage
     * @return popis rezervacija koje zadovoljavaju uvjet
     */
    public List<Reservation> search(Predicate<Reservation> predicate) {
        return reservations.filter(predicate);
    }

    /**
     * Vraća rezervacije sortirane prema komparatoru.
     *
     * @param comparator redoslijed sortiranja
     * @return sortirani popis rezervacija
     */
    public List<Reservation> sortedBy(Comparator<Reservation> comparator) {
        return reservations.sorted(comparator);
    }

    /**
     * Kreira novu rezervaciju nakon provjere je li soba u ponudi i
     * raspoloživa za zadano razdoblje.
     *
     * @param guest gost rezervacije
     * @param room soba koja se rezervira
     * @param checkIn datum dolaska
     * @param checkOut datum odlaska
     * @param changedBy uloga korisnika koji kreira rezervaciju
     * @return kreirana rezervacija
     * @throws ReservationNotAvailableException ako soba nije u ponudi ili je već rezervirana za dio zadanog razdoblja
     */
    public Reservation createReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, Role changedBy)
            throws ReservationNotAvailableException {
        if (!room.isAvailableFor(checkIn, checkOut)) {
            LOGGER.warn("soba {} nije u ponudi ({}) ili lose datumi: {} - {}",
                    room.getRoomNumber(), room.getStatus(), checkIn, checkOut);
            throw new ReservationNotAvailableException(
                    "Soba " + room.getRoomNumber() + " (" + room.getStatus().getLabel() + ") nije dostupna za razdoblje "
                            + checkIn + " - " + checkOut + ".");
        }
        Schedulable period = new Schedulable() {
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
                .anyMatch(r -> r.overlaps(period));
        if (overlapping) {
            LOGGER.warn("soba {} zauzeta: {} - {}", room.getRoomNumber(), checkIn, checkOut);
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
        LOGGER.info("Nova Rezervacija: {}", saved);
        return saved;
    }

    /**
     * Mijenja status postojeće rezervacije i bilježi promjenu u
     * log.
     *
     * @param reservation rezervacija za promjenu statusa
     * @param newStatus novi status rezervacije
     * @param changedBy uloga korisnika koji izvršava promjenu
     */
    public void changeStatus(Reservation reservation, ReservationStatus newStatus, Role changedBy) {
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(newStatus);
        reservationDao.updateStatus(reservation);
        logChange(reservation.getId(), "status", oldStatus.name(), newStatus.name(), changedBy);
        LOGGER.info("Status {}: {} -> {}", reservation.getId(), oldStatus, newStatus);
    }

    /**
     * Briše rezervaciju i bilježi promjenu u log.
     *
     * @param reservation rezervacija za brisanje
     * @param changedBy uloga korisnika koji izvršava promjenu
     */
    public void deleteReservation(Reservation reservation, Role changedBy) {
        if (invoiceService != null) {
            invoiceService.findByReservation(reservation).forEach(invoice -> invoiceService.deleteInvoice(invoice, changedBy));
        }
        reservationDao.delete(reservation.getId());
        reservations.remove(reservation);
        logChange(reservation.getId(), "sve", reservation.toString(), null, changedBy);
        LOGGER.info("Rezervacija Obrisana: {}", reservation);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLog.append(new ChangeRecord("Reservation", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}