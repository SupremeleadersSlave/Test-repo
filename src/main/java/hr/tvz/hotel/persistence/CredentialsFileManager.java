package hr.tvz.hotel.persistence;

import hr.tvz.hotel.entities.Role;
import hr.tvz.hotel.exceptions.CredentialsFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Čita korisnička imena i hashirane lozinke iz tekstualne datoteke,
 * koristi se za jednokratno punjenje baze podataka korisnicima
 * prilikom prvog pokretanja aplikacije.
 * <p>
 * Očekivani format svakog retka datoteke je
 * {@code korisnickoIme;hashLozinke;ROLA}.
 */
public class CredentialsFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsFileManager.class);
    private static final String SEPARATOR = ";";

    private final Path credentialsFilePath;

    /**
     * Kreira upravitelj datotekom s podacima za prijavu.
     *
     * @param credentialsFilePath putanja do tekstualne datoteke s podacima za prijavu
     */
    public CredentialsFileManager(Path credentialsFilePath) {
        this.credentialsFilePath = credentialsFilePath;
    }

    /**
     * Učitava sve podatke za prijavu iz tekstualne datoteke.
     *
     * @return mapa korisničkih imena na zapise o podacima za prijavu
     * @throws CredentialsFileException ako datoteka ne postoji ili sadrži neispravan format
     */
    public Map<String, CredentialEntry> loadCredentials() throws CredentialsFileException {
        if (!Files.exists(credentialsFilePath)) {
            throw new CredentialsFileException("Datoteka za prijavu ne postoji: " + credentialsFilePath);
        }
        Map<String, CredentialEntry> credentials = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(credentialsFilePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                CredentialEntry entry = parseLine(line);
                credentials.put(entry.username(), entry);
            }
        } catch (IOException e) {
            LOGGER.error("Čitanje datoteke za prijavu neuspjelo: {}", credentialsFilePath, e);
            throw new CredentialsFileException("Datoteka za prijavu se ne čita.", e);
        }
        return credentials;
    }

    /**
     * Rastavlja redak tekstualne datoteke u {@link CredentialEntry}.
     *
     * @throws CredentialsFileException ako redak nije u očekivanom formatu ili sadrži nepoznatu rolu
     */
    private CredentialEntry parseLine(String line) throws CredentialsFileException {
        String[] parts = line.split(SEPARATOR);
        if (parts.length != 3) {
            throw new CredentialsFileException("Neispravan format retka: " + line);
        }
        try {
            return new CredentialEntry(parts[0].trim(), parts[1].trim(), Role.valueOf(parts[2].trim()));
        } catch (IllegalArgumentException e) {
            throw new CredentialsFileException("Nepoznata rola: " + line, e);
        }
    }

    /**
     * Jedan redak podataka za prijavu, učitan iz tekstualne datoteke.
     *
     * @param username korisničko ime
     * @param passwordHash hashirana lozinka
     * @param role uloga korisnika
     */
    public record CredentialEntry(String username, String passwordHash, Role role) {
    }
}