package hr.tvz.hotel.service;

import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import hr.tvz.hotel.files.CredentialsFile;
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
    private final CredentialsFile credentialsFile;

    /**
     * Kreira novu instancu servisa za prijavu.
     *
     * @param credentialsFile upravitelj datotekom s podacima za prijavu
     */
    public AuthService(CredentialsFile credentialsFile) {
        this.credentialsFile = credentialsFile;
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
            return credentialsFile.authenticate(username, password);
        } catch (CredentialsFileException e) {
            LOGGER.error("Prijava neuspjela: greška datoteke.", e);
            return Optional.empty();
        }
    }
}
