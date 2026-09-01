package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.User;
import hr.tvz.hotel.service.UserService;
import hr.tvz.hotel.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za upravljanje korisnicima sustava, dostupan
 * samo administratoru: pretraga, dodavanje, uređivanje i brisanje.
 */
public class UserController {

    private final UserService userService;
    private final Role currentRole;

    @FXML
    private TableView<User> tableView;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, Role> roleColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    /**
     * Kreira novi kontroler ekrana za korisnike sustava.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public UserController(ServiceContext context, Role currentRole) {
        this.userService = context.userService();
        this.currentRole = currentRole;
    }

    @FXML
    private void initialize() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        reload();
    }

    /**
     * Ponovno učitava podatke o korisnicima iz servisa u tablicu.
     */
    public void reload() {
        refreshTable(userService.findAll());
    }

    private void refreshTable(List<User> users) {
        tableView.setItems(FXCollections.observableArrayList(users));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        refreshTable(userService.search(u -> u.getUsername().toLowerCase().contains(query)
                || u.getFullName().toLowerCase().contains(query)));
    }

    @FXML
    private void handleAdd() {
        showUserDialog(null).ifPresent(result -> {
            userService.addUser(result.user(), result.plainPassword(), currentRole);
            reload();
        });
    }

    @FXML
    private void handleEdit() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda uređivanja", "Želite li urediti odabranog korisnika?")) {
            return;
        }
        showUserDialog(selected).ifPresent(result -> {
            result.user().setPasswordHash(selected.getPasswordHash());
            userService.updateUser(selected, result.user(), currentRole);
            reload();
        });
    }

    @FXML
    private void handleDelete() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda brisanja", "Želite li obrisati odabranog korisnika?")) {
            return;
        }
        userService.deleteUser(selected, currentRole);
        reload();
    }

    /**
     * Sadrži rezultat dijaloga za unos korisnika: izgrađenog korisnika i
     * lozinku u čitljivom obliku unesenu u dijalogu. Lozinka se hashira
     * tek u servisnom sloju.
     *
     * @param user izgrađeni korisnik
     * @param plainPassword lozinka u čitljivom obliku unesena u dijalogu
     */
    private record DialogResult(User user, String plainPassword) {
    }

    /**
     * Prikazuje dijalog za unos ili uređivanje podataka o korisniku.
     *
     * @param existing korisnik za uređivanje ili {@code null} za novog korisnika
     * @return rezultat dijaloga ako je unos potvrđen, inače prazan {@link Optional}
     */
    private Optional<DialogResult> showUserDialog(User existing) {
        Dialog<DialogResult> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Novi korisnik" : "Uređivanje korisnika");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField firstNameField = new TextField(existing != null ? existing.getFirstName() : "");
        TextField lastNameField = new TextField(existing != null ? existing.getLastName() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");
        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(existing == null ? "Lozinka" : "Ostavite prazno za zadržavanje stare lozinke");
        ComboBox<Role> roleBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleBox.setValue(existing != null ? existing.getRole() : Role.RECEPTIONIST);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Ime:"), firstNameField);
        grid.addRow(1, new Label("Prezime:"), lastNameField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Telefon:"), phoneField);
        grid.addRow(4, new Label("Korisničko ime:"), usernameField);
        grid.addRow(5, new Label("Lozinka:"), passwordField);
        grid.addRow(6, new Label("Rola:"), roleBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            User user = new User(existing != null ? existing.getId() : null, firstNameField.getText(), lastNameField.getText(),
                    emailField.getText(), phoneField.getText(), usernameField.getText(), null, roleBox.getValue());
            return new DialogResult(user, passwordField.getText());
        });
        return DialogUtils.showAndWait(dialog);
    }
}