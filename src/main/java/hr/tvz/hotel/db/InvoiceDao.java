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
 * <p>
 * Pri mapiranju koristi {@link ReservationDao} za dohvaćanje rezervacije.
 * Stupac {@code payment_type} određuje implementaciju zapečaćenog sučelja
 * {@link PaymentMethod}: {@link CashPayment} ili {@link CardPayment}.
 */
public class InvoiceDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceDao.class);
    private final DatabaseConnection databaseConnection;
    private final ReservationDao reservationDao;

    /**
     * Kreira DAO za račune.
     *
     * @param databaseConnection konekcija prema bazi podataka
     * @param reservationDao     DAO za dohvaćanje rezervacija
     */
    public InvoiceDao(DatabaseConnection databaseConnection, ReservationDao reservationDao) {
        this.databaseConnection = databaseConnection;
        this.reservationDao = reservationDao;
    }

    /**
     * Dohvaća sve račune.
     *
     * @return popis svih računa, od najnovijeg prema najstarijem
     */
    public List<Invoice> findAll() {
        try {
            return databaseConnection.executeQuery("SELECT * FROM invoices ORDER BY issue_date DESC", this::mapRow);
        } catch (SQLException e) {
            LOGGER.error("Dohvat računa neuspio.", e);
            return List.of();
        }
    }

    /**
     * Sprema novi račun.
     *
     * @param invoice račun za spremanje
     * @return identifikator novokreiranog računa
     */
    public Long insert(Invoice invoice) {
        PaymentMethod method = invoice.getPaymentMethod();
        String type = method instanceof CashPayment ? "CASH" : "CARD";
        String cashReceived = method instanceof CashPayment cash ? cash.amountReceived().toString() : null;
        String cardNumber = method instanceof CardPayment card ? card.maskedCardNumber() : null;
        String authCode = method instanceof CardPayment card ? card.authorizationCode() : null;
        try {
            return databaseConnection.executeInsert(
                    "INSERT INTO invoices (reservation_id, amount, payment_type, cash_amount_received, "
                            + "card_masked_number, card_authorization_code, issue_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    invoice.getReservation().getId(), invoice.getAmount(), type, cashReceived, cardNumber, authCode,
                    Timestamp.valueOf(invoice.getIssueDate()));
        } catch (SQLException e) {
            LOGGER.error("Spremanje računa neuspjelo.", e);
            throw new IllegalStateException("Račun se ne sprema.", e);
        }
    }

    /**
     * Briše račun prema identifikatoru.
     *
     * @param id identifikator računa
     */
    public void delete(Long id) {
        try {
            databaseConnection.executeUpdate("DELETE FROM invoices WHERE id = ?", id);
        } catch (SQLException e) {
            LOGGER.error("Brisanje računa {} neuspjelo.", id, e);
            throw new IllegalStateException("Račun se ne briše.", e);
        }
    }

    /**
     * Mapira redak rezultata u {@link Invoice}, rekonstruirajući
     * odgovarajuću implementaciju sučelja {@link PaymentMethod} prema
     * stupcu {@code payment_type}.
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
