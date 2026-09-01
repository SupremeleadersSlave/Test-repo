package hr.tvz.hotel.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prikazuje JavaFX dijaloške okvire za potvrdu, obavijest i pogrešku.
 * Sadrži zajedničku logiku za njihovo prikazivanje.
 * <p>
 * Broji trenutno otvorene dijaloge: dok je barem jedan otvoren,
 * pozadinsko osvježavanje podataka se preskače kako se odabir u
 * tablicama ne bi gubio.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public final class DialogUtils {

    private static final AtomicInteger OPEN_DIALOGS = new AtomicInteger();

    private DialogUtils() {
    }

    /**
     * Prikazuje dijalog i čeka odgovor korisnika, uz bilježenje da je
     * dijalog otvoren.
     *
     * @param dialog dijalog za prikaz
     * @param <T> tip rezultata dijaloga
     * @return rezultat dijaloga, ili prazan {@link Optional} ako je dijalog odbačen
     */
    public static <T> Optional<T> showAndWait(Dialog<T> dialog) {
        OPEN_DIALOGS.incrementAndGet();
        try {
            return dialog.showAndWait();
        } finally {
            OPEN_DIALOGS.decrementAndGet();
        }
    }

    /**
     * Provjerava je li trenutno otvoren barem jedan dijalog.
     *
     * @return {@code true} ako je barem jedan dijalog otvoren
     */
    public static boolean isDialogOpen() {
        return OPEN_DIALOGS.get() > 0;
    }

    /**
     * Prikazuje dijalog za potvrdu akcije i čeka odgovor korisnika.
     *
     * @param title naslov dijaloga
     * @param message poruka koja se prikazuje korisniku
     * @return {@code true} ako je korisnik potvrdio akciju gumbom OK
     */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = showAndWait(alert);
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Prikazuje dijalog s porukom o grešci.
     *
     * @param title naslov dijaloga
     * @param message poruka o grešci koja se prikazuje korisniku
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        showAndWait(alert);
    }

    /**
     * Prikazuje dijalog s informativnom porukom.
     *
     * @param title naslov dijaloga
     * @param message informativna poruka koja se prikazuje korisniku
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        showAndWait(alert);
    }
}