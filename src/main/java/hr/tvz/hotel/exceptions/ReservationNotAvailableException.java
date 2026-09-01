package hr.tvz.hotel.exceptions;

/**
 * Iznimka koja se baca kada soba nije raspoloživa za traženo razdoblje.
 *
 * @version 1.0
 */
public class ReservationNotAvailableException extends Exception {

    /**
     * Kreira iznimku s opisnom porukom.
     *
     * @param message opis razloga nedostupnosti sobe
     */
    public ReservationNotAvailableException(String message) {
        super(message);
    }

    /**
     * Kreira iznimku s opisnom porukom i uzrokom.
     *
     * @param message opis razloga nedostupnosti sobe
     * @param cause uzrok iznimke
     */
    public ReservationNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}