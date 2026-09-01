package hr.tvz.hotel.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Promjena podataka u aplikaciji.
 *
 * @param entityName naziv promijenjenog entiteta
 * @param entityId id promijenjenog entiteta
 * @param fieldName naziv promijenjenog polja
 * @param oldValue stara vrijednost
 * @param newValue nova vrijednost
 * @param changedByRole rola autora promjene
 * @param timestamp datum i vrijeme promjene
 *
 * @version 1.0
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
