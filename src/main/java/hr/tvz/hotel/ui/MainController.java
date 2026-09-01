package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.concurrency.ChangeLogWatcherTask;
import hr.tvz.hotel.concurrency.DataRefreshTask;
import hr.tvz.hotel.entities.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Kontroler glavnog ekrana aplikacije s navigacijom između pojedinih
 * ekrana entiteta putem kartica.
 * <p>
 * Pokreće pozadinske niti za periodičko osvježavanje podataka i nadzor
 * povijesti promjena te ih zaustavlja prilikom zatvaranja aplikacije.
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

    private DataRefreshTask dataRefreshTask;
    private ChangeLogWatcherTask changeLogWatcherTask;

    @FXML
    private TabPane tabPane;

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
            LOGGER.error("Učitavanje ekrana entiteta propalo.", e);
        }

        dataRefreshTask = new DataRefreshTask(this::refreshAllTabs, 5000);
        changeLogWatcherTask = new ChangeLogWatcherTask(context.changeLogManager(), 3000);
        startDaemonThread(dataRefreshTask, "data-refresh-thread");
        startDaemonThread(changeLogWatcherTask, "changelog-watcher-thread");

        stage.setOnCloseRequest(event -> shutdown());
        LOGGER.info("Prijavljen korisnik {}, rola {}.", username, currentRole);
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
     * karticama glavnog ekrana.
     */
    private void refreshAllTabs() {
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
     * Zaustavlja pozadinske niti i zatvara vezu s bazom
     * prilikom zatvaranja aplikacije.
     */
    private void shutdown() {
        LOGGER.info("Zatvaranje - zaustavljanje niti.");
        dataRefreshTask.stop();
        changeLogWatcherTask.stop();
        context.databaseConnection().close();
    }
}
