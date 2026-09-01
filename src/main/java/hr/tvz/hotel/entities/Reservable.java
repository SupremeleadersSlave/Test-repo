package hr.tvz.hotel.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sučelje za entitete koji se mogu rezervirati.
 *
 * @version 1.0
 */
public interface Reservable {

    /**
     * Provjerava je li entitet raspoloživ za zadano razdoblje.
     *
     * @param checkIn datum dolaska
     * @param checkOut datum odlaska
     * @return {@code true} ako je entitet dostupan
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
