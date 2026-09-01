package hr.tvz.hotel.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Hotelska soba.
 */
public class Room implements Reservable {

    private Long id;
    private String roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private int capacity;
    private boolean active;

    /**
     * Kreira novu sobu.
     *
     * @param id            identifikator sobe
     * @param roomNumber    oznaka sobe
     * @param type          vrsta sobe
     * @param pricePerNight cijena po noćenju
     * @param capacity      najveći broj gostiju u sobi
     * @param active        označava je li soba u ponudi
     */
    public Room(Long id, String roomNumber, RoomType type, BigDecimal pricePerNight, int capacity, boolean active) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.active = active;
    }

    /**
     * Provjerava je li soba aktivna i razdoblje valjano.
     */
    @Override
    public boolean isAvailableFor(LocalDate checkIn, LocalDate checkOut) {
        return active && checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
    }

    @Override
    public BigDecimal calculatePrice(LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    /**
     * Vraća id sobe.
     *
     * @return id sobe
     */
    public Long getId() {
        return id;
    }

    /**
     * Postavlja id sobe.
     *
     * @param id novi id sobe*
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Vraća broj sobe.
     *
     * @return broj sobe
     */
    public String getRoomNumber() {
        return roomNumber;
    }

    /**
     * Postavlja broj sobe.
     *
     * @param roomNumber novi broj sobe
     */
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    /**
     * Vraća vrstu sobe.
     *
     * @return vrsta sobe
     */
    public RoomType getType() {
        return type;
    }

    /**
     * Postavlja vrstu sobe.
     *
     * @param type nova vrsta sobe
     */
    public void setType(RoomType type) {
        this.type = type;
    }

    /**
     * Vraća cijenu po noćenju.
     *
     * @return cijena po noćenju
     */
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    /**
     * Postavlja cijenu po noćenju.
     *
     * @param pricePerNight nova cijena po noćenju
     */
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    /**
     * Vraća kapacitet sobe.
     *
     * @return kapacitet sobe
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Postavlja kapacitet sobe.
     *
     * @param capacity novi kapacitet sobe
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Provjerava je li soba trenutno u ponudi.
     *
     * @return {@code true}: soba je aktivna
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Postavlja je li soba trenutno u ponudi.
     *
     * @param active nova vrijednost aktivnosti sobe
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room room)) {
            return false;
        }
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", broj='" + roomNumber + "', tip=" + type + "}";
    }
}
