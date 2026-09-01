package hr.tvz.hotel.exceptions;

/**
 * Označena iznimka: baca se kada tekstualna datoteka s korisničkim
 * imenima i hashiranim lozinkama ne postoji, ne može se pročitati ili
 * ima neispravan format zapisa.
 * <p>
 * Označena je: čitanje vanjske datoteke može propasti iz razloga
 * izvan kontrole programa, npr. datoteka je obrisana ili premještena.
 * Pozivatelj mora obraditi tu situaciju.
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
     * @param cause   izvorni uzrok iznimke, npr. {@link java.io.IOException}
     */
    public CredentialsFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
