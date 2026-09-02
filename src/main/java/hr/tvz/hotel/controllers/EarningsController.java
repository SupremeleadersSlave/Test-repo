package hr.tvz.hotel.controllers;

import hr.tvz.hotel.app.ServiceContext;
import hr.tvz.hotel.service.InvoiceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Map;

/**
 * Prikazuje projekciju zarade po mjesecima za tekuću godinu, zbrojenu
 * iz izdanih računa. Dostupno samo administratoru.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class EarningsController {

    private final InvoiceService invoiceService;
    private final int year = LocalDate.now(ZoneId.systemDefault()).getYear();

    @FXML
    private TableView<MonthEarnings> tableView;
    @FXML
    private TableColumn<MonthEarnings, String> monthColumn;
    @FXML
    private TableColumn<MonthEarnings, String> amountColumn;
    @FXML
    private Button refreshButton;

    /**
     * Kreira novi kontroler ekrana zarade.
     *
     * @param context kontekst usluga aplikacije
     */
    public EarningsController(ServiceContext context) {
        this.invoiceService = context.invoiceService();
    }

    @FXML
    private void initialize() {
        monthColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().month()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().amount()));
        reload();
    }

    /**
     * Ponovno računa zaradu po mjesecima i osvježava tablicu.
     */
    @FXML
    public void reload() {
        Map<Month, BigDecimal> byMonth = invoiceService.earningsByMonth(year);
        tableView.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(Month.values())
                        .map(m -> new MonthEarnings(
                                m.name(),
                                byMonth.getOrDefault(m, BigDecimal.ZERO).toString()))
                        .toList()));
    }

    /**
     * Jedan redak tablice zarade: naziv mjeseca i ukupan iznos.
     *
     * @param month naziv mjeseca
     * @param amount ukupan iznos računa tog mjeseca
     */
    public record MonthEarnings(String month, String amount) {
    }
}
