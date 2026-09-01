package hr.tvz.hotel.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Funkcijsko sučelje koje mapira jedan redak rezultata upita
 * ({@link ResultSet}) u objekt zadanog tipa.
 *
 * @param <T> tip mapiranog objekta
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Mapira trenutni redak rezultata upita u objekt.
     *
     * @param resultSet rezultat upita, pozicioniran na redak
     * @return mapirani objekt
     * @throws SQLException: čitanje retka ne uspijeva
     */
    T map(ResultSet resultSet) throws SQLException;
}
