package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.entities.Room;
import hr.tvz.hotel.entities.RoomType;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler JavaFX ekrana za upravljanje sobama, s tablicom TableView
 * za pretragu, dodavanje, uređivanje i brisanje soba. Uređivanje i
 * brisanje zahtijevaju potvrdu korisnika putem dijaloškog okvira.
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
    private TableColumn<Room, Integer> capacityColumn;
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
     * @param context     kontekst usluga aplikacije
     * @param currentRole rola prijavljenog korisnika, bilježena uz svaku promjenu
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
     * Ponovno učitava podatke o sobama iz usluge u tablicu.
     */
    public void reload() {
        refreshTable(roomService.findAll());
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
        roomService.deleteRoom(selected, currentRole);
        reload();
    }

    /**
     * Prikazuje dijalog za unos ili uređivanje podataka o sobi.
     *
     * @param existing soba za uređivanje, ili {@code null} za novu sobu
     * @return izgrađena soba kod potvrde unosa, inače prazan {@link Optional}
     */
    private Optional<Room> showRoomDialog(Room existing) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nova soba" : "Uređivanje sobe");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField roomNumberField = new TextField(existing != null ? existing.getRoomNumber() : "");
        ComboBox<RoomType> typeBox = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeBox.setValue(existing != null ? existing.getType() : RoomType.SINGLE);
        TextField priceField = new TextField(existing != null ? existing.getPricePerNight().toString() : "");
        TextField capacityField = new TextField(existing != null ? String.valueOf(existing.getCapacity()) : "");
        CheckBox activeBox = new CheckBox("Aktivna");
        activeBox.setSelected(existing == null || existing.isActive());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Broj sobe:"), roomNumberField);
        grid.addRow(1, new Label("Vrsta:"), typeBox);
        grid.addRow(2, new Label("Cijena/noć:"), priceField);
        grid.addRow(3, new Label("Kapacitet:"), capacityField);
        grid.addRow(4, activeBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            try {
                return new Room(existing != null ? existing.getId() : null, roomNumberField.getText(), typeBox.getValue(),
                        new BigDecimal(priceField.getText()), Integer.parseInt(capacityField.getText()), activeBox.isSelected());
            } catch (NumberFormatException e) {
                DialogUtils.showError("Neispravan unos", "Cijena i kapacitet moraju biti brojevi.");
                return null;
            }
        });
        return dialog.showAndWait();
    }
}
