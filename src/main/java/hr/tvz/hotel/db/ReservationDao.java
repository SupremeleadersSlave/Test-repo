package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.ReservationStatus;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO klasa za entitet {@link Reservation}.
 * Pri mapiranju koristi {@link GuestDao} i {@link RoomDao} za dohvat gosta i sobe.
 *
 * @version 1.0
 */
public class ReservationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationDao.class);
    private final Database database;
    private final GuestDao guestDao;
    private final RoomDao roomDao;

    /**
     * Kreira DAO za rezervacije.
     *
     * @param database konekcija prema bazi
     * @param guestDao DAO za dohvat gostiju
     * @param roomDao DAO za dohvat soba
     */
    public ReservationDao(Database database, GuestDao guestDao, RoomDao roomDao) {
        this.database = database;
        this.guestDao = guestDao;
        this.roomDao = roomDao;
    }

    /**
     * Dohvaća sve rezervacije.
     *
     * @return sve rezervacije
     */
    public List<Reservation> findAll() {
        try {
            return database.executeQuery("SELECT * FROM reservations ORDER BY check_in", this::mapRow);
        } catch (SQLException e) {
            LOGGER.error("rezervacije nisu dohvacene", e);
            return List.of();
        }
    }

    /**
     * Dohvaća rezervaciju prema id-u.
     *
     * @param id id rezervacije
     * @return pronađena rezervacija
     * @throws EntityNotFoundException ako rezervacija ne postoji
     */
    public Reservation findById(Long id) {
        try {
            List<Reservation> results = database.executeQuery("SELECT * FROM reservations WHERE id = ?", this::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("rezervacija {} ne postoji", id);
                throw new EntityNotFoundException("Rezervacija s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("rezervacija {} nije dohvacena", id, e);
            throw new EntityNotFoundException("Rezervacija s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Sprema novu rezervaciju.
     *
     * @param reservation rezervacija za spremanje
     * @return generirani id rezervacije
     */
    public Long insert(Reservation reservation) {
        try {
            return database.executeInsert(
                    "INSERT INTO reservations (guest_id, room_id, check_in, check_out, status, total_price) VALUES (?, ?, ?, ?, ?, ?)",
                    reservation.getGuest().getId(), reservation.getRoom().getId(), reservation.getCheckInDate(),
                    reservation.getCheckOutDate(), reservation.getStatus().name(), reservation.getTotalPrice());
        } catch (SQLException e) {
            LOGGER.error("rezervacija: spremanje palo", e);
            throw new IllegalStateException("Rezervacija se ne sprema.", e);
        }
    }

    /**
     * Updatea status postojeće rezervacije.
     *
     * @param reservation rezervacija s novim statusom
     */
    public void updateStatus(Reservation reservation) {
        try {
            database.executeUpdate("UPDATE reservations SET status = ? WHERE id = ?",
                    reservation.getStatus().name(), reservation.getId());
        } catch (SQLException e) {
            LOGGER.error("rezervacija {} update pao", reservation.getId(), e);
            throw new IllegalStateException("Rezervacija se ne ažurira.", e);
        }
    }

    /**
     * Briše rezervaciju prema id-u.
     *
     * @param id id rezervacije
     */
    public void delete(Long id) {
        try {
            database.executeUpdate("DELETE FROM reservations WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("rezervacija {} se ne brise", id, e);
            throw new IllegalStateException("Rezervacija se ne briše.", e);
        }
    }

    /**
     * Mapira redak rezultata u {@link Reservation}, dohvatom gosta i sobe putem DAO-ova.
     */
    private Reservation mapRow(ResultSet rs) throws SQLException {
        Guest guest = guestDao.findById(rs.getLong("guest_id"));
        Room room = roomDao.findById(rs.getLong("room_id"));
        return new Reservation.Builder()
                .id(rs.getLong("id"))
                .guest(guest)
                .room(room)
                .checkInDate(rs.getDate("check_in").toLocalDate())
                .checkOutDate(rs.getDate("check_out").toLocalDate())
                .status(ReservationStatus.valueOf(rs.getString("status")))
                .totalPrice(rs.getBigDecimal("total_price"))
                .build();
    }
}
