package hr.tvz.hotel.ui;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.persistence.ChangeLogManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Prikazuje povijest promjena podataka: entitet, izmijenjeno polje,
 * staru i novu vrijednost, ulogu korisnika i vrijeme promjene.
 *
 * @version 1.0
 */
public class HistoryController {

    private final ChangeLogManager changeLogManager;

    @FXML
    private TableView<ChangeRecord> tableView;
    @FXML
    private TableColumn<ChangeRecord, String> entityNameColumn;
    @FXML
    private TableColumn<ChangeRecord, String> entityIdColumn;
    @FXML
    private TableColumn<ChangeRecord, String> fieldNameColumn;
    @FXML
    private TableColumn<ChangeRecord, String> oldValueColumn;
    @FXML
    private TableColumn<ChangeRecord, String> newValueColumn;
    @FXML
    private TableColumn<ChangeRecord, String> roleColumn;
    @FXML
    private TableColumn<ChangeRecord, String> timestampColumn;
    @FXML
    private Button refreshButton;

    /**
     * Kreira novi kontroler ekrana povijesti promjena.
     *
     * @param context kontekst usluga aplikacije
     */
    public HistoryController(ServiceContext context) {
        this.changeLogManager = context.changeLogManager();
    }

    @FXML
    private void initialize() {
        entityNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().entityName()));
        entityIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().entityId())));
        fieldNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().fieldName()));
        oldValueColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().oldValue())));
        newValueColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().newValue())));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().changedByRole().name()));
        timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().timestamp().toString()));
        reload();
    }

    /**
     * Ponovno učitava povijest promjena iz binarne datoteke u tablicu.
     */
    @FXML
    public void reload() {
        tableView.setItems(FXCollections.observableArrayList(changeLogManager.readAll()));
    }
}
