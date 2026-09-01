package hr.tvz.hotel.service;

import hr.tvz.hotel.db.GuestDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.persistence.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira logiku upravljanja gostima.
 */
public class GuestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestService.class);

    private final GuestDao guestDao;
    private final ChangeLogManager changeLogManager;
    private final EntityCollection<Guest> guests = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje gostima i učitava
     * postojeće goste iz baze podataka.
     *
     * @param guestDao         DAO za pristup gostima u bazi podataka
     * @param changeLogManager upravitelj poviješću promjena
     */
    public GuestService(GuestDao guestDao, ChangeLogManager changeLogManager) {
        this.guestDao = guestDao;
        this.changeLogManager = changeLogManager;
        refresh();
    }

    /**
     * Ponovno učitava goste iz baze podataka.
     */
    public final void refresh() {
        guests.clear();
        guestDao.findAll().forEach(guests::add);
    }

    /**
     * Vraća sve trenutno učitane goste.
     *
     * @return popis svih gostiju
     */
    public List<Guest> findAll() {
        return guests.getAll();
    }

    /**
     * Pretražuje goste prema zadanom uvjetu.
     *
     * @param predicate uvjet pretrage, lambda izraz
     * @return popis gostiju koji zadovoljavaju uvjet
     */
    public List<Guest> search(Predicate<Guest> predicate) {
        return guests.filter(predicate);
    }

    /**
     * Vraća goste sortirane prema zadanom komparatoru.
     *
     * @param comparator redoslijed sortiranja, lambda izraz
     * @return sortirani popis gostiju
     */
    public List<Guest> sortedBy(Comparator<Guest> comparator) {
        return guests.sorted(comparator);
    }

    /**
     * Dodaje novog gosta, bilježi promjenu u povijest promjena.
     *
     * @param guest     novi gost
     * @param changedBy rola korisnika, izvršitelj promjene
     */
    public void addGuest(Guest guest, Role changedBy) {
        Long id = guestDao.insert(guest);
        guest.setId(id);
        guests.add(guest);
        logChange(id, "sve", null, guest.toString(), changedBy);
        LOGGER.info("Dodan novi gost: {}", guest);
    }

    /**
     * Ažurira postojećeg gosta, bilježi promjenu u povijest promjena.
     *
     * @param oldGuest  gost prije izmjene
     * @param newGuest  gost nakon izmjene
     * @param changedBy rola korisnika, izvršitelj promjene
     */
    public void updateGuest(Guest oldGuest, Guest newGuest, Role changedBy) {
        guestDao.update(newGuest);
        guests.remove(oldGuest);
        guests.add(newGuest);
        logChange(newGuest.getId(), "sve", oldGuest.toString(), newGuest.toString(), changedBy);
        LOGGER.info("Ažuriran gost: {}", newGuest);
    }

    /**
     * Briše gosta, bilježi promjenu u povijest promjena.
     *
     * @param guest     gost za brisanje
     * @param changedBy rola korisnika, izvršitelj promjene
     */
    public void deleteGuest(Guest guest, Role changedBy) {
        guestDao.delete(guest.getId());
        guests.remove(guest);
        logChange(guest.getId(), "sve", guest.toString(), null, changedBy);
        LOGGER.info("Obrisan gost: {}", guest);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLogManager.append(new ChangeRecord("Guest", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
