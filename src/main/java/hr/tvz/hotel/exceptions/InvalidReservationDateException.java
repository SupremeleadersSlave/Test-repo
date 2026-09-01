package hr.tvz.hotel.exceptions;

/**
 * Neoznačena iznimka: baca se kada su datumi rezervacije neispravni,
 * npr. datum odlaska nije nakon datuma dolaska.
 * <p>
 * Riječ je o programerskoj, validacijskoj pogrešci: sprječava se prije
 * poziva poslovne logike. Ne predstavlja poslovnu situaciju za obradu
 * pozivatelja.
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
     * @param cause   izvorni uzrok iznimke
     */
    public InvalidReservationDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
