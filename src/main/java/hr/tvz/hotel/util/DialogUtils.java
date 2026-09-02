package hr.tvz.hotel.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.util.StringConverter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Prikazuje JavaFX dijaloške okvire za potvrdu, obavijest i pogrešku.
 * Sadrži zajedničku logiku za njihovo prikazivanje.
 * <p>
 * Broji trenutno otvorene dijaloge: dok je barem jedan otvoren,
 * pozadinsko osvježavanje podataka se preskače kako se odabir u
 * tablicama ne bi gubio.
 *
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

    /**
     * Postavlja prikaz stavki padajućeg izbornika prema zadanoj funkciji.
     *
     * @param comboBox padajući izbornik za postavljanje prikaza
     * @param display funkcija koja pretvara stavku u tekstualni prikaz
     * @param <T> tip stavki padajućeg izbornika
     */
    public static <T> void setDisplay(ComboBox<T> comboBox, Function<T, String> display) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : display.apply(item);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }
}
