package hr.tvz.hotel.app;

import hr.tvz.hotel.db.*;
import hr.tvz.hotel.db.Database;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import hr.tvz.hotel.files.ChangeLog;
import hr.tvz.hotel.files.CredentialsFile;
import hr.tvz.hotel.service.AuthService;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.service.InvoiceService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.RoomService;
import hr.tvz.hotel.service.UserService;
import hr.tvz.hotel.controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Starter klasa aplikacije. Inicijalizira bazu, usluge i upravitelje
 * datotekama te prilikom prvog pokretanja puni tablicu korisnika iz
 * tekstualne datoteke.
 *
 * @version 1.0
 */
public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    /**
     * Pokreće aplikaciju, inicijalizira bazu i usluge, prikazuje ekran
     * za prijavu.
     *
     * @param primaryStage glavni prozor, dodjeljuje JavaFX runtime
     * @throws Exception ako se ekran za prijavu ne učita
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.info("APP POKRENUT.");

        ServiceContext context = buildServiceContext();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setController(new LoginController(context, primaryStage));
        Parent root = loader.load();

        primaryStage.setTitle("Sustav za rezervaciju hotela - Prijava");
        primaryStage.setScene(new Scene(root, 400, 260));
        primaryStage.show();
    }

    /**
     * Izgrađuje kontekst usluga aplikacije: uspostavlja konekciju prema
     * bazi, inicijalizira shemu, kreira DAO objekte, usluge i
     * upravitelje datotekama, puni korisnike prilikom prvog pokretanja.
     *
     * @return izgrađeni kontekst usluga
     */
    private ServiceContext buildServiceContext() {
        Database database = new Database();
        try {
            database.connect();
            database.initializeSchema();
        } catch (SQLException e) {
            LOGGER.error("Inicijalizacija baze propala.", e);
            throw new IllegalStateException("Baza nedostupna.", e);
        }

        RoomDao roomDao = new RoomDao(database);
        GuestDao guestDao = new GuestDao(database);
        UserDao userDao = new UserDao(database);
        ReservationDao reservationDao = new ReservationDao(database, guestDao, roomDao);
        InvoiceDao invoiceDao = new InvoiceDao(database, reservationDao);

        ChangeLog changeLog = new ChangeLog(Path.of("data", "changelog.dat"));
        CredentialsFile credentialsFile = new CredentialsFile(Path.of("data", "credentials.txt"));

        seedUsersIfEmpty(userDao, credentialsFile);

        RoomService roomService = new RoomService(roomDao, changeLog);
        GuestService guestService = new GuestService(guestDao, changeLog);
        ReservationService reservationService = new ReservationService(reservationDao, changeLog);
        InvoiceService invoiceService = new InvoiceService(invoiceDao, changeLog);

        reservationService.setInvoiceService(invoiceService);
        guestService.setReservationService(reservationService);
        roomService.setReservationService(reservationService);

        return new ServiceContext(
                database,
                roomService,
                guestService,
                reservationService,
                invoiceService,
                new UserService(userDao, changeLog, credentialsFile),
                new AuthService(credentialsFile),
                changeLog);
    }

    /**
     * Puni tablicu korisnika iz tekstualne datoteke ako je prazna, npr.
     * prilikom prvog pokretanja aplikacije na praznoj bazi.
     *
     * @param userDao DAO za pristup korisnicima u bazi podataka
     * @param credentialsFile upravitelj datotekom s podacima za prijavu
     */
    private void seedUsersIfEmpty(UserDao userDao, CredentialsFile credentialsFile) {
        if (!userDao.findAll().isEmpty()) {
            return;
        }
        try {
            credentialsFile.loadCredentials().values().forEach(entry -> {
                User user = new User(null, entry.username(), "(uvezeno)", null, null,
                        entry.username(), entry.passwordHash(), entry.role());
                userDao.insert(user);
                LOGGER.info("Korisnik uvezen iz datoteke: {}", entry.username());
            });
        } catch (CredentialsFileException e) {
            LOGGER.warn("Punjenje korisnika iz datoteke preskočeno.", e);
        }
    }

    /**
     * Ulazna točka programa.
     *
     * @param args argumenti naredbenog retka
     */
    public static void main(String[] args) {
        launch(args);
    }
}
