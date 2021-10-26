package me.william278.husksync.bungeecord.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import me.william278.husksync.HuskSyncBungeeCord;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "PRAGMA foreign_keys = ON;",
            "PRAGMA encoding = 'UTF-8';",

            "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE_NAME + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`uuid` char(36) NOT NULL UNIQUE," +
                    "`username` varchar(16) NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + DATA_TABLE_NAME + " (" +
                    "`player_id` integer NOT NULL REFERENCES " + PLAYER_TABLE_NAME + "(`id`)," +
                    "`version_uuid` char(36) NOT NULL UNIQUE," +
                    "`timestamp` datetime NOT NULL," +
                    "`inventory` longtext NOT NULL," +
                    "`ender_chest` longtext NOT NULL," +
                    "`health` double NOT NULL," +
                    "`max_health` double NOT NULL," +
                    "`health_scale` double NOT NULL," +
                    "`hunger` integer NOT NULL," +
                    "`saturation` float NOT NULL," +
                    "`saturation_exhaustion` float NOT NULL," +
                    "`selected_slot` integer NOT NULL," +
                    "`status_effects` longtext NOT NULL," +
                    "`total_experience` integer NOT NULL," +
                    "`exp_level` integer NOT NULL," +
                    "`exp_progress` float NOT NULL," +
                    "`game_mode` tinytext NOT NULL," +
                    "`statistics` longtext NOT NULL," +
                    "`is_flying` boolean NOT NULL," +
                    "`advancements` longtext NOT NULL," +
                    "`location` text NOT NULL," +

                    "PRIMARY KEY (`player_id`,`version_uuid`)" +
                    ");"
    };

    private static final String DATABASE_NAME = "HuskSyncData";

    private HikariDataSource dataSource;

    public SQLite(HuskSyncBungeeCord instance) {
        super(instance);
    }

    // Create the database file if it does not exist yet
    private void createDatabaseFileIfNotExist() {
        File databaseFile = new File(plugin.getDataFolder(), DATABASE_NAME + ".db");
        if (!databaseFile.exists()) {
            try {
                if (!databaseFile.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to write new file: " + DATABASE_NAME + ".db (file already exists)");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred writing a file: " + DATABASE_NAME + ".db (" + e.getCause() + ")");
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void load() {
        // Make SQLite database file
        createDatabaseFileIfNotExist();

        // Create new HikariCP data source
        final String jdbcUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + DATABASE_NAME + ".db";
        dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        dataSource.addDataSourceProperty("url", jdbcUrl);

        // Set various additional parameters
        dataSource.setMaximumPoolSize(hikariMaximumPoolSize);
        dataSource.setMinimumIdle(hikariMinimumIdle);
        dataSource.setMaxLifetime(hikariMaximumLifetime);
        dataSource.setKeepaliveTime(hikariKeepAliveTime);
        dataSource.setConnectionTimeout(hikariConnectionTimeOut);
        dataSource.setPoolName(DATA_POOL_NAME);
    }

    @Override
    public void createTables() {
        // Create tables
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                    statement.execute(tableCreationStatement);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables on the SQLite database: ", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}