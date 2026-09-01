package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Guest;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Kontroler JavaFX ekrana za upravljanje rezervacijama, s tablicom
 * TableView za pretragu, kreiranje, promjenu statusa i brisanje
 * rezervacija. Provjerava raspoloživost sobe prilikom kreiranja,
 * zahtijeva potvrdu korisnika za promjenu statusa i brisanje.
 */
public class ReservationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;
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

    /**
     * Kreira novi kontroler ekrana za rezervacije.
     *
     * @param context     kontekst usluga aplikacije
     * @param currentRole rola prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public ReservationController(ServiceContext context, Role currentRole) {
        this.reservationService = context.reservationService();
        this.guestService = context.guestService();
        this.roomService = context.roomService();
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
     * Ponovno učitava podatke o rezervacijama iz usluge u tablicu.
     */
    public void reload() {
        refreshTable(reservationService.findAll());
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
        dialog.showAndWait().ifPresent(newStatus -> {
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
        reservationService.deleteReservation(selected, currentRole);
        reload();
    }

    private record NewReservationRequest(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
    }

    /**
     * Prikazuje dijalog za unos podataka o novoj rezervaciji.
     *
     * @return uneseni podaci kod potvrde unosa i odabira gosta i sobe, inače prazan {@link Optional}
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
        return dialog.showAndWait();
    }

    /**
     * Postavlja prikaz stavki padajućeg izbornika prema zadanoj funkciji.
     *
     * @param comboBox        padajući izbornik za postavljanje prikaza
     * @param displayFunction funkcija, pretvara stavku u tekstualni prikaz
     * @param <T>             tip stavki padajućeg izbornika
     */
    private <T> void setDisplay(ComboBox<T> comboBox, Function<T, String> displayFunction) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : displayFunction.apply(item);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }
}
