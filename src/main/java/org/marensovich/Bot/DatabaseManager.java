package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.time.Instant;

public class DatabaseManager {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = dotenv.get("DATABASE_URL");
    private static final String DB_USER = dotenv.get("DATABASE_USER");
    private static final String DB_PASSWORD = dotenv.get("DATABASE_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver не найден!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void initializeDatabase() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String CREATE_ALL_USERS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS All_Users (
                user_id BIGINT PRIMARY KEY,
                registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""";
        String CREATE_USERS_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS Users (
                user_id BIGINT PRIMARY KEY,
                yandex_lang ENUM("ru_RU", "en_US", "en_RU", "ru_UA", "uk_UA", "tr_TR") NOT NULL DEFAULT 'ru_RU',
                yandex_theme ENUM("dark", "light") NOT NULL DEFAULT 'light',
                yandex_maptype ENUM('map', 'driving', 'transit', 'admin') NOT NULL DEFAULT 'map',
                is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                subscribe BOOLEAN NOT NULL DEFAULT FALSE,
                gen_map INTEGER NOT NULL DEFAULT 0,
                registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        String CREATE_POLICE_DATA_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS Police (
                id BIGINT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                latitude FLOAT NOT NULL,
                longitude FLOAT NOT NULL,
                registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                comment TEXT
                )""";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_ALL_USERS_TABLE_SQL);
            stmt.executeUpdate(CREATE_USERS_TABLE_SQL);
            stmt.executeUpdate(CREATE_POLICE_DATA_TABLE_SQL);
            System.out.println("Таблицы All_Users, Users и Police успешно созданы или уже существовали");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы: " + e.getMessage());
            throw new RuntimeException("Не удалось создать таблицу", e);
        }
    }

    public boolean checkAllUsersExists(long userId) {
        String SQL = "SELECT 1 FROM All_Users WHERE user_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке пользователя: " + e.getMessage());
            return false;
        }
    }

    public void addAllUser(long userId) {
        String SQL = "INSERT INTO All_Users (user_id, registration_time) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
            System.out.println("Пользователь " + userId + " успешно добавлен");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пользователя: " + e.getMessage());
            throw new RuntimeException("Не удалось добавить пользователя", e);
        }
    }

    public Timestamp getUserRegistrationTime(long userId) {
        String SQL = "SELECT registration_time FROM All_Users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("registration_time");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении времени регистрации: " + e.getMessage());
        }
        return null;
    }

    public int getTotalUsersCount() {
        String SQL = "SELECT COUNT(*) FROM All_Users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении количества пользователей: " + e.getMessage());
        }
        return 0;
    }

    public boolean checkUsersExists(long userId) {
        String SQL = "SELECT 1 FROM Users WHERE user_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке пользователя: " + e.getMessage());
            return false;
        }
    }

    public void addUser(long userId) {
        String SQL = "INSERT INTO Users (user_id) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            System.out.println("Пользователь " + userId + " успешно добавлен");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пользователя: " + e.getMessage());
            throw new RuntimeException("Не удалось добавить пользователя", e);
        }
    }
}