package hr.tvz.hotel.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Funkcijsko sučelje za mapiranje retka rezultata upita u objekt.
 *
 * @param <T> tip mapiranog objekta
 *
 * @version 1.0
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Mapira trenutni redak rezultata upita.
     *
     * @param resultSet rezultat upita na trenutnom retku
     * @return mapirani objekt
     * @throws SQLException ako čitanje retka ne uspije
     */
    T map(ResultSet resultSet) throws SQLException;
}
