package hr.tvz.hotel.threads;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodički osvježava podatke na JavaFX sučelju.
 *
 * @version 1.0
 */
public class DataRefresher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRefresher.class);

    private final Runnable refreshAction;
    private final long intervalMillis;
    private volatile boolean running = true;

    /**
     * Stvara zadatak za refresh podataka.
     *
     * @param refreshAction akcija refreshanja
     * @param intervalMillis interval refreshanja (ms)
     */
    public DataRefresher(Runnable refreshAction, long intervalMillis) {
        this.refreshAction = refreshAction;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        LOGGER.info("NIT RADI");
        while (running) {
            try {
                Platform.runLater(refreshAction);
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                LOGGER.warn("nit prekinuta", e);
            }
        }
        LOGGER.info("NIT STOP");
    }

    /**
     * Zaustavlja zadatak.
     */
    public void stop() {
        running = false;
    }
}
