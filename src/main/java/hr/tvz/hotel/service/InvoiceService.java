package hr.tvz.hotel.service;

import hr.tvz.hotel.db.InvoiceDao;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.entities.EntityCollection;
import hr.tvz.hotel.entities.Invoice;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.persistence.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementira poslovnu logiku upravljanja računima izdanima za
 * rezervacije.
 */
public class InvoiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceDao invoiceDao;
    private final ChangeLogManager changeLogManager;
    private final EntityCollection<Invoice> invoices = new EntityCollection<>();

    /**
     * Kreira novu instancu servisa za upravljanje računima i učitava
     * postojeće račune iz baze podataka.
     *
     * @param invoiceDao DAO za pristup računima u bazi
     * @param changeLogManager upravitelj poviješću promjena
     */
    public InvoiceService(InvoiceDao invoiceDao, ChangeLogManager changeLogManager) {
        this.invoiceDao = invoiceDao;
        this.changeLogManager = changeLogManager;
        refresh();
    }

    /**
     * Ponovno učitava račune iz baze u memorijsku kolekciju.
     */
    public final void refresh() {
        invoices.clear();
        invoiceDao.findAll().forEach(invoices::add);
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
        changeLogManager.append(new ChangeRecord("Invoice", entityId, field, oldValue, newValue, changedBy, LocalDateTime.now()));
    }
}
