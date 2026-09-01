package hr.tvz.hotel.exceptions;

/**
 * Iznimka koja se baca kada su datumi rezervacije neispravni.
 */
public class InvalidReservationDateException extends RuntimeException {

    /**
     * Kreira iznimku s opisnom porukom.
     *
     * @param message opis razloga neispravnosti datuma
     */
    public InvalidReservationDateException(String message) {
        super(message);
    }

    /**
     * Kreira iznimku s opisnom porukom i uzrokom.
     *
     * @param message opis razloga neispravnosti datuma
     * @param cause uzrok iznimke
     */
    public InvalidReservationDateException(String message, Throwable cause) {
        super(message, cause);
    }
}