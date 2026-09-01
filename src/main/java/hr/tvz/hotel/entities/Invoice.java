package hr.tvz.hotel.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Račun izdan za rezervaciju.
 */
public class Invoice {

    private Long id;
    private Reservation reservation;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime issueDate;

    /**
     * Kreira novi račun.
     *
     * @param id id računa
     * @param reservation rezervacija za koju se izdaje račun
     * @param amount iznos računa
     * @param paymentMethod način plaćanja
     * @param issueDate datum i vrijeme izdavanja računa
     */
    public Invoice(Long id, Reservation reservation, BigDecimal amount, PaymentMethod paymentMethod, LocalDateTime issueDate) {
        this.id = id;
        this.reservation = reservation;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.issueDate = issueDate;
    }

    /**
     * Vraća id računa.
     *
     * @return id računa
     */
    public Long getId() {
        return id;
    }

    /**
     * Postavlja id računa.
     *
     * @param id novi id računa
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Vraća rezervaciju za koju je račun izdan.
     *
     * @return rezervacija računa
     */
    public Reservation getReservation() {
        return reservation;
    }

    /**
     * Postavlja rezervaciju za koju je račun izdan.
     *
     * @param reservation nova rezervacija računa
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * Vraća iznos računa.
     *
     * @return iznos računa
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Postavlja iznos računa.
     *
     * @param amount novi iznos računa
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Vraća način plaćanja računa.
     *
     * @return način plaćanja
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Postavlja način plaćanja računa.
     *
     * @param paymentMethod novi način plaćanja
     */
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Vraća datum i vrijeme izdavanja računa.
     *
     * @return datum i vrijeme izdavanja
     */
    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    /**
     * Postavlja datum i vrijeme izdavanja računa.
     *
     * @param issueDate novi datum i vrijeme izdavanja
     */
    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Invoice invoice)) {
            return false;
        }
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Invoice{id=" + id + ", iznos=" + amount + ", nacinPlacanja=" + paymentMethod.describe() + "}";
    }
}
