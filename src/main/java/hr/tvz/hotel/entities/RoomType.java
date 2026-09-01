package hr.tvz.hotel.entities;

/**
 * Vrsta hotelske sobe. Svaka vrsta vezana je uz kat na kojem se nalaze
 * sobe te vrste, pa kat jednoznačno određuje vrstu sobe.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public enum RoomType {

    /** Jednokrevetna soba, prvi kat. */
    SINGLE(1),

    /** Dvokrevetna soba s bračnim krevetom, drugi kat. */
    DOUBLE(2),

    /** Dvokrevetna soba s odvojenim krevetima, treći kat. */
    TWIN(3),

    /** Apartman, četvrti kat. */
    SUITE(4),

    /** Luksuzna soba, peti kat. */
    DELUXE(5);

    private final int floor;

    RoomType(int floor) {
        this.floor = floor;
    }

    /**
     * Vraća kat na kojem se nalaze sobe ove vrste.
     *
     * @return broj kata
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Vraća vrstu sobe koja se nalazi na zadanom katu.
     *
     * @param floor broj kata
     * @return vrsta sobe na tom katu
     * @throws IllegalArgumentException ako nijedna vrsta ne odgovara katu
     */
    public static RoomType fromFloor(int floor) {
        for (RoomType type : values()) {
            if (type.floor == floor) {
                return type;
            }
        }
        throw new IllegalArgumentException("Nepoznat kat: " + floor);
    }
}
