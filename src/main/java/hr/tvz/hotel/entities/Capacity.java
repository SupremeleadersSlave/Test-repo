package hr.tvz.hotel.entities;

/**
 * Kapacitet hotelske sobe izražen najvećim brojem osoba.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public enum Capacity {

    /** Jedna osoba. */
    SINGLE(1),

    /** Dvije osobe. */
    DOUBLE(2),

    /** Tri osobe. */
    TRIPLE(3),

    /** Četiri osobe. */
    QUAD(4);

    private final int persons;

    Capacity(int persons) {
        this.persons = persons;
    }

    /**
     * Vraća najveći broj osoba za ovaj kapacitet.
     *
     * @return broj osoba
     */
    public int getPersons() {
        return persons;
    }
}
