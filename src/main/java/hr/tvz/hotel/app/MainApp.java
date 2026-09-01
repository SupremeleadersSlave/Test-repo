package hr.tvz.hotel.app;

import hr.tvz.hotel.db.DatabaseConnection;
import hr.tvz.hotel.db.GuestDao;
import hr.tvz.hotel.db.InvoiceDao;
import hr.tvz.hotel.db.ReservationDao;
import hr.tvz.hotel.db.RoomDao;
import hr.tvz.hotel.db.UserDao;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import hr.tvz.hotel.persistence.ChangeLogManager;
import hr.tvz.hotel.persistence.CredentialsFileManager;
import hr.tvz.hotel.service.AuthService;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.service.InvoiceService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.RoomService;
import hr.tvz.hotel.service.UserService;
import hr.tvz.hotel.ui.LoginController;
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
 * Starter klasa aplikacije.
 * <p>
 * Inicijalizira slojeve aplikacije: bazu podataka, usluge, upravitelje
 * datoteka, prikaz početnog ekrana za prijavu. Prilikom prvog
 * pokretanja puni tablicu korisnika iz tekstualne datoteke.
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
        DatabaseConnection databaseConnection = new DatabaseConnection();
        try {
            databaseConnection.connect();
            databaseConnection.initializeSchema();
        } catch (SQLException e) {
            LOGGER.error("Inicijalizacija baze propala.", e);
            throw new IllegalStateException("Baza nedostupna.", e);
        }

        RoomDao roomDao = new RoomDao(databaseConnection);
        GuestDao guestDao = new GuestDao(databaseConnection);
        UserDao userDao = new UserDao(databaseConnection);
        ReservationDao reservationDao = new ReservationDao(databaseConnection, guestDao, roomDao);
        InvoiceDao invoiceDao = new InvoiceDao(databaseConnection, reservationDao);

        ChangeLogManager changeLogManager = new ChangeLogManager(Path.of("data", "changelog.dat"));

        seedUsersIfEmpty(userDao);

        return new ServiceContext(
                databaseConnection,
                new RoomService(roomDao, changeLogManager),
                new GuestService(guestDao, changeLogManager),
                new ReservationService(reservationDao, changeLogManager),
                new InvoiceService(invoiceDao, changeLogManager),
                new UserService(userDao, changeLogManager),
                new AuthService(userDao),
                changeLogManager);
    }

    /**
     * Puni tablicu korisnika iz {@code data/credentials.txt} ako je
     * prazna, npr. prilikom prvog pokretanja aplikacije na praznoj
     * bazi. Na sljedećim pokretanjima tablica više nije prazna, pa se
     * datoteka uopće ne čita.
     *
     * @param userDao DAO za pristup korisnicima u bazi podataka
     */
    private void seedUsersIfEmpty(UserDao userDao) {
        if (!userDao.findAll().isEmpty()) {
            return;
        }
        CredentialsFileManager credentialsFileManager = new CredentialsFileManager(Path.of("data", "credentials.txt"));
        try {
            credentialsFileManager.loadCredentials().values().forEach(entry -> {
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