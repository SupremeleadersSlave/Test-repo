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
 * <p>
 * Pri mapiranju retka koristi {@link GuestDao} i {@link RoomDao} za
 * dohvaćanje gosta i sobe.
 */
public class ReservationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationDao.class);
    private final DatabaseConnection databaseConnection;
    private final GuestDao guestDao;
    private final RoomDao roomDao;

    /**
     * Kreira DAO za rezervacije.
     *
     * @param databaseConnection konekcija prema bazi podataka
     * @param guestDao           DAO za dohvaćanje gostiju
     * @param roomDao            DAO za dohvaćanje soba
     */
    public ReservationDao(DatabaseConnection databaseConnection, GuestDao guestDao, RoomDao roomDao) {
        this.databaseConnection = databaseConnection;
        this.guestDao = guestDao;
        this.roomDao = roomDao;
    }

    /**
     * Dohvaća sve rezervacije.
     *
     * @return popis svih rezervacija
     */
    public List<Reservation> findAll() {
        try {
            return databaseConnection.executeQuery("SELECT * FROM reservations ORDER BY check_in", this::mapRow);
        } catch (SQLException e) {
            LOGGER.error("Dohvat rezervacija neuspio.", e);
            return List.of();
        }
    }

    /**
     * Dohvaća rezervaciju prema identifikatoru.
     *
     * @param id identifikator rezervacije
     * @return pronađena rezervacija
     * @throws EntityNotFoundException: rezervacija ne postoji
     */
    public Reservation findById(Long id) {
        try {
            List<Reservation> results = databaseConnection.executeQuery("SELECT * FROM reservations WHERE id = ?", this::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("Rezervacija {} ne postoji.", id);
                throw new EntityNotFoundException("Rezervacija s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("Dohvat rezervacije {} neuspio.", id, e);
            throw new EntityNotFoundException("Rezervacija s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Sprema novu rezervaciju.
     *
     * @param reservation rezervacija za spremanje
     * @return identifikator novokreirane rezervacije
     */
    public Long insert(Reservation reservation) {
        try {
            return databaseConnection.executeInsert(
                    "INSERT INTO reservations (guest_id, room_id, check_in, check_out, status, total_price) VALUES (?, ?, ?, ?, ?, ?)",
                    reservation.getGuest().getId(), reservation.getRoom().getId(), reservation.getCheckInDate(),
                    reservation.getCheckOutDate(), reservation.getStatus().name(), reservation.getTotalPrice());
        } catch (SQLException e) {
            LOGGER.error("Spremanje rezervacije neuspjelo.", e);
            throw new IllegalStateException("Rezervacija se ne sprema.", e);
        }
    }

    /**
     * Ažurira status postojeće rezervacije.
     *
     * @param reservation rezervacija s ažuriranim statusom
     */
    public void updateStatus(Reservation reservation) {
        try {
            databaseConnection.executeUpdate("UPDATE reservations SET status = ? WHERE id = ?",
                    reservation.getStatus().name(), reservation.getId());
        } catch (SQLException e) {
            LOGGER.error("Ažuriranje rezervacije {} neuspjelo.", reservation.getId(), e);
            throw new IllegalStateException("Rezervacija se ne ažurira.", e);
        }
    }

    /**
     * Briše rezervaciju prema identifikatoru.
     *
     * @param id identifikator rezervacije
     */
    public void delete(Long id) {
        try {
            databaseConnection.executeUpdate("DELETE FROM reservations WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("Brisanje rezervacije {} neuspjelo.", id, e);
            throw new IllegalStateException("Rezervacija se ne briše.", e);
        }
    }

    /**
     * Mapira redak rezultata u {@link Reservation}, dohvaćajući gosta i
     * sobu putem njihovih DAO-ova.
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
