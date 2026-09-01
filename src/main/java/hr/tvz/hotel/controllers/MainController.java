package hr.tvz.hotel.controllers;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.threads.ChangeWatcher;
import hr.tvz.hotel.threads.DataRefresher;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.util.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Kontroler glavnog ekrana s navigacijom putem kartica. Pokreće i
 * zaustavlja pozadinske niti za osvježavanje i nadzor promjena.
 *
 * @version 1.0
 */
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private final ServiceContext context;
    private final Role currentRole;
    private final String username;
    private final Stage stage;

    private RoomController roomController;
    private GuestController guestController;
    private ReservationController reservationController;
    private InvoiceController invoiceController;
    private UserController userController;
    private HistoryController historyController;

    private DataRefresher dataRefresher;
    private ChangeWatcher changeWatcher;

    @FXML
    private TabPane tabPane;
    @FXML
    private Label userLabel;
    @FXML
    private Button logoutButton;

    /**
     * Kreira novi kontroler glavnog ekrana.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika
     * @param username korisničko ime prijavljenog korisnika
     * @param stage glavni prozor aplikacije
     */
    public MainController(ServiceContext context, Role currentRole, String username, Stage stage) {
        this.context = context;
        this.currentRole = currentRole;
        this.username = username;
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        try {
            roomController = new RoomController(context, currentRole);
            addTab("Sobe", "/fxml/rooms.fxml", roomController);
            guestController = new GuestController(context, currentRole);
            addTab("Gosti", "/fxml/guests.fxml", guestController);
            reservationController = new ReservationController(context, currentRole);
            addTab("Rezervacije", "/fxml/reservations.fxml", reservationController);
            invoiceController = new InvoiceController(context, currentRole);
            addTab("Računi", "/fxml/invoices.fxml", invoiceController);
            if (currentRole == Role.ADMIN) {
                userController = new UserController(context, currentRole);
                addTab("Korisnici", "/fxml/users.fxml", userController);
            }
            historyController = new HistoryController(context);
            addTab("Povijest promjena", "/fxml/history.fxml", historyController);
        } catch (IOException e) {
            LOGGER.error("ekran entiteta se ne ucitava", e);
        }

        dataRefresher = new DataRefresher(this::refreshAllTabs, 5000);
        changeWatcher = new ChangeWatcher(context.changeLog(), 3000);
        startDaemonThread(dataRefresher, "data-refresh-thread");
        startDaemonThread(changeWatcher, "changelog-watcher-thread");

        userLabel.setText("Prijavljen: " + username + " (" + currentRole + ")");
        stage.setOnCloseRequest(event -> shutdown());
        LOGGER.info("USER PRIJAVA: {} ({})", username, currentRole);
    }

    private void startDaemonThread(Runnable task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Učitava ekran entiteta iz zadane FXML datoteke i dodaje ga kao novu
     * karticu u glavni ekran aplikacije.
     *
     * @param title naziv kartice
     * @param fxmlPath putanja do FXML datoteke unutar resursa aplikacije
     * @param controller kontroler koji se povezuje s učitanim ekranom
     * @throws IOException ako se ekran ne može učitati
     */
    private void addTab(String title, String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setController(controller);
        Parent root = loader.load();
        tabPane.getTabs().add(new Tab(title, root));
    }

    /**
     * Ponovno učitava podatke svih usluga i osvježava prikaz u svim
     * karticama glavnog ekrana. Preskače se dok je otvoren dijalog kako
     * se odabir u tablicama ne bi gubio.
     */
    private void refreshAllTabs() {
        if (DialogUtils.isDialogOpen()) {
            return;
        }
        context.roomService().refresh();
        context.guestService().refresh();
        context.reservationService().refresh();
        context.invoiceService().refresh();
        roomController.reload();
        guestController.reload();
        reservationController.reload();
        invoiceController.reload();
        if (userController != null) {
            context.userService().refresh();
            userController.reload();
        }
        historyController.reload();
    }

    /**
     * Odjavljuje korisnika: zaustavlja pozadinske niti i vraća ekran za
     * prijavu, bez zatvaranja aplikacije i veze s bazom.
     */
    @FXML
    private void handleLogout() {
        if (!DialogUtils.confirm("Odjava", "Želite li se odjaviti?")) {
            return;
        }
        dataRefresher.stop();
        changeWatcher.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setController(new LoginController(context, stage));
            Parent root = loader.load();
            stage.setTitle("Sustav za rezervaciju hotela - Prijava");
            stage.setScene(new Scene(root, 400, 260));
        } catch (IOException e) {
            LOGGER.error("logout FAILED successfully", e);
            DialogUtils.showError("Greška", "Ekran za prijavu se ne može učitati.");
        }
    }

    /**
     * Zaustavlja pozadinske niti i zatvara vezu s bazom
     * prilikom zatvaranja aplikacije.
     */
    private void shutdown() {
        LOGGER.info("GASENJE");
        dataRefresher.stop();
        changeWatcher.stop();
        context.database().close();
    }
}