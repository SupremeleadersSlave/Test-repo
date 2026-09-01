package hr.tvz.hotel.exceptions;

/**
 * Iznimka koja se baca kada datoteka s korisničkim podacima ne postoji,
 * ne može se pročitati ili ima neispravan format zapisa.
 */
public class CredentialsFileException extends Exception {

    /**
     * Kreira iznimku s opisnom porukom.
     *
     * @param message opis pogreške prilikom čitanja datoteke
     */
    public CredentialsFileException(String message) {
        super(message);
    }

    /**
     * Kreira iznimku s opisnom porukom i uzrokom.
     *
     * @param message opis pogreške prilikom čitanja datoteke
     * @param cause uzrok iznimke
     */
    public CredentialsFileException(String message, Throwable cause) {
        super(message, cause);
    }
}