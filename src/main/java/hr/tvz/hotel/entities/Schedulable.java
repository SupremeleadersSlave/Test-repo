package hr.tvz.hotel.entities;

import java.time.LocalDate;

/**
 * Sučelje za entitete s datumom početka i završetka.
 */
public interface Schedulable {

    /**
     * Vraća datum početka.
     *
     * @return datum početka
     */
    LocalDate getStartDate();

    /**
     * Vraća datum završetka.
     *
     * @return datum završetka
     */
    LocalDate getEndDate();

    /**
     * Provjerava preklapa li se razdoblje s drugim {@code Schedulable} entitetom.
     *
     * @param other entitet za usporedbu
     * @return {@code true}: razdoblja se preklapaju, inače {@code false}
     */
    default boolean overlaps(Schedulable other) {
        if (other == null) {
            return false;
        }
        return !getStartDate().isAfter(other.getEndDate()) && !other.getStartDate().isAfter(getEndDate());
    }
}
