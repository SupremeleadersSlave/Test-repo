package hr.tvz.hotel.exceptions;

/**
 * Označena iznimka koja se baca kada podaci o sobi nisu valjani, npr.
 * kada broj sobe ne odgovara očekivanom formatu kat + redni broj.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class InvalidRoomException extends Exception {

    /**
     * Kreira iznimku s porukom o pogrešci.
     *
     * @param message opis pogreške
     */
    public InvalidRoomException(String message) {
        super(message);
    }

    /**
     * Kreira iznimku s porukom o pogrešci i uzrokom.
     *
     * @param message opis pogreške
     * @param cause uzrok pogreške
     */
    public InvalidRoomException(String message, Throwable cause) {
        super(message, cause);
    }
}
