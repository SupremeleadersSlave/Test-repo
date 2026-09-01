package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Guest;
import hr.tvz.hotel.entities.Relation;
import hr.tvz.hotel.entities.Reservation;
import hr.tvz.hotel.entities.ReservationStatus;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.exceptions.InvalidReservationDateException;
import hr.tvz.hotel.exceptions.ReservationNotAvailableException;
import hr.tvz.hotel.service.GuestService;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.RoomService;
import hr.tvz.hotel.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Kontroler JavaFX ekrana za upravljanje rezervacijama: pretraga,
 * kreiranje, promjena statusa i brisanje rezervacija.
 *
 * @version 1.0
 */
public class ReservationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final hr.tvz.hotel.service.InvoiceService invoiceService;
    private final Role currentRole;

    @FXML
    private TableView<Reservation> tableView;
    @FXML
    private TableColumn<Reservation, String> guestColumn;
    @FXML
    private TableColumn<Reservation, String> roomColumn;
    @FXML
    private TableColumn<Reservation, LocalDate> checkInColumn;
    @FXML
    private TableColumn<Reservation, LocalDate> checkOutColumn;
    @FXML
    private TableColumn<Reservation, ReservationStatus> statusColumn;
    @FXML
    private TableColumn<Reservation, BigDecimal> priceColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label relationsLabel;

    /**
     * Kreira novi kontroler ekrana za rezervacije.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public ReservationController(ServiceContext context, Role currentRole) {
        this.reservationService = context.reservationService();
        this.guestService = context.guestService();
        this.roomService = context.roomService();
        this.invoiceService = context.invoiceService();
        this.currentRole = currentRole;
    }

    @FXML
    private void initialize() {
        guestColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getGuest().getFullName()));
        roomColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRoom().getRoomNumber()));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        reload();
    }

    /**
     * Ponovno učitava podatke o rezervacijama iz servisa u tablicu.
     */
    public void reload() {
        refreshTable(reservationService.sortedBy(Comparator.comparing(r -> r.getCheckInDate())));
        Set<Relation<Guest, Room>> relations = reservationService.findGuestRoomRelations();
        relationsLabel.setText("Veza gost-soba: " + relations.size());
    }

    private void refreshTable(List<Reservation> reservations) {
        tableView.setItems(FXCollections.observableArrayList(reservations));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        refreshTable(reservationService.search(r -> r.getGuest().getFullName().toLowerCase().contains(query)
                || r.getRoom().getRoomNumber().toLowerCase().contains(query)));
    }

    @FXML
    private void handleAdd() {
        showReservationDialog().ifPresent(request -> {
            try {
                reservationService.createReservation(request.guest(), request.room(), request.checkIn(), request.checkOut(), currentRole);
                reload();
            } catch (ReservationNotAvailableException | InvalidReservationDateException e) {
                LOGGER.warn("Rezervacija odbijena: {}", e.getMessage());
                DialogUtils.showError("Rezervacija nije moguća", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda uređivanja", "Želite li promijeniti status odabrane rezervacije?")) {
            return;
        }
        ChoiceDialog<ReservationStatus> dialog = new ChoiceDialog<>(selected.getStatus(), ReservationStatus.values());
        dialog.setTitle("Promjena statusa rezervacije");
        dialog.setHeaderText(null);
        dialog.setContentText("Novi status:");
        DialogUtils.showAndWait(dialog).ifPresent(newStatus -> {
            reservationService.changeStatus(selected, newStatus, currentRole);
            reload();
        });
    }

    @FXML
    private void handleDelete() {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda brisanja", "Želite li obrisati odabranu rezervaciju?")) {
            return;
        }
        int count = invoiceService.findByReservation(selected).size();
        if (count > 0 && !DialogUtils.confirm("Dodatno upozorenje",
                "Brisanje ove rezervacije povući će i brisanje " + count
                        + " povezanih računa. Nastaviti?")) {
            return;
        }
        reservationService.deleteReservation(selected, currentRole);
        reload();
    }

    private record NewReservationRequest(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
    }

    /**
     * Prikazuje dijalog za unos podataka o novoj rezervaciji.
     *
     * @return uneseni podaci ako je unos potvrđen i odabrani su gost i soba,
     *         inače prazan {@link Optional}
     */
    private Optional<NewReservationRequest> showReservationDialog() {
        Dialog<NewReservationRequest> dialog = new Dialog<>();
        dialog.setTitle("Nova rezervacija");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Guest> guestBox = new ComboBox<>(FXCollections.observableArrayList(guestService.findAll()));
        setDisplay(guestBox, Guest::getFullName);
        ComboBox<Room> roomBox = new ComboBox<>(FXCollections.observableArrayList(roomService.findAll()));
        setDisplay(roomBox, r -> r.getRoomNumber() + " (" + r.getType() + ")");
        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Gost:"), guestBox);
        grid.addRow(1, new Label("Soba:"), roomBox);
        grid.addRow(2, new Label("Dolazak:"), checkInPicker);
        grid.addRow(3, new Label("Odlazak:"), checkOutPicker);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK || guestBox.getValue() == null || roomBox.getValue() == null) {
                return null;
            }
            return new NewReservationRequest(guestBox.getValue(), roomBox.getValue(), checkInPicker.getValue(), checkOutPicker.getValue());
        });
        return DialogUtils.showAndWait(dialog);
    }

    /**
     * Postavlja prikaz stavki padajućeg izbornika prema zadanoj funkciji.
     *
     * @param comboBox padajući izbornik za postavljanje prikaza
     * @param display funkcija koja pretvara stavku u tekstualni prikaz
     * @param <T> tip stavki padajućeg izbornika
     */
    private <T> void setDisplay(ComboBox<T> comboBox, Function<T, String> display) {
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