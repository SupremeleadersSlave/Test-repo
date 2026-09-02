package hr.tvz.hotel.service;

import hr.tvz.hotel.db.UserDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import hr.tvz.hotel.files.ChangeLog;
import hr.tvz.hotel.files.CredentialsFile;
import hr.tvz.hotel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja korisnicima sustava i
 * održava tekstualnu datoteku za prijavu usklađenom s bazom.
 *
 * @version 1.0
 */
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final ChangeLog changeLog;
    private final CredentialsFile credentialsFile;
    private final EntityCollection<User> users = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje korisnicima sustava i
     * učitava postojeće korisnike iz baze.
     *
     * @param userDao DAO za pristup korisnicima u bazi
     * @param changeLog upravitelj poviješću promjena
     * @param credentialsFile upravitelj datotekom s podacima za prijavu
     */
    public UserService(UserDao userDao, ChangeLog changeLog, CredentialsFile credentialsFile) {
        this.userDao = userDao;
        this.changeLog = changeLog;
        this.credentialsFile = credentialsFile;
        refresh();
    }

    /**
     * Ponovno učitava korisnike sustava iz baze podataka u memoriju.
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
     * Pretražuje korisnike sustava prema uvjetu.
     *
     * @param predicate uvjet pretrage
     * @return popis korisnika koji zadovoljavaju uvjet
     */
    public List<User> search(Predicate<User> predicate) {
        return users.filter(predicate);
    }

    /**
     * Vraća korisnike sustava sortirane prema komparatoru.
     *
     * @param comparator redoslijed sortiranja
     * @return sortirani popis korisnika
     */
    public List<User> sortedBy(Comparator<User> comparator) {
        return users.sorted(comparator);
    }

    /**
     * Dodaje novog korisnika sustava, hashira lozinku, bilježi
     * promjenu u log.
     *
     * @param user novi korisnik
     * @param plainPassword lozinka u čitljivom obliku za hashiranje
     * @param changedBy korisnika koji izvršava promjene
     */
    public void addUser(User user, String plainPassword, Role changedBy) {
        user.setPasswordHash(PasswordHasher.hash(plainPassword));
        Long id = userDao.insert(user);
        user.setId(id);
        users.add(user);
        syncCredentialsFile();
        logChange(id, "sve", null, user.toString(), changedBy);
        LOGGER.info("Dodan Korisnik: {}", user);
    }

    /**
     * Ažurira postojećeg korisnika sustava, bilježi promjenu u
     * log.
     *
     * @param oldUser korisnik prije izmjene
     * @param newUser korisnik nakon izmjene
     * @param changedBy korisnika koji izvršava promjene
     */
    public void updateUser(User oldUser, User newUser, Role changedBy) {
        userDao.update(newUser);
        users.remove(oldUser);
        users.add(newUser);
        syncCredentialsFile();
        logChange(newUser.getId(), "sve", oldUser.toString(), newUser.toString(), changedBy);
        LOGGER.info("Korisnik Update: {}", newUser);
    }

    /**
     * Briše korisnika sustava, bilježi promjenu u log.
     *
     * @param user korisnik za brisanje
     * @param changedBy korisnika koji izvršava promjene
     */
    public void deleteUser(User user, Role changedBy) {
        userDao.delete(user.getId());
        users.remove(user);
        syncCredentialsFile();
        logChange(user.getId(), "sve", user.toString(), null, changedBy);
        LOGGER.info("Korisnik Obrisan: {}", user);
    }

    private void syncCredentialsFile() {
        List<CredentialsFile.CredentialEntry> entries = users.getAll().stream()
                .filter(u -> u.getUsername() != null && u.getPasswordHash() != null)
                .map(u -> new CredentialsFile.CredentialEntry(u.getUsername(), u.getPasswordHash(), u.getRole()))
                .toList();
        try {
            credentialsFile.saveCredentials(entries);
        } catch (CredentialsFileException e) {
            LOGGER.error("sync prijava datoteke pao", e);
        }
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLog.append(new ChangeRecord("User", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now(ZoneId.systemDefault())));
    }
}
