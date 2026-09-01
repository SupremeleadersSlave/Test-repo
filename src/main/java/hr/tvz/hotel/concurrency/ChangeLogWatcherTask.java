package hr.tvz.hotel.concurrency;

import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.persistence.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Čita i ispisuje posljednju promjenu iz binarne datoteke.
 * Thread safe je za istovrremeni pristup.
 *
 * @author Viktor Barešić
 * @version 1.0
 */
public class ChangeLogWatcherTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeLogWatcherTask.class);

    private final ChangeLogManager changeLogManager;
    private final long intervalMillis;
    private volatile boolean running = true;
    private ChangeRecord lastSeenRecord;

    /**
     * Stvara zadatak za nadzor povijesti promjena.
     *
     * @param changeLogManager upravlja promjena
     * @param intervalMillis interval provjere (milis)
     */
    public ChangeLogWatcherTask(ChangeLogManager changeLogManager, long intervalMillis) {
        this.changeLogManager = changeLogManager;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        LOGGER.info("Nit pokrenuta.");
        while (running) {
            try {
                ChangeRecord lastChange = changeLogManager.getLastChange();
                if (lastChange != null && !lastChange.equals(lastSeenRecord)) {
                    lastSeenRecord = lastChange;
                    LOGGER.info("Nova promjena zabilježena: {}", lastChange);
                }
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
