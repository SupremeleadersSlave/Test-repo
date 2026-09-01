package hr.tvz.hotel.controllers;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Capacity;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.RoomType;
import hr.tvz.hotel.exceptions.InvalidRoomException;
import hr.tvz.hotel.service.RoomService;
import hr.tvz.hotel.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za upravljanje sobama: pretraga, dodavanje,
 * uređivanje i brisanje soba uz potvrdu korisnika.
 *
 * @version 1.0
 */
public class RoomController {

    private final RoomService roomService;
    private final Role currentRole;

    @FXML
    private TableView<Room> tableView;
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, RoomType> typeColumn;
    @FXML
    private TableColumn<Room, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Room, Capacity> capacityColumn;
    @FXML
    private TableColumn<Room, Boolean> activeColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    /**
     * Kreira novi kontroler ekrana za sobe.
     *
     * @param context kontekst usluga aplikacije
     * @param currentRole uloga prijavljenog korisnika, bilježena uz svaku promjenu
     */
    public RoomController(ServiceContext context, Role currentRole) {
        this.roomService = context.roomService();
        this.currentRole = currentRole;
    }

    @FXML
    private void initialize() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        reload();
    }

    /**
     * Ponovno učitava podatke o sobama iz servisa u tablicu.
     */
    public void reload() {
        refreshTable(roomService.sortedBy(Comparator.comparing(r -> r.getRoomNumber())));
    }

    private void refreshTable(List<Room> rooms) {
        tableView.setItems(FXCollections.observableArrayList(rooms));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        refreshTable(roomService.search(r -> r.getRoomNumber().toLowerCase().contains(query)
                || r.getType().name().toLowerCase().contains(query)));
    }

    @FXML
    private void handleAdd() {
        showRoomDialog(null).ifPresent(room -> {
            if (roomService.roomNumberExists(room.getRoomNumber(), null)) {
                DialogUtils.showError("Neispravan broj sobe", "Soba " + room.getRoomNumber() + " već postoji.");
                return;
            }
            roomService.addRoom(room, currentRole);
            reload();
        });
    }

    @FXML
    private void handleEdit() {
        Room selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda uređivanja", "Želite li urediti odabranu sobu?")) {
            return;
        }
        showRoomDialog(selected).ifPresent(updated -> {
            if (roomService.roomNumberExists(updated.getRoomNumber(), selected.getId())) {
                DialogUtils.showError("Neispravan broj sobe", "Soba " + updated.getRoomNumber() + " već postoji.");
                return;
            }
            roomService.updateRoom(selected, updated, currentRole);
            reload();
        });
    }

    @FXML
    private void handleDelete() {
        Room selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!DialogUtils.confirm("Potvrda brisanja", "Želite li obrisati odabranu sobu?")) {
            return;
        }
        int count = roomService.countReservations(selected);
        if (count > 0 && !DialogUtils.confirm("Dodatno upozorenje",
                "Brisanje ove sobe povući će i brisanje " + count
                        + " povezanih rezervacija i pripadajućih računa. Nastaviti?")) {
            return;
        }
        roomService.deleteRoom(selected, currentRole);
        reload();
    }

    /**
     * Prikazuje dijalog za unos ili uređivanje podataka o sobi. Kat
     * određuje vrstu sobe, a broj sobe se sastavlja od kata i rednog
     * broja sobe (01 - 20).
     *
     * @param existing soba za uređivanje ili {@code null} za novu sobu
     * @return izgrađena soba ako je unos potvrđen, inače prazan {@link Optional}
     */
    private Optional<Room> showRoomDialog(Room existing) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nova soba" : "Uređivanje sobe");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<RoomType> floorBox = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        floorBox.setValue(existing != null ? existing.getType() : RoomType.SINGLE);
        Label typeLabel = new Label();
        floorBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomType t) {
                return t == null ? "" : t.getFloor() + ". kat (" + t + ")";
            }

            @Override
            public RoomType fromString(String string) {
                return null;
            }
        });

        TextField roomNumberField = new TextField(
                existing != null ? existing.getRoomNumber().substring(1) : "");
        roomNumberField.setPromptText("01 - 20");
        ComboBox<Capacity> capacityBox = new ComboBox<>(FXCollections.observableArrayList(Capacity.values()));
        capacityBox.setValue(existing != null ? existing.getCapacity() : Capacity.SINGLE);
        TextField priceField = new TextField(existing != null ? existing.getPricePerNight().toString() : "");
        CheckBox activeBox = new CheckBox("Aktivna");
        activeBox.setSelected(existing == null || existing.isActive());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Kat / vrsta:"), floorBox);
        grid.addRow(1, new Label("Broj sobe:"), roomNumberField);
        grid.addRow(2, new Label("Kapacitet:"), capacityBox);
        grid.addRow(3, new Label("Cijena/noć:"), priceField);
        grid.addRow(4, activeBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            RoomType type = floorBox.getValue();
            String roomNumber = type.getFloor() + roomNumberField.getText().trim();
            try {
                Room.validateRoomNumber(roomNumber);
                return new Room(existing != null ? existing.getId() : null, roomNumber, type,
                        new BigDecimal(priceField.getText()), capacityBox.getValue(), activeBox.isSelected());
            } catch (NumberFormatException e) {
                DialogUtils.showError("Neispravan unos", "Cijena mora biti broj.");
                return null;
            } catch (InvalidRoomException e) {
                DialogUtils.showError("Neispravan broj sobe", e.getMessage());
                return null;
            }
        });
        return DialogUtils.showAndWait(dialog);
    }
}
