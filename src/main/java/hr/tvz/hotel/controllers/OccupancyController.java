package hr.tvz.hotel.controllers;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.service.ReservationService;
import hr.tvz.hotel.service.ReservationService.Occupancy;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Map;

/**
 * Prikazuje popunjenost po mjesecima za tekuću godinu: broj rezervacija
 * i broj gostiju u svakom mjesecu. Dostupno administratoru i recepciji.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class OccupancyController {

    private final ReservationService reservationService;
    private final int year = LocalDate.now(ZoneId.systemDefault()).getYear();

    @FXML
    private TableView<MonthOccupancy> tableView;
    @FXML
    private TableColumn<MonthOccupancy, String> monthColumn;
    @FXML
    private TableColumn<MonthOccupancy, String> reservationsColumn;
    @FXML
    private TableColumn<MonthOccupancy, String> guestsColumn;
    @FXML
    private Button refreshButton;

    /**
     * Kreira novi kontroler ekrana popunjenosti.
     *
     * @param context kontekst usluga aplikacije
     */
    public OccupancyController(ServiceContext context) {
        this.reservationService = context.reservationService();
    }

    @FXML
    private void initialize() {
        monthColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().month()));
        reservationsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().reservations()));
        guestsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().guests()));
        reload();
    }

    /**
     * Ponovno računa popunjenost po mjesecima i osvježava tablicu.
     */
    @FXML
    public void reload() {
        Map<Month, Occupancy> byMonth = reservationService.occupancyByMonth(year);
        tableView.setItems(FXCollections.observableArrayList(
                byMonth.entrySet().stream()
                        .map(e -> new MonthOccupancy(
                                e.getKey().name(),
                                String.valueOf(e.getValue().reservations()),
                                String.valueOf(e.getValue().guests())))
                        .toList()));
    }

    /**
     * Jedan redak tablice popunjenosti: mjesec, broj rezervacija i broj
     * gostiju.
     *
     * @param month naziv mjeseca
     * @param reservations broj rezervacija tog mjeseca
     * @param guests broj gostiju tog mjeseca
     */
    public record MonthOccupancy(String month, String reservations, String guests) {
    }
}
