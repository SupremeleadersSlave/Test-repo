package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.util.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za prijavu korisnika u aplikaciju.
 */
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private final ServiceContext context;
    private final Stage stage;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;

    /**
     * Kreira novi kontroler ekrana za prijavu.
     *
     * @param context kontekst usluga aplikacije
     * @param stage glavni prozor aplikacije
     */
    public LoginController(ServiceContext context, Stage stage) {
        this.context = context;
        this.stage = stage;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Optional<Role> role = context.authService().login(username, password);
        if (role.isEmpty()) {
            errorLabel.setText("Neispravno korisničko ime ili lozinka.");
            return;
        }
        try {
            showMainScreen(role.get(), username);
        } catch (IOException e) {
            LOGGER.error("Učitavanje glavnog ekrana propalo.", e);
            DialogUtils.showError("Greška", "Glavni ekran se ne može učitati.");
        }
    }

    /**
     * Učitava i prikazuje glavni ekran aplikacije za uspješno prijavljenog korisnika.
     *
     * @param role uloga prijavljenog korisnika
     * @param username korisničko ime prijavljenog korisnika
     * @throws IOException ako se glavni ekran ne može učitati
     */
    private void showMainScreen(Role role, String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setController(new MainController(context, role, username, stage));
        Parent root = loader.load();
        stage.setTitle("Sustav za rezervaciju hotela - " + username + " (" + role + ")");
        stage.setScene(new Scene(root, 950, 600));
    }
}
