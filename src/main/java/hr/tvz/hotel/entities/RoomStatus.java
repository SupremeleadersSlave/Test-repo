package hr.tvz.hotel.entities;

/**
 * Status raspoloživosti hotelske sobe. Određuje je li soba u ponudi za
 * rezervacije; trenutna zauzetost izvodi se iz rezervacija, a ne iz
 * ovog statusa.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public enum RoomStatus {

    /** Soba je u ponudi i može se rezervirati. */
    AVAILABLE("dostupna"),

    /** Soba je na održavanju i nije u ponudi. */
    MAINTENANCE("na održavanju"),

    /** Soba je u obnovi i nije u ponudi. */
    RENOVATION("u obnovi");

    private final String label;

    RoomStatus(String label) {
        this.label = label;
    }

    /**
     * Vraća opisnu oznaku statusa na hrvatskom jeziku.
     *
     * @return opis statusa
     */
    public String getLabel() {
        return label;
    }

    /**
     * Provjerava može li se soba u ovom statusu rezervirati.
     *
     * @return {@code true} ako je soba u ponudi
     */
    public boolean isBookable() {
        return this == AVAILABLE;
    }
}
