package hr.tvz.hotel.service;

import hr.tvz.hotel.db.InvoiceDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Invoice;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.exceptions.EntityNotFoundException;
import hr.tvz.hotel.files.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja računima izdanima za
 * rezervacije.
 *
 * @version 1.0
 */
public class InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceDao invoiceDao;
    private final ChangeLog changeLog;
    private final EntityCollection<Invoice> invoices = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje računima i učitava
     * postojeće račune iz baze podataka.
     *
     * @param invoiceDao DAO za pristup računima u bazi
     * @param changeLog upravitelj poviješću promjena
     */
    public InvoiceService(InvoiceDao invoiceDao, ChangeLog changeLog) {
        this.invoiceDao = invoiceDao;
        this.changeLog = changeLog;
        refresh();
    }

    /**
     * Ponovno učitava račune iz baze u memorijsku kolekciju.
     */
    public final void refresh() {
        invoices.clear();
        try {
            invoiceDao.findAll().forEach(invoices::add);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Učitavanje računa neuspjelo: rezervacija ne postoji.", e);
        }
    }

    /**
     * Vraća sve učitane račune.
     *
     * @return popis svih računa
     */
    public List<Invoice> findAll() {
        return invoices.getAll();
    }

    /**
     * Pretražuje račune prema uvjetu.
     *
     * @param predicate uvjet pretrage
     * @return popis računa koji zadovoljavaju uvjet
     */
    public List<Invoice> search(Predicate<Invoice> predicate) {
        return invoices.filter(predicate);
    }

    /**
     * Vraća račune sortirane prema komparatoru.
     *
     * @param comparator redoslijed sortiranja
     * @return sortirani popis računa
     */
    public List<Invoice> sortedBy(Comparator<Invoice> comparator) {
        return invoices.sorted(comparator);
    }

    /**
     * Vraća račune povezane sa zadanom rezervacijom.
     *
     * @param reservation rezervacija čiji se računi traže
     * @return popis računa te rezervacije
     */
    public List<Invoice> findByReservation(Reservation reservation) {
        return invoices.filter(i -> i.getReservation().equals(reservation));
    }

    /**
     * Izdaje novi račun, bilježi promjenu u logu.
     *
     * @param invoice   novi račun
     * @param changedBy rola korisnika koji izvršava promjene
     */
    public void addInvoice(Invoice invoice, Role changedBy) {
        Long id = invoiceDao.insert(invoice);
        invoice.setId(id);
        invoices.add(invoice);
        logChange(id, "sve", null, invoice.toString(), changedBy);
        LOGGER.info("Izdan novi račun: {}", invoice);
    }

    /**
     * Briše račun, bilježi promjenu u log.
     *
     * @param invoice   račun za brisanje
     * @param changedBy rola korisnika koji izvršava promjene
     */
    public void deleteInvoice(Invoice invoice, Role changedBy) {
        invoiceDao.delete(invoice.getId());
        invoices.remove(invoice);
        logChange(invoice.getId(), "sve", invoice.toString(), null, changedBy);
        LOGGER.info("Obrisan račun: {}", invoice);
    }

    private void logChange(Long entityId, String field, String oldValue, String newValue, Role changedBy) {
        changeLog.append(new ChangeRecord("Invoice", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
