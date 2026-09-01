package hr.tvz.hotel.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Opisuje jednu promjenu podataka u aplikaciji.
 * <p>
 * Instance se serijaliziraju u binarnu datoteku radi prikaza povijesti promjena.
 *
 * @param entityName    naziv promijenjenog entiteta, npr. "Room"
 * @param entityId      identifikator promijenjenog entiteta
 * @param fieldName     naziv promijenjenog polja
 * @param oldValue      stara vrijednost polja, tekstualni prikaz
 * @param newValue      nova vrijednost polja, tekstualni prikaz
 * @param changedByRole rola korisnika, autora promjene
 * @param timestamp     datum i vrijeme promjene
 */
public record ChangeRecord(
        String entityName,
        Long entityId,
        String fieldName,
        String oldValue,
        String newValue,
        Role changedByRole,
        LocalDateTime timestamp
) implements Serializable {
}
