package hr.tvz.hotel.entities;

import hr.tvz.hotel.exceptions.InvalidRoomException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Hotelska soba.
 *
 * @version 1.0
 */
public class Room implements Reservable {

    private Long id;
    private String roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private Capacity capacity;
    private RoomStatus status;

    /**
     * Kreira novu sobu.
     *
     * @param id            identifikator sobe
     * @param roomNumber    oznaka sobe
     * @param type          vrsta sobe
     * @param pricePerNight cijena po noćenju
     * @param capacity      kapacitet sobe
     * @param status        status raspoloživosti sobe
     */
    public Room(Long id, String roomNumber, RoomType type, BigDecimal pricePerNight, Capacity capacity, RoomStatus status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.status = status;
    }

    /**
     * Provjerava je li soba aktivna i razdoblje valjano.
     */
    @Override
    public boolean isAvailableFor(LocalDate checkIn, LocalDate checkOut) {
        return status.isBookable() && checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
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
     * @param id novi id sobe
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
    public Capacity getCapacity() {
        return capacity;
    }

    /**
     * Postavlja kapacitet sobe.
     *
     * @param capacity novi kapacitet sobe
     */
    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    /**
     * Provjerava je li broj sobe valjan: mora imati oblik kat + redni
     * broj, gdje je kat znamenka od 1 do 5, a redni broj dvoznamenkasti
     * od 01 do 20 (npr. 101 - 120, 201 - 220, ... 501 - 520).
     *
     * @param roomNumber broj sobe za provjeru
     * @throws InvalidRoomException ako broj sobe nije u ispravnom formatu ili rasponu
     */
    public static void validateRoomNumber(String roomNumber) throws InvalidRoomException {
        if (roomNumber == null || !roomNumber.matches("[1-5]\\d{2}")) {
            throw new InvalidRoomException("Broj sobe mora biti oblika kat (1-5) + soba (01-20): " + roomNumber);
        }
        int room = Integer.parseInt(roomNumber.substring(1));
        if (room < 1 || room > 20) {
            throw new InvalidRoomException("Redni broj sobe mora biti između 01 i 20: " + roomNumber);
        }
    }

    /**
     * Vraća status raspoloživosti sobe.
     *
     * @return status sobe
     */
    public RoomStatus getStatus() {
        return status;
    }

    /**
     * Postavlja status raspoloživosti sobe.
     *
     * @param status novi status sobe
     */
    public void setStatus(RoomStatus status) {
        this.status = status;
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
