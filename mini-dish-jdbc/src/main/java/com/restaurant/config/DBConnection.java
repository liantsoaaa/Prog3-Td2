package com.restaurant.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String url;
    private final String username;
    private final String password;

    public DBConnection() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        this.url = dotenv.get("DB_URL", "jdbc:postgresql://localhost:5432/mini_dish_db");
        this.username = dotenv.get("DB_USERNAME", "mini_dish_manager");
        this.password = dotenv.get("DB_PASSWORD", "1234");
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        }
    }

    public void closeConnection(Connection connection) throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la fermeture de la connexion: " + e.getMessage(), e);
        }
    }
}