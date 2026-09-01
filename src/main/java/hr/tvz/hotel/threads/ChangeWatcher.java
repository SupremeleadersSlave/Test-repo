package hr.tvz.hotel.threads;

import hr.tvz.hotel.entities.ChangeRecord;
import hr.tvz.hotel.files.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Čita i ispisuje posljednju promjenu iz binarne datoteke.
 * Thread safe je za istovrremeni pristup.
 *
 * @version 1.0
 */
public class ChangeWatcher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeWatcher.class);

    private final ChangeLog changeLog;
    private final long intervalMillis;
    private volatile boolean running = true;
    private ChangeRecord lastSeenRecord;

    /**
     * Stvara zadatak za nadzor povijesti promjena.
     *
     * @param changeLog upravlja promjena
     * @param intervalMillis interval provjere (milis)
     */
    public ChangeWatcher(ChangeLog changeLog, long intervalMillis) {
        this.changeLog = changeLog;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        LOGGER.info("NIT RADI");
        while (running) {
            try {
                ChangeRecord lastChange = changeLog.getLastChange();
                if (lastChange != null && !lastChange.equals(lastSeenRecord)) {
                    lastSeenRecord = lastChange;
                    LOGGER.info("Promjena: {}", lastChange);
                }
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
