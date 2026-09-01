package hr.tvz.hotel.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Prikazuje JavaFX dijaloške okvire za potvrdu, obavijest i pogrešku.
 * Sadrži zajedničku logiku za njihovo prikazivanje.
 */
public final class DialogUtils {

    private DialogUtils() {
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
        Optional<ButtonType> result = alert.showAndWait();
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
        alert.showAndWait();
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
        alert.showAndWait();
    }
}
