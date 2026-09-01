package hr.tvz.hotel.service;

import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import hr.tvz.hotel.persistence.CredentialsFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Logika prijave korisnika, provjerava podatke iz tekstualne datoteke.
 *
 * @version 1.0
 */
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final CredentialsFileManager credentialsFileManager;

    /**
     * Kreira novu instancu servisa za prijavu.
     *
     * @param credentialsFileManager upravitelj datotekom s podacima za prijavu
     */
    public AuthService(CredentialsFileManager credentialsFileManager) {
        this.credentialsFileManager = credentialsFileManager;
    }

    /**
     * Prijavljuje korisnika s korisničkim imenom i lozinkom.
     *
     * @param username korisničko ime
     * @param password lozinka
     * @return uloga prijavljenog korisnika ili prazan {@link Optional} kod neuspješne prijave
     */
    public Optional<Role> login(String username, String password) {
        try {
            return credentialsFileManager.authenticate(username, password);
        } catch (CredentialsFileException e) {
            LOGGER.error("Prijava neuspjela: greška datoteke.", e);
            return Optional.empty();
        }
    }
}
