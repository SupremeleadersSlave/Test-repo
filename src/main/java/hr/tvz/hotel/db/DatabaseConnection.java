package hr.tvz.hotel.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Upravlja konekcijom prema H2 bazi podataka i izvršavanjem upita.
 * Baza se pohranjuje u datoteci unutar mape {@code data}.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String URL = "jdbc:h2:./data/hoteldb";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private Connection connection;

    /**
     * Vraća aktivnu konekciju prema bazi podataka ili uspostavlja novu.
     *
     * @return aktivna konekcija
     * @throws SQLException ako uspostavljanje konekcije ne uspije
     */
    public Connection connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.info("Konekcija uspostavljena: {}", URL);
        }
        return connection;
    }

    /**
     * Kreira tablice baze podataka ako ne postoje.
     *
     * @throws SQLException ako izvršavanje DDL naredbi ne uspije
     */
    public void initializeSchema() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    room_number VARCHAR(20) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    price_per_night DECIMAL(10,2) NOT NULL,
                    capacity INT NOT NULL,
                    active BOOLEAN NOT NULL
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS guests (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(150),
                    phone VARCHAR(30),
                    document_number VARCHAR(50),
                    street VARCHAR(150),
                    city VARCHAR(100),
                    postal_code VARCHAR(20),
                    country VARCHAR(100)
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(150),
                    phone VARCHAR(30),
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    guest_id BIGINT NOT NULL REFERENCES guests(id),
                    room_id BIGINT NOT NULL REFERENCES rooms(id),
                    check_in DATE NOT NULL,
                    check_out DATE NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    total_price DECIMAL(10,2) NOT NULL
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS invoices (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    reservation_id BIGINT NOT NULL REFERENCES reservations(id),
                    amount DECIMAL(10,2) NOT NULL,
                    payment_type VARCHAR(20) NOT NULL,
                    cash_amount_received DECIMAL(10,2),
                    card_masked_number VARCHAR(30),
                    card_authorization_code VARCHAR(30),
                    issue_date TIMESTAMP NOT NULL
                )""");
        LOGGER.info("Tablice provjerene/kreirane.");
    }

    /**
     * Izvršava upit bez rezultata: INSERT, UPDATE, DELETE, DDL.
     *
     * @param sql SQL naredba s parametrima
     * @param params vrijednosti parametara upita
     * @throws SQLException ako izvršavanje upita ne uspije
     */
    public void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = prepareStatement(sql, params)) {
            statement.executeUpdate();
        }
    }

    /**
     * Umeće redak i vraća generirani id.
     *
     * @param sql SQL INSERT naredba s parametrima
     * @param params vrijednosti parametara upita
     * @return generirani id umetnutog retka
     * @throws SQLException ako izvršavanje upita ne uspije
     */
    public long executeInsert(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connect().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindParameters(statement, params);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        }
    }

    /**
     * Dohvaća podatke i mapira retke pomoću mapera.
     *
     * @param sql SQL SELECT naredba s parametrima
     * @param mapper funkcija, mapiranje retka u objekt
     * @param params vrijednosti parametara upita
     * @param <T> tip mapiranog objekta
     * @return popis mapiranih objekata
     * @throws SQLException ako izvršavanje upita ne uspije
     */
    public <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement statement = prepareStatement(sql, params);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
        }
        return results;
    }

    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connect().prepareStatement(sql);
        bindParameters(statement, params);
        return statement;
    }

    private void bindParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    /**
     * Zatvara otvorenu konekciju prema bazi podataka.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Konekcija zatvorena.");
            } catch (SQLException e) {
                LOGGER.error("Zatvaranje konekcije neuspjelo.", e);
            }
        }
    }
}
