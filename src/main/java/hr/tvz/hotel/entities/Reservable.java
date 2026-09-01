package hr.tvz.hotel.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sučelje za entitete: mogu se rezervirati.
 */
public interface Reservable {

    /**
     * Provjerava je li entitet raspoloživ za zadano razdoblje.
     *
     * @param checkIn  datum dolaska
     * @param checkOut datum odlaska
     * @return {@code true}: entitet je raspoloživ, inače {@code false}
     */
    boolean isAvailableFor(LocalDate checkIn, LocalDate checkOut);

    /**
     * Izračunava cijenu za zadano razdoblje.
     *
     * @param checkIn  datum dolaska
     * @param checkOut datum odlaska
     * @return cijena za razdoblje
     */
    BigDecimal calculatePrice(LocalDate checkIn, LocalDate checkOut);
}
