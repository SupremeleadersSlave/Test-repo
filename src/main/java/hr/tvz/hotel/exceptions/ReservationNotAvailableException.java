package hr.tvz.hotel.exceptions;

/**
 * Označena iznimka: baca se kada soba nije raspoloživa za traženo
 * razdoblje, preklapa se s postojećom rezervacijom.
 * <p>
 * Označena je: pozivatelj, sloj usluga ili korisničko sučelje, mora
 * obraditi ovu poslovnu situaciju, npr. ponuditi korisniku drugo
 * razdoblje ili sobu.
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
     * @param cause   izvorni uzrok iznimke
     */
    public ReservationNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
