package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO klasa za entitet {@link User}.
 *
 * @version 1.0
 */
public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private final Database database;

    /**
     * Kreira DAO za korisnike sustava.
     *
     * @param database konekcija prema bazi
     */
    public UserDao(Database database) {
        this.database = database;
    }

    /**
     * Dohvaća sve korisnike sustava.
     *
     * @return svi korisnici
     */
    public List<User> findAll() {
        try {
            return database.executeQuery("SELECT * FROM users ORDER BY username", UserDao::mapRow);
        } catch (SQLException e) {
            LOGGER.error("useri nisu dohvaceni", e);
            return List.of();
        }
    }

    /**
     * Dohvaća korisnika prema id-u.
     *
     * @param id id korisnika
     * @return pronađeni korisnik
     * @throws EntityNotFoundException ako korisnik ne postoji
     */
    public User findById(Long id) {
        try {
            List<User> results = database.executeQuery("SELECT * FROM users WHERE id = ?", UserDao::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("user {} ne postoji", id);
                throw new EntityNotFoundException("Korisnik s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("user {} nije dohvacen", id, e);
            throw new EntityNotFoundException("Korisnik s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Sprema novog korisnika sustava.
     *
     * @param user korisnik za spremanje
     * @return generirani id korisnika
     */
    public Long insert(User user) {
        try {
            return database.executeInsert(
                    "INSERT INTO users (first_name, last_name, email, phone, username, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                    user.getUsername(), user.getPasswordHash(), user.getRole().name());
        } catch (SQLException e) {
            LOGGER.error("user: spremanje palo", e);
            throw new IllegalStateException("Korisnik se ne sprema.", e);
        }
    }

    /**
     * Updatea postojećeg korisnika sustava.
     *
     * @param user korisnik s novim podacima
     */
    public void update(User user) {
        try {
            database.executeUpdate(
                    "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, username = ?, password_hash = ?, role = ? WHERE id = ?",
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                    user.getUsername(), user.getPasswordHash(), user.getRole().name(), user.getId());
        } catch (SQLException e) {
            LOGGER.error("user {} update pao", user.getId(), e);
            throw new IllegalStateException("Korisnik se ne ažurira.", e);
        }
    }

    /**
     * Briše korisnika sustava prema id-u.
     *
     * @param id id korisnika
     */
    public void delete(Long id) {
        try {
            database.executeUpdate("DELETE FROM users WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("user {} se ne brise", id, e);
            throw new IllegalStateException("Korisnik se ne briše.", e);
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User.Builder()
                .id(rs.getLong("id"))
                .name(rs.getString("first_name"), rs.getString("last_name"))
                .contact(rs.getString("email"), rs.getString("phone"))
                .credentials(rs.getString("username"), rs.getString("password_hash"))
                .role(Role.valueOf(rs.getString("role")))
                .build();
    }
}
