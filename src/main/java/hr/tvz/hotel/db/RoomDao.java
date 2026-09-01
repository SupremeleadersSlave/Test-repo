package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.RoomType;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO klasa za {@link Room}.
 *
 * @version 1.0
 */
public class RoomDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomDao.class);
    private final Database database;

    /**
     * Kreira DAO za sobe.
     *
     * @param database konekcija prema bazi
     */
    public RoomDao(Database database) {
        this.database = database;
    }

    /**
     * Dohvaća sve sobe.
     *
     * @return sve sobe
     */
    public List<Room> findAll() {
        try {
            return database.executeQuery("SELECT * FROM rooms ORDER BY room_number", RoomDao::mapRow);
        } catch (SQLException e) {
            LOGGER.error("Dohvat soba neuspio.", e);
            return List.of();
        }
    }

    /**
     * Dohvaća sobu prema id-u.
     *
     * @param id id sobe
     * @return pronađena soba
     * @throws EntityNotFoundException ako soba ne postoji
     */
    public Room findById(Long id) {
        try {
            List<Room> results = database.executeQuery("SELECT * FROM rooms WHERE id = ?", RoomDao::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("Soba {} ne postoji.", id);
                throw new EntityNotFoundException("Soba s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("Dohvat sobe {} neuspio.", id, e);
            throw new EntityNotFoundException("Soba s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Sprema novu sobu.
     *
     * @param room soba za spremanje
     * @return generirani id sobe
     */
    public Long insert(Room room) {
        try {
            return database.executeInsert(
                    "INSERT INTO rooms (room_number, type, price_per_night, capacity, active) VALUES (?, ?, ?, ?, ?)",
                    room.getRoomNumber(), room.getType().name(), room.getPricePerNight(), room.getCapacity(), room.isActive());
        } catch (SQLException e) {
            LOGGER.error("Spremanje sobe neuspjelo.", e);
            throw new IllegalStateException("Soba se ne sprema.", e);
        }
    }

    /**
     * Updatea postojeću sobu.
     *
     * @param room soba s novim podacima
     */
    public void update(Room room) {
        try {
            database.executeUpdate(
                    "UPDATE rooms SET room_number = ?, type = ?, price_per_night = ?, capacity = ?, active = ? WHERE id = ?",
                    room.getRoomNumber(), room.getType().name(), room.getPricePerNight(), room.getCapacity(), room.isActive(), room.getId());
        } catch (SQLException e) {
            LOGGER.error("Ažuriranje sobe {} neuspjelo.", room.getId(), e);
            throw new IllegalStateException("Soba se ne ažurira.", e);
        }
    }

    /**
     * Briše sobu prema id-u.
     *
     * @param id id sobe
     */
    public void delete(Long id) {
        try {
            database.executeUpdate("DELETE FROM rooms WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("Brisanje sobe {} neuspjelo.", id, e);
            throw new IllegalStateException("Soba se ne briše.", e);
        }
    }

    private static Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getLong("id"),
                rs.getString("room_number"),
                RoomType.valueOf(rs.getString("type")),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("capacity"),
                rs.getBoolean("active")
        );
    }
}
