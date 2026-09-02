package hr.tvz.hotel.entities;

import java.time.LocalDate;

/**
 * Razdoblje omeđeno datumom početka i završetka.
 *
 * @author Viktor Barešić
 * @version 1.0
 * @param start datum početka razdoblja
 * @param end datum završetka razdoblja
 */
public record DateRange(LocalDate start, LocalDate end) implements Schedulable {

    @Override
    public LocalDate getStartDate() {
        return start;
    }

    @Override
    public LocalDate getEndDate() {
        return end;
    }
}
