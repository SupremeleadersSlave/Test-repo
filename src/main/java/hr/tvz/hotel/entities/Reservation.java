package hr.tvz.hotel.entities;

import hr.tvz.hotel.exceptions.InvalidReservationDateException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Rezervacija sobe za gosta u zadanom razdoblju.
 * Instance se stvaraju kroz {@link Builder}, koji validira datume.
 */
public class Reservation implements Schedulable {

    private final Long id;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private ReservationStatus status;
    private final BigDecimal totalPrice;

    private Reservation(Builder builder) {
        this.id = builder.id;
        this.guest = builder.guest;
        this.room = builder.room;
        this.checkInDate = builder.checkInDate;
        this.checkOutDate = builder.checkOutDate;
        this.status = builder.status;
        this.totalPrice = builder.totalPrice;
    }

    @Override
    public LocalDate getStartDate() {
        return checkInDate;
    }

    @Override
    public LocalDate getEndDate() {
        return checkOutDate;
    }

    /**
     * Vraća identifikator rezervacije.
     *
     * @return identifikator rezervacije
     */
    public Long getId() {
        return id;
    }

    /**
     * Vraća gosta na kojeg glasi rezervacija.
     *
     * @return gost rezervacije
     */
    public Guest getGuest() {
        return guest;
    }

    /**
     * Vraća sobu na koju se rezervacija odnosi.
     *
     * @return soba rezervacije
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Vraća datum dolaska.
     *
     * @return datum dolaska
     */
    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    /**
     * Vraća datum odlaska.
     *
     * @return datum odlaska
     */
    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    /**
     * Vraća trenutni status rezervacije.
     *
     * @return status rezervacije
     */
    public ReservationStatus getStatus() {
        return status;
    }

    /**
     * Postavlja status rezervacije.
     *
     * @param status novi status rezervacije
     */
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    /**
     * Vraća ukupnu cijenu rezervacije.
     *
     * @return ukupna cijena
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", soba=" + room + ", " + checkInDate + " - " + checkOutDate + ", status=" + status + "}";
    }

    /**
     * Builder za {@link Reservation}.
     */
    public static final class Builder {

        private Long id;
        private Guest guest;
        private Room room;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private ReservationStatus status = ReservationStatus.PENDING;
        private BigDecimal totalPrice;

        /**
         * Postavlja id rezervacije.
         *
         * @param id id rezervacije
         * @return ovaj Builder
         */
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Postavlja gosta na kojeg rezervacija glasi.
         *
         * @param guest gost rezervacije
         * @return ovaj Builder
         */
        public Builder guest(Guest guest) {
            this.guest = guest;
            return this;
        }

        /**
         * Postavlja sobu na koju se rezervacija odnosi.
         *
         * @param room soba rezervacije
         * @return ovaj Builder
         */
        public Builder room(Room room) {
            this.room = room;
            return this;
        }

        /**
         * Postavlja datum dolaska.
         *
         * @param checkInDate datum dolaska
         * @return ovaj Builder
         */
        public Builder checkInDate(LocalDate checkInDate) {
            this.checkInDate = checkInDate;
            return this;
        }

        /**
         * Postavlja datum odlaska.
         *
         * @param checkOutDate datum odlaska
         * @return ovaj Builder
         */
        public Builder checkOutDate(LocalDate checkOutDate) {
            this.checkOutDate = checkOutDate;
            return this;
        }

        /**
         * Postavlja status rezervacije.
         *
         * @param status status rezervacije
         * @return ovaj Builder
         */
        public Builder status(ReservationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Postavlja ukupnu cijenu rezervacije.
         *
         * @param totalPrice ukupna cijena
         * @return ovaj Builder
         */
        public Builder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        /**
         * Gradi novu rezervaciju i provjerava valjanost datuma.
         *
         * @return izgrađena rezervacija
         * @throws InvalidReservationDateException ako datum odlaska nije nakon datuma dolaska
         */
        public Reservation build() {
            if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
                throw new InvalidReservationDateException(
                        "Datum odlaska (" + checkOutDate + ") nije nakon datuma dolaska (" + checkInDate + ").");
            }
            if (totalPrice == null && room != null) {
                totalPrice = room.calculatePrice(checkInDate, checkOutDate);
            }
            return new Reservation(this);
        }
    }
}
