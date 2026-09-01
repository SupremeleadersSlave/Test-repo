package hr.tvz.hotel.exceptions;

/**
 * Iznimka koja se baca kada traženi entitet nije pronađen.
 *
 * @version 1.0
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Kreira iznimku s opisnom porukom.
     *
     * @param message opis nepronađenog entiteta
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Kreira iznimku s opisnom porukom i uzrokom.
     *
     * @param message opis nepronađenog entiteta
     * @param cause uzrok iznimke
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}