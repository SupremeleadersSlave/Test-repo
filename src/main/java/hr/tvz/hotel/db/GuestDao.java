package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.Address;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO klasa za entitet {@link Guest}.
 */
public class GuestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestDao.class);
    private final DatabaseConnection databaseConnection;

    /**
     * Kreira DAO za goste.
     *
     * @param databaseConnection konekcija prema bazi podataka
     */
    public GuestDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Dohvaća sve goste.
     *
     * @return popis svih gostiju
     */
    public List<Guest> findAll() {
        try {
            return databaseConnection.executeQuery("SELECT * FROM guests ORDER BY last_name", GuestDao::mapRow);
        } catch (SQLException e) {
            LOGGER.error("Dohvat gostiju neuspio.", e);
            return List.of();
        }
    }

    /**
     * Dohvaća gosta prema identifikatoru.
     *
     * @param id identifikator gosta
     * @return pronađeni gost
     * @throws EntityNotFoundException: gost ne postoji
     */
    public Guest findById(Long id) {
        try {
            List<Guest> results = databaseConnection.executeQuery("SELECT * FROM guests WHERE id = ?", GuestDao::mapRow, id);
            if (results.isEmpty()) {
                LOGGER.warn("Gost {} ne postoji.", id);
                throw new EntityNotFoundException("Gost s identifikatorom " + id + " ne postoji.");
            }
            return results.get(0);
        } catch (SQLException e) {
            LOGGER.error("Dohvat gosta {} neuspio.", id, e);
            throw new EntityNotFoundException("Gost s identifikatorom " + id + " ne postoji.", e);
        }
    }

    /**
     * Sprema novog gosta.
     *
     * @param guest gost za spremanje
     * @return identifikator novokreiranog gosta
     */
    public Long insert(Guest guest) {
        try {
            Address address = guest.getAddress();
            return databaseConnection.executeInsert(
                    "INSERT INTO guests (first_name, last_name, email, phone, document_number, street, city, postal_code, country) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    guest.getFirstName(), guest.getLastName(), guest.getEmail(), guest.getPhone(), guest.getDocumentNumber(),
                    address.street(), address.city(), address.postalCode(), address.country());
        } catch (SQLException e) {
            LOGGER.error("Spremanje gosta neuspjelo.", e);
            throw new IllegalStateException("Gost se ne sprema.", e);
        }
    }

    /**
     * Ažurira postojećeg gosta.
     *
     * @param guest gost s ažuriranim podacima
     */
    public void update(Guest guest) {
        try {
            Address address = guest.getAddress();
            databaseConnection.executeUpdate(
                    "UPDATE guests SET first_name = ?, last_name = ?, email = ?, phone = ?, document_number = ?, "
                            + "street = ?, city = ?, postal_code = ?, country = ? WHERE id = ?",
                    guest.getFirstName(), guest.getLastName(), guest.getEmail(), guest.getPhone(), guest.getDocumentNumber(),
                    address.street(), address.city(), address.postalCode(), address.country(), guest.getId());
        } catch (SQLException e) {
            LOGGER.error("Ažuriranje gosta {} neuspjelo.", guest.getId(), e);
            throw new IllegalStateException("Gost se ne ažurira.", e);
        }
    }

    /**
     * Briše gosta prema identifikatoru.
     *
     * @param id identifikator gosta
     */
    public void delete(Long id) {
        try {
            databaseConnection.executeUpdate("DELETE FROM guests WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("Brisanje gosta {} neuspjelo.", id, e);
            throw new IllegalStateException("Gost se ne briše.", e);
        }
    }

    private static Guest mapRow(ResultSet rs) throws SQLException {
        Address address = new Address(rs.getString("street"), rs.getString("city"), rs.getString("postal_code"), rs.getString("country"));
        return new Guest(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("document_number"),
                address
        );
    }
}
