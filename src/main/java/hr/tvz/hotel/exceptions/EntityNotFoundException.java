package hr.tvz.hotel.exceptions;

/**
 * Neoznačena iznimka: baca se kada traženi entitet, npr. soba, gost,
 * rezervacija ili korisnik, nije pronađen prema zadanom identifikatoru.
 * <p>
 * Riječ je o pogrešci u toku programa: pokušaj rada s nepostojećim
 * identifikatorom. Ne predstavlja situaciju izravno uzrokovanu
 * korisničkim unosom podataka.
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
     * @param cause   izvorni uzrok iznimke
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
