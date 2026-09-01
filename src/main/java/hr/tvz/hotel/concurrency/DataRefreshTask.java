package hr.tvz.hotel.concurrency;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodički poziva akciju osvježavanja podataka na JavaFX ekranu.
 * <p>
 * Akcija se izvršava na JavaFX aplikacijskoj niti putem
 * {@link Platform#runLater(Runnable)}: sigurno ažuriranje sučelja iz
 * pozadinske niti.
 */
public class DataRefreshTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRefreshTask.class);

    private final Runnable refreshAction;
    private final long intervalMillis;
    private volatile boolean running = true;

    /**
     * Kreira zadatak osvježavanja podataka.
     *
     * @param refreshAction  akcija za svako osvježavanje
     * @param intervalMillis razmak između dva osvježavanja u milisekundama
     */
    public DataRefreshTask(Runnable refreshAction, long intervalMillis) {
        this.refreshAction = refreshAction;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        LOGGER.info("Nit pokrenuta.");
        while (running) {
            try {
                Platform.runLater(refreshAction);
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                LOGGER.warn("Nit prekinuta.", e);
            }
        }
        LOGGER.info("Nit zaustavljena.");
    }

    /**
     * Zaustavlja zadatak osvježavanja podataka.
     */
    public void stop() {
        running = false;
    }
}
