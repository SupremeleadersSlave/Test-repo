package hr.tvz.hotel.controllers;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.CardPayment;
import hr.tvz.hotel.entities.CashPayment;
import hr.tvz.hotel.entities.Invoice;
import hr.tvz.hotel.entities.PaymentMethod;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.service.InvoiceService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.util.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za upravljanje računima: pretraga, izdavanje
 * i brisanje računa uz potvrdu korisnika.
 *
 * @version 1.0
 */
public class InvoiceController {

    private static final String NEISPRAVAN_UNOS = "Neispravan unos";
    private static final String GOTOVINA = "Gotovina";

    private final InvoiceService invoiceService;
    private final ReservationService reservationService;
    private final Role currentRole;

    @FXML
    private TableView<Invoice> tableView;
    @FXML
    private TableColumn<Invoice, String> reservationColumn;
    @FXML
    private TableColumn<Invoice, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Invoice, String> paymentColumn;
    @FXML
    private TableColumn<Invoice, String> issueDateColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    /**
     * Kreira novi kontroler ekrana za račune.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public InvoiceController(ServiceContext context, Role currentRole) {
        this.invoiceService = context.invoiceService();
        this.reservationService = context.reservationService();
        this.currentRole = currentRole;
    }

    @FXML
    private void initialize() {
        reservationColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getReservation().getGuest().getFullName() + " - "
                        + data.getValue().getReservation().getRoom().getRoomNumber()));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMethod().describe()));
        issueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));
        reload();
    }

    /**
     * Ponovno učitava podatke o računima iz usluge u tablicu.
     */
    public void reload() {
        refreshTable(invoiceService.sortedBy(Comparator.comparing(Invoice::getIssueDate)));
    }

    private void refreshTable(List<Invoice> invoices) {
        tableView.setItems(FXCollections.observableArrayList(invoices));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        refreshTable(invoiceService.search(i -> i.getReservation().getGuest().getFullName().toLowerCase().contains(query)
                || i.getReservation().getRoom().getRoomNumber().toLowerCase().contains(query)));
    }

    @FXML
    private void handleAdd() {
        showInvoiceDialog().ifPresent(invoice -> {
            invoiceService.addInvoice(invoice, currentRole);
            reload();
        });
    }

    @FXML
    private void handleDelete() {
        Invoice selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda brisanja", "Želite li obrisati odabrani račun?")) {
            return;
        }
        invoiceService.deleteInvoice(selected, currentRole);
        reload();
    }

    /**
     * Prikazuje dijalog za unos podataka o novom računu, uključujući
     * odabir rezervacije i načina plaćanja.
     *
     * @return izgrađeni račun ako je unos potvrđen i rezervacija odabrana,
     * inače prazan {@link Optional}
     */
    private Optional<Invoice> showInvoiceDialog() {
        Dialog<Invoice> dialog = new Dialog<>();
        dialog.setTitle("Novi račun");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Reservation> reservationBox = new ComboBox<>(FXCollections.observableArrayList(reservationService.findAll()));
        DialogUtils.setDisplay(reservationBox, r -> r.getGuest().getFullName() + " - " + r.getRoom().getRoomNumber());
        TextField amountField = new TextField();
        Button calcButton = new Button("Izračunaj");
        calcButton.setOnAction(e -> {
            Reservation selected = reservationBox.getValue();
            if (selected == null) {
                DialogUtils.showError(NEISPRAVAN_UNOS, "Prvo odaberite rezervaciju.");
                return;
            }
            amountField.setText(selected.getTotalPrice().toString());
        });
        ChoiceBox<String> typeBox = new ChoiceBox<>(FXCollections.observableArrayList(GOTOVINA, "Kartica"));
        typeBox.setValue(GOTOVINA);
        TextField cashField = new TextField();
        TextField cardNumberField = new TextField();
        TextField authCodeField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Rezervacija:"), reservationBox);
        grid.addRow(1, new Label("Iznos:"), amountField, calcButton);
        grid.addRow(2, new Label("Način plaćanja:"), typeBox);
        grid.addRow(3, new Label("Primljena gotovina:"), cashField);
        grid.addRow(4, new Label("Broj kartice (maskiran):"), cardNumberField);
        grid.addRow(5, new Label("Autorizacijski kod:"), authCodeField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            Reservation reservation = reservationBox.getValue();
            if (reservation == null) {
                DialogUtils.showError(NEISPRAVAN_UNOS, "Potrebno je odabrati rezervaciju.");
                return null;
            }
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                PaymentMethod method = GOTOVINA.equals(typeBox.getValue())
                        ? new CashPayment(new BigDecimal(cashField.getText()))
                        : new CardPayment(cardNumberField.getText(), authCodeField.getText());
                return new Invoice(null, reservation, amount, method, LocalDateTime.now(ZoneId.systemDefault()));
            } catch (NumberFormatException _) {
                DialogUtils.showError(NEISPRAVAN_UNOS, "Iznos i primljena gotovina moraju biti brojevi.");
                return null;
            }
        });
        return DialogUtils.showAndWait(dialog);
    }

}