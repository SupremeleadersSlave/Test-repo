package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Address;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.util.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za upravljanje gostima: pretraga, dodavanje,
 * uređivanje i brisanje gostiju uz potvrdu korisnika.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class GuestController {

    private final GuestService guestService;
    private final Role currentRole;

    @FXML
    private TableView<Guest> tableView;
    @FXML
    private TableColumn<Guest, String> firstNameColumn;
    @FXML
    private TableColumn<Guest, String> lastNameColumn;
    @FXML
    private TableColumn<Guest, String> emailColumn;
    @FXML
    private TableColumn<Guest, String> phoneColumn;
    @FXML
    private TableColumn<Guest, String> documentColumn;
    @FXML
    private TableColumn<Guest, String> cityColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    /**
     * Kreira novi kontroler ekrana za goste.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public GuestController(ServiceContext context, Role currentRole) {
        this.guestService = context.guestService();
        this.currentRole = currentRole;
    }

    @FXML
    private void initialize() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        documentColumn.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        cityColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAddress() != null ? data.getValue().getAddress().city() : ""));
        reload();
    }

    /**
     * Ponovno učitava podatke o gostima iz usluge u tablicu.
     */
    public void reload() {
        refreshTable(guestService.sortedBy(Comparator.comparing(g -> g.getLastName())));
    }

    private void refreshTable(List<Guest> guests) {
        tableView.setItems(FXCollections.observableArrayList(guests));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        refreshTable(guestService.search(g -> g.getFullName().toLowerCase().contains(query)
                || g.getDocumentNumber().toLowerCase().contains(query)));
    }

    @FXML
    private void handleAdd() {
        showGuestDialog(null).ifPresent(guest -> {
            guestService.addGuest(guest, currentRole);
            reload();
        });
    }

    @FXML
    private void handleEdit() {
        Guest selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda uređivanja", "Želite li urediti odabranog gosta?")) {
            return;
        }
        showGuestDialog(selected).ifPresent(updated -> {
            guestService.updateGuest(selected, updated, currentRole);
            reload();
        });
    }

    @FXML
    private void handleDelete() {
        Guest selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda brisanja", "Želite li obrisati odabranog gosta?")) {
            return;
        }
        guestService.deleteGuest(selected, currentRole);
        reload();
    }

    /**
     * Prikazuje dijalog za unos ili uređivanje podataka o gostu.
     *
     * @param existing gost za uređivanje ili {@code null} za novog gosta
     * @return izgrađeni gost ako je unos potvrđen, inače prazan {@link Optional}
     */
    private Optional<Guest> showGuestDialog(Guest existing) {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Novi gost" : "Uređivanje gosta");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Address address = existing != null ? existing.getAddress() : null;
        TextField firstNameField = new TextField(existing != null ? existing.getFirstName() : "");
        TextField lastNameField = new TextField(existing != null ? existing.getLastName() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");
        TextField documentField = new TextField(existing != null ? existing.getDocumentNumber() : "");
        TextField streetField = new TextField(address != null ? address.street() : "");
        TextField cityField = new TextField(address != null ? address.city() : "");
        TextField postalCodeField = new TextField(address != null ? address.postalCode() : "");
        TextField countryField = new TextField(address != null ? address.country() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Ime:"), firstNameField);
        grid.addRow(1, new Label("Prezime:"), lastNameField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Telefon:"), phoneField);
        grid.addRow(4, new Label("Broj dokumenta:"), documentField);
        grid.addRow(5, new Label("Ulica:"), streetField);
        grid.addRow(6, new Label("Grad:"), cityField);
        grid.addRow(7, new Label("Poštanski broj:"), postalCodeField);
        grid.addRow(8, new Label("Država:"), countryField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            Address newAddress = new Address(streetField.getText(), cityField.getText(), postalCodeField.getText(), countryField.getText());
            return new Guest(existing != null ? existing.getId() : null, firstNameField.getText(), lastNameField.getText(),
                    emailField.getText(), phoneField.getText(), documentField.getText(), newAddress);
        });
        return DialogUtils.showAndWait(dialog);
    }
}