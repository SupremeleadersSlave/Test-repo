package hr.tvz.hotel.service;

import hr.tvz.hotel.db.UserDao;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Logika prijave korisnika, provjerava podatke prema korisnicima
 * pohranjenim u bazi podataka.
 */
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;

    /**
     * Kreira novu instancu servisa za prijavu.
     *
     * @param userDao DAO za pristup korisnicima u bazi podataka
     */
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Prijavljuje korisnika s korisničkim imenom i lozinkom.
     *
     * @param username korisničko ime
     * @param password lozinka
     * @return uloga prijavljenog korisnika ili prazan {@link Optional} kod neuspješne prijave
     */
    public Optional<Role> login(String username, String password) {
        Optional<User> user = userDao.findByUsername(username);
        if (user.isEmpty()) {
            LOGGER.warn("Nepostojeće korisničko ime: {}", username);
            return Optional.empty();
        }
        if (!PasswordHasher.matches(password, user.get().getPasswordHash())) {
            LOGGER.warn("Neuspješna prijava: {}", username);
            return Optional.empty();
        }
        LOGGER.info("Prijava uspjela: {} ({})", username, user.get().getRole());
        return Optional.of(user.get().getRole());
    }
}