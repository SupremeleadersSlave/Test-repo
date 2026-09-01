package hr.tvz.hotel.files;

import hr.tvz.hotel.entities.ChangeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sprema i učitava povijest promjena entiteta ({@link ChangeRecord})
 * u binarnu datoteku.
 *
 * @version 1.0
 */
public class ChangeLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeLog.class);

    private final Object lock = new Object();
    private final Path changeLogFilePath;

    /**
     * Kreira upravitelj binarnom datotekom povijesti promjena.
     *
     * @param changeLogFilePath putanja do binarne datoteke povijesti promjena
     */
    public ChangeLog(Path changeLogFilePath) {
        this.changeLogFilePath = changeLogFilePath;
    }

    /**
     * Dodaje promjenu na kraj povijesti i sprema ažuriranu povijest u
     * binarnu datoteku.
     *
     * @param changeRecord promjena koja se bilježi
     */
    public void append(ChangeRecord changeRecord) {
        synchronized (lock) {
            List<ChangeRecord> history = new ArrayList<>(readAllInternal());
            history.add(changeRecord);
            writeAll(history);
        }
    }

    /**
     * Čita povijest promjena iz binarne datoteke.
     *
     * @return nepromjenjivi popis svih zabilježenih promjena
     */
    public List<ChangeRecord> readAll() {
        synchronized (lock) {
            return Collections.unmodifiableList(readAllInternal());
        }
    }

    /**
     * Vraća posljednju zabilježenu promjenu, ako postoji.
     *
     * @return posljednja promjena ili {@code null} ako povijest ne postoji
     */
    public ChangeRecord getLastChange() {
        synchronized (lock) {
            List<ChangeRecord> history = readAllInternal();
            return history.isEmpty() ? null : history.get(history.size() - 1);
        }
    }

    /**
     * Čita povijest promjena iz binarne datoteke.
     *
     * @return popis svih zabilježenih promjena ili prazan popis ako datoteka ne postoji
     */
    @SuppressWarnings("unchecked")
    private List<ChangeRecord> readAllInternal() {
        File file = changeLogFilePath.toFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ChangeRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Čitanje povijesti promjena neuspjelo: {}", changeLogFilePath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Sprema povijest promjena u binarnu datoteku.
     */
    private void writeAll(List<ChangeRecord> history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(changeLogFilePath.toFile()))) {
            oos.writeObject(history);
        } catch (IOException e) {
            LOGGER.error("Spremanje povijesti promjena neuspjelo: {}", changeLogFilePath, e);
        }
    }
}