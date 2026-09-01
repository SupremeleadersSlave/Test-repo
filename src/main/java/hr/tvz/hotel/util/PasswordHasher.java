package hr.tvz.hotel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pomoćna klasa za hashiranje lozinki korištenjem SHA-256 algoritma.
 * <p>
 * Lozinke se ne pohranjuju niti uspoređuju u čitljivom obliku, već
 * isključivo kao heksadecimalni prikaz njihovog hasha.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /**
     * Izračunava SHA-256 hash zadane lozinke i vraća ga u
     * heksadecimalnom obliku.
     *
     * @param plainPassword lozinka u čitljivom obliku
     * @return heksadecimalni prikaz hasha lozinke
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexBuilder = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nedostupan u JVM-u.", e);
        }
    }

    /**
     * Provjerava odgovara li zadana lozinka u čitljivom obliku
     * pohranjenom hashu.
     *
     * @param plainPassword lozinka u čitljivom obliku
     * @param expectedHash očekivani heksadecimalni hash
     * @return {@code true} ako se hash lozinke podudara s očekivanim hashom
     */
    public static boolean matches(String plainPassword, String expectedHash) {
        return hash(plainPassword).equalsIgnoreCase(expectedHash);
    }
}
