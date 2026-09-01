package hr.tvz.hotel.service;

import hr.tvz.hotel.db.UserDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.persistence.ChangeLogManager;
import hr.tvz.hotel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja korisnicima sustava.
 */
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final ChangeLogManager changeLogManager;
    private final EntityCollection<User> users = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje korisnicima sustava i
     * učitava postojeće korisnike iz baze podataka.
     *
     * @param userDao          DAO za pristup korisnicima u bazi podataka
     * @param changeLogManager upravitelj poviješću promjena
     */
    public UserService(UserDao userDao, ChangeLogManager changeLogManager) {
        this.userDao = userDao;
        this.changeLogManager = changeLogManager;
        refresh();
    }

    /**
     * Ponovno učitava korisnike sustava iz baze podataka u memorijsku kolekciju.
     */
    public final void refresh() {
        users.clear();
        userDao.findAll().forEach(users::add);
    }

    /**
     * Vraća sve trenutno učitane korisnike sustava.
     *
     * @return popis svih korisnika
     */
    public List<User> findAll() {
        return users.getAll();
    }

    /**
     * Pretražuje korisnike sustava prema zadanom uvjetu.
     *
     * @param predicate uvjet pretrage, lambda izraz
     * @return popis korisnika koji zadovoljavaju uvjet
     */
    public List<User> search(Predicate<User> predicate) {
        return users.filter(predicate);
    }

    /**
     * Vraća korisnike sustava sortirane prema zadanom komparatoru.
     *
     * @param comparator redoslijed sortiranja, lambda izraz
     * @return sortirani popis korisnika
     */
    public List<User> sortedBy(Comparator<User> comparator) {
        return users.sorted(comparator);
    }

    /**
     * Dodaje novog korisnika sustava, hashira lozinku, bilježi
     * promjenu u povijest promjena.
     *
     * @param user          novi korisnik
     * @param plainPassword lozinka u čitljivom obliku za hashiranje
     * @param changedBy     rola korisnika, izvršitelj promjene
     */
    public void addUser(User user, String plainPassword, Role changedBy) {
        user.setPasswordHash(PasswordHasher.hash(plainPassword));
        Long id = userDao.insert(user);
        user.setId(id);
        users.add(user);
        logChange(id, "sve", null, user.toString(), changedBy);
        LOGGER.info("Dodan novi korisnik: {}", user);
    }

    /**
     * Ažurira postojećeg korisnika sustava, bilježi promjenu u
     * povijest promjena.
     *
     * @param oldUser   korisnik prije izmjene
     * @param newUser   korisnik nakon izmjene
     * @param changedBy rola korisnika, izvršitelj promjene
     */
    public void updateUser(User oldUser, User newUser, Role changedBy) {
        userDao.update(newUser);
        users.remove(oldUser);
        users.add(newUser);
        logChange(newUser.getId(), "sve", oldUser.toString(), newUser.toString(), changedBy);
        LOGGER.info("Ažuriran korisnik: {}", newUser);
    }

    /**
     * Briše korisnika sustava, bilježi promjenu u povijest promjena.
     *
     * @param user      korisnik za brisanje
     * @param changedBy rola korisnika, izvršitelj promjene
     */
    public void deleteUser(User user, Role changedBy) {
        userDao.delete(user.getId());
        users.remove(user);
        logChange(user.getId(), "sve", user.toString(), null, changedBy);
        LOGGER.info("Obrisan korisnik: {}", user);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLogManager.append(new ChangeRecord("User", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
