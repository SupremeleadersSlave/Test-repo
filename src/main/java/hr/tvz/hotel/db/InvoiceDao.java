package hr.tvz.hotel.db;

import hr.tvz.hotel.entities.CardPayment;
import hr.tvz.hotel.entities.CashPayment;
import hr.tvz.hotel.entities.Invoice;
import hr.tvz.hotel.entities.PaymentMethod;
import hr.tvz.hotel.entities.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * DAO klasa za entitet {@link Invoice}.
 * Koristi {@link ReservationDao} za rezervacije.
 * Mapira {@code payment_type} u {@link PaymentMethod}.
 *
 * @version 1.0
 */
public class InvoiceDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceDao.class);
    private final Database database;
    private final ReservationDao reservationDao;

    /**
     * Kreira DAO za račune.
     *
     * @param database konekcija prema bazi
     * @param reservationDao DAO za dohvat rezervacija
     */
    public InvoiceDao(Database database, ReservationDao reservationDao) {
        this.database = database;
        this.reservationDao = reservationDao;
    }

    /**
     * Dohvaća sve račune.
     *
     * @return računi od najnovijeg prema najstarijem
     */
    public List<Invoice> findAll() {
        try {
            return database.executeQuery("SELECT * FROM invoices ORDER BY issue_date DESC", this::mapRow);
        } catch (SQLException e) {
            LOGGER.error("racuni nisu dohvaceni", e);
            return List.of();
        }
    }

    /**
     * Sprema novi račun.
     *
     * @param invoice račun za spremanje
     * @return generirani id računa
     */
    public Long insert(Invoice invoice) {
        PaymentMethod method = invoice.getPaymentMethod();
        String type = method instanceof CashPayment ? "CASH" : "CARD";
        String cashReceived = method instanceof CashPayment(var amountReceived) ? amountReceived.toString() : null;
        String cardNumber = method instanceof CardPayment(var maskedCardNumber, var authorizationCode)
                ? maskedCardNumber : null;
        String authCode = method instanceof CardPayment(var maskedCardNumber, var authorizationCode)
                ? authorizationCode : null;
        try {
            return database.executeInsert(
                    "INSERT INTO invoices (reservation_id, amount, payment_type, cash_amount_received, "
                            + "card_masked_number, card_authorization_code, issue_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    invoice.getReservation().getId(), invoice.getAmount(), type, cashReceived, cardNumber, authCode,
                    Timestamp.valueOf(invoice.getIssueDate()));
        } catch (SQLException e) {
            LOGGER.error("racun: spremanje palo", e);
            throw new IllegalStateException("Račun se ne sprema.", e);
        }
    }

    /**
     * Briše račun prema id-u.
     *
     * @param id id računa
     */
    public void delete(Long id) {
        try {
            database.executeUpdate("DELETE FROM invoices WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("racun {} se ne brise", id, e);
            throw new IllegalStateException("Račun se ne briše.", e);
        }
    }

    /**
     * Mapira redak rezultata u {@link Invoice}.
     * Mapira {@code payment_type} u {@link PaymentMethod}.
     */
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Reservation reservation = reservationDao.findById(rs.getLong("reservation_id"));
        PaymentMethod method;
        if ("CASH".equals(rs.getString("payment_type"))) {
            method = new CashPayment(rs.getBigDecimal("cash_amount_received"));
        } else {
            method = new CardPayment(rs.getString("card_masked_number"), rs.getString("card_authorization_code"));
        }
        return new Invoice(rs.getLong("id"), reservation, rs.getBigDecimal("amount"), method,
                rs.getTimestamp("issue_date").toLocalDateTime());
    }
}
