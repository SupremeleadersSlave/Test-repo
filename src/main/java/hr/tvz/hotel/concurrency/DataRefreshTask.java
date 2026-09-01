package hr.tvz.hotel.concurrency;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodički osvježava podatke na JavaFX sučelju.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class DataRefreshTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRefreshTask.class);

    private final Runnable refreshAction;
    private final long intervalMillis;
    private volatile boolean running = true;

    /**
     * Stvara zadatak za refresh podataka.
     *
     * @param refreshAction akcija refreshanja
     * @param intervalMillis interval refreshanja (ms)
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
     * Zaustavlja zadatak.
     */
    public void stop() {
        running = false;
    }
}
