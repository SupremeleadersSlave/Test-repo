package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO klasa za entitet {@link User}.
 */
public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private final DatabaseConnection databaseConnection;

    /**
     * Kreira DAO za korisnike sustava.
     *
     * @param databaseConnection konekcija prema bazi
     */
    public UserDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Dohvaća sve korisnike sustava.
     *
     * @return svi korisnici
     */
    public List<User> findAll() {
        try {
            return databaseConnection.executeQuery("SELECT * FROM users ORDER BY username", UserDao::mapRow);
        } catch (SQLException e) {
            LOGGER.error("Dohvat korisnika neuspio.", e);
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
            List<User> results = databaseConnection.executeQuery("SELECT * FROM users WHERE id = ?", UserDao::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("Korisnik {} ne postoji.", id);
                throw new EntityNotFoundException("Korisnik s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("Dohvat korisnika {} neuspio.", id, e);
            throw new EntityNotFoundException("Korisnik s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Dohvaća korisnika prema korisničkom imenu.
     *
     * @param username korisničko ime
     * @return korisnik s zadanim korisničkim imenom, ili prazan {@link Optional} ako ne postoji
     */
    public Optional<User> findByUsername(String username) {
        try {
            List<User> results = databaseConnection.executeQuery("SELECT * FROM users WHERE username = ?", UserDao::mapRow, username);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.error("Dohvat korisnika prema korisničkom imenu {} neuspio.", username, e);
            return Optional.empty();
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
            return databaseConnection.executeInsert(
                    "INSERT INTO users (first_name, last_name, email, phone, username, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                    user.getUsername(), user.getPasswordHash(), user.getRole().name());
        } catch (SQLException e) {
            LOGGER.error("Spremanje korisnika neuspjelo.", e);
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
            databaseConnection.executeUpdate(
                    "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, username = ?, password_hash = ?, role = ? WHERE id = ?",
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                    user.getUsername(), user.getPasswordHash(), user.getRole().name(), user.getId());
        } catch (SQLException e) {
            LOGGER.error("Ažuriranje korisnika {} neuspjelo.", user.getId(), e);
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
            databaseConnection.executeUpdate("DELETE FROM users WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("Brisanje korisnika {} neuspjelo.", id, e);
            throw new IllegalStateException("Korisnik se ne briše.", e);
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role"))
        );
    }
}
