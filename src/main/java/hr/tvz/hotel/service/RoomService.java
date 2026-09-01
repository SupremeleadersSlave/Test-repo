package hr.tvz.hotel.service;

import hr.tvz.hotel.db.RoomDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.persistence.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja sobama.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class RoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

    private final RoomDao roomDao;
    private final ChangeLogManager changeLogManager;
    private final EntityCollection<Room> rooms = new EntityCollection<>();
    private ReservationService reservationService;

    /**
     * Kreira novu instancu servisa za upravljanje sobama i učitava
     * postojeće sobe iz baze.
     *
     * @param roomDao          DAO za pristup sobama u bazi
     * @param changeLogManager upravitelj poviješću promjena
     */
    public RoomService(RoomDao roomDao, ChangeLogManager changeLogManager) {
        this.roomDao = roomDao;
        this.changeLogManager = changeLogManager;
        refresh();
    }

    /**
     * Ponovno učitava sobe iz baze podataka u memoriju.
     */
    public final void refresh() {
        rooms.clear();
        roomDao.findAll().forEach(rooms::add);
    }

    /**
     * Postavlja uslugu za rezervacije, potrebnu za kaskadno brisanje
     * rezervacija povezanih sa sobom.
     *
     * @param reservationService usluga za upravljanje rezervacijama
     */
    public void setReservationService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Vraća broj rezervacija povezanih sa zadanom sobom.
     *
     * @param room soba za koju se broje rezervacije
     * @return broj povezanih rezervacija
     */
    public int countReservations(Room room) {
        return reservationService == null ? 0 : reservationService.findByRoom(room).size();
    }

    /**
     * Vraća sve trenutno učitane sobe.
     *
     * @return popis svih soba
     */
    public List<Room> findAll() {
        return rooms.getAll();
    }

    /**
     * Pretražuje sobe prema uvjetu.
     *
     * @param predicate uvjet pretrage
     * @return popis soba koje zadovoljavaju uvjet
     */
    public List<Room> search(Predicate<Room> predicate) {
        return rooms.filter(predicate);
    }

    /**
     * Vraća sobe sortirane prema komparatoru.
     *
     * @param comparator redoslijed sortiranja
     * @return sortirani popis soba
     */
    public List<Room> sortedBy(Comparator<Room> comparator) {
        return rooms.sorted(comparator);
    }

    /**
     * Dodaje novu sobu, bilježi promjenu u log.
     *
     * @param room      nova soba
     * @param changedBy rola korisnika koji izvršava promjene
     */
    public void addRoom(Room room, Role changedBy) {
        Long id = roomDao.insert(room);
        room.setId(id);
        rooms.add(room);
        logChange(id, "sve", null, room.toString(), changedBy);
        LOGGER.info("Dodana nova soba: {}", room);
    }

    /**
     * Ažurira postojeću sobu, bilježi promjenu u log.
     *
     * @param oldRoom   soba prije izmjene
     * @param newRoom   soba nakon izmjene
     * @param changedBy korisnika koji izvršava promjene
     */
    public void updateRoom(Room oldRoom, Room newRoom, Role changedBy) {
        roomDao.update(newRoom);
        rooms.remove(oldRoom);
        rooms.add(newRoom);
        logChange(newRoom.getId(), "sve", oldRoom.toString(), newRoom.toString(), changedBy);
        LOGGER.info("Ažurirana soba: {}", newRoom);
    }

    /**
     * Briše sobu, bilježi promjenu u log.
     *
     * @param room      soba za brisanje
     * @param changedBy korisnika koji izvršava promjene
     */
    public void deleteRoom(Room room, Role changedBy) {
        if (reservationService != null) {
            reservationService.findByRoom(room).forEach(r -> reservationService.deleteReservation(r, changedBy));
        }
        roomDao.delete(room.getId());
        rooms.remove(room);
        logChange(room.getId(), "sve", room.toString(), null, changedBy);
        LOGGER.info("Obrisana soba: {}", room);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLogManager.append(new ChangeRecord("Room", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
