package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.Data.PolicePost;
import org.marensovich.Bot.Data.SubscribeTypes;
import org.marensovich.Bot.Data.UserInfo;
import org.marensovich.Bot.Maps.MapUtils.Distance;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapLanguage;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapTheme;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapTypes;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
                    subscribe ENUM("none", "vip", "premium") NOT NULL DEFAULT 'none',
                    gen_map INTEGER NOT NULL DEFAULT 0,
                    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        String CREATE_POLICE_DATA_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS Police (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    latitude DOUBLE NOT NULL,
                    longitude DOUBLE NOT NULL,
                    post_type VARCHAR(20) NOT NULL,
                    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    comment TEXT
                )""";
        String CREATE_SUBSCRIBES_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS Subscribes (
                    user_id BIGINT PRIMARY KEY NOT NULL,
                    `type` ENUM('none', 'vip', 'premium') NOT NULL,
                    exp_at TIMESTAMP NOT NULL
                )""";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_ALL_USERS_TABLE_SQL);
            stmt.executeUpdate(CREATE_USERS_TABLE_SQL);
            stmt.executeUpdate(CREATE_POLICE_DATA_TABLE_SQL);
            stmt.executeUpdate(CREATE_SUBSCRIBES_TABLE_SQL);
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

    public UserInfo getUserInfo(long userId) {
        String sql = "SELECT " +
                "u.user_id, u.yandex_lang, u.yandex_theme, u.yandex_maptype, u.is_admin, u.subscribe, u.gen_map, u.registration_time, " +
                "s.type AS subscribe_type, s.exp_at AS subscription_expiration " +
                "FROM Users u " +
                "LEFT JOIN Subscribes s ON u.user_id = s.user_id " +
                "WHERE u.user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.userId = rs.getLong("user_id");
                    userInfo.yandexLang = rs.getString("yandex_lang");
                    userInfo.yandexTheme = rs.getString("yandex_theme");
                    userInfo.yandexMaptype = rs.getString("yandex_maptype");
                    userInfo.isAdmin = rs.getBoolean("is_admin");
                    userInfo.subscribe = rs.getString("subscribe");
                    userInfo.genMap = rs.getInt("gen_map");
                    userInfo.registrationTime = rs.getTimestamp("registration_time");
                    userInfo.subscribeType = rs.getString("subscribe_type");
                    userInfo.subscriptionExpiration = rs.getTimestamp("subscription_expiration");
                    return userInfo;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkUserIsAdmin(long userId) {
        String SQL = "SELECT is_admin FROM Users WHERE user_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_admin");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке статуса администратора: " + e.getMessage());
            return false;
        }
    }

    public void addSub(long userid, SubscribeTypes type) {
        String SQL_AddSub = "INSERT INTO Subscribes (user_id, `type`, exp_at) VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL 30 DAY)";
        String SQL_UpdateUser = "UPDATE Users SET subscribe = ? WHERE user_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtAdd = conn.prepareStatement(SQL_AddSub);
                 PreparedStatement stmtUpdate = conn.prepareStatement(SQL_UpdateUser)) {
                stmtAdd.setLong(1, userid);
                stmtAdd.setString(2, type.toString());
                int rowsAdded = stmtAdd.executeUpdate();

                stmtUpdate.setString(1, type.toString());
                stmtUpdate.setLong(2, userid);
                int rowsUpdated = stmtUpdate.executeUpdate();

                if (rowsAdded > 0 && rowsUpdated > 0) {
                    System.out.println("Подписка " + type + " успешно выдана пользователю " + userid);
                    conn.commit();
                } else {
                    System.out.println("Пользователь с ID " + userid + " не найден или ошибка при добавлении.");
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выдаче подписки пользователю " + userid + ": " + e.getMessage());
            throw new RuntimeException("Не удалось выдать подписку пользователю " + userid, e);
        }
    }

    public void resetSub(long userid) {
        String SQL_UpdateUser = "UPDATE Users SET subscribe = ? WHERE user_id = ?";
        String SQL_DeleteSubs = "DELETE FROM Subscribes WHERE user_id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtUpdateUser = conn.prepareStatement(SQL_UpdateUser);
                 PreparedStatement stmtDeleteSubs = conn.prepareStatement(SQL_DeleteSubs)) {

                stmtUpdateUser.setString(1, SubscribeTypes.None.getType());
                stmtUpdateUser.setLong(2, userid);
                int rowsUpdated = stmtUpdateUser.executeUpdate();

                stmtDeleteSubs.setLong(1, userid);
                int rowsDeleted = stmtDeleteSubs.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Подписка пользователя " + userid + " успешно обнулена.");
                    conn.commit();
                } else {
                    System.out.println("Пользователь с ID " + userid + " не найден или ошибка при обновлении.");
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при сбросе подписки пользователю " + userid + ": " + e.getMessage());
            throw new RuntimeException("Не удалось сбросить подписку пользователю " + userid, e);
        }
    }

    public Timestamp getExpAtForUser(long userId) {
        String sql = "SELECT exp_at FROM Subscribes WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("exp_at");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addPolicePost(Long userId, Location location, String postType, String comment){
        String SQL = "INSERT INTO Police (user_id, latitude, longitude, post_type, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)){
            stmt.setLong(1, userId);
            stmt.setDouble(2, location.getLatitude());
            stmt.setDouble(3, location.getLongitude());
            stmt.setString(4, postType);
            stmt.setString(5, comment);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось добавить пост", e);
        }
    }


    public void setUserYandexLang(long userId, YandexMapLanguage yandexMapLanguage){
        String SQL = "UPDATE Users SET yandex_lang = ? WHERE user_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, yandexMapLanguage.getLang());
                stmt.setLong(2, userId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Настройки пользователя " + userId + " успешно обновлены.");
                    conn.commit();
                } else {
                    System.out.println("Пользователь с ID " + userId + " не найден или ошибка при обновлении.");
                    conn.rollback();
                }
            } catch (SQLException e){
                throw new RuntimeException("Не удалось обновить настройки", e);
            }
    }

    public void setUserYandexTheme(long userId, YandexMapTheme yandexMapTheme){
        String SQL = "UPDATE Users SET yandex_lang = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, yandexMapTheme.getTheme());
            stmt.setLong(2, userId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Настройки пользователя " + userId + " успешно обновлены.");
                conn.commit();
            } else {
                System.out.println("Пользователь с ID " + userId + " не найден или ошибка при обновлении.");
                conn.rollback();
            }
        } catch (SQLException e){
            throw new RuntimeException("Не удалось обновить настройки", e);
        }
    }

    public void setUserYandexMapType(long userId, YandexMapTypes yandexMapType){
        String SQL = "UPDATE Users SET yandex_lang = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, yandexMapType.getType());
            stmt.setLong(2, userId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Настройки пользователя " + userId + " успешно обновлены.");
                conn.commit();
            } else {
                System.out.println("Пользователь с ID " + userId + " не найден или ошибка при обновлении.");
                conn.rollback();
            }
        } catch (SQLException e){
            throw new RuntimeException("Не удалось обновить настройки", e);
        }
    }

    public Map<String, String> getUserSettings(long userId) {
        Map<String, String> settings = new HashMap<>();
        String query = "SELECT yandex_lang, yandex_theme, yandex_maptype FROM Users WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                settings.put("yandex_lang", resultSet.getString("yandex_lang"));
                settings.put("yandex_theme", resultSet.getString("yandex_theme"));
                settings.put("yandex_maptype", resultSet.getString("yandex_maptype"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public void updateUserSetting(long userid, String setting, String value){
        String sql = "UPDATE Users SET " + setting + " = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setLong(2, userid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserSettings(long userId, YandexMapTheme currentTheme, YandexMapTypes currentMapType, YandexMapLanguage currentLang){
        String sql = "UPDATE Users SET yandex_theme = ?, yandex_maptype = ?, yandex_lang = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentTheme.getTheme());
            stmt.setString(2, currentMapType.getType());
            stmt.setString(3, currentLang.getLang());
            stmt.setLong(4, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long[] getAllUsersTableIDs() {
        String query = "SELECT user_id FROM All_Users";
        List<Long> userIds = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                userIds.add(resultSet.getLong("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[0];
        }

        return userIds.stream().mapToLong(Long::longValue).toArray();
    }

    public long[] getUsersTableIDs() {
        String query = "SELECT user_id FROM Users";
        List<Long> userIds = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                userIds.add(resultSet.getLong("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[0];
        }

        return userIds.stream().mapToLong(Long::longValue).toArray();
    }


    public long[] getAllUsers() {
        Set<Long> uniqueIds = new HashSet<>();

        try (Connection connection = getConnection()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id FROM All_Users")) {
                while (rs.next()) {
                    uniqueIds.add(rs.getLong("user_id"));
                }
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users")) {
                while (rs.next()) {
                    uniqueIds.add(rs.getLong("user_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new long[0];
        }

        return uniqueIds.stream().mapToLong(Long::longValue).toArray();
    }

    public List<PolicePost> getFilteredPolicePosts(double centerLat, double centerLon,
                                                   double maxDistanceKm) {
        List<PolicePost> result = new ArrayList<>();
        Instant now = Instant.now();

        String query = "SELECT id, user_id, latitude, longitude, post_type, registration_time, comment FROM Police";

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                long id = rs.getLong("id");
                long userId = rs.getLong("user_id");
                double lat = rs.getDouble("latitude");
                double lon = rs.getDouble("longitude");
                String type = rs.getString("post_type");
                Timestamp regTime = rs.getTimestamp("registration_time");
                String comment = rs.getString("comment");

                double distance = Distance.getDistanceInKm(centerLat, centerLon, lat, lon);
                if (distance > maxDistanceKm) continue;

                String distanceStr = Distance.getDistance(centerLat, centerLon, lat, lon);

                Instant postTime = regTime.toInstant();
                long hoursPassed = ChronoUnit.HOURS.between(postTime, now);
                long minutesPassed = ChronoUnit.MINUTES.between(postTime, now);

                boolean expired = false;
                if ("Пост ДПС".equals(type)) {
                    expired = hoursPassed >= 4;
                } else if ("Патрульная машина".equals(type)) {
                    expired = minutesPassed >= 15;
                }

                if (hoursPassed < 24) {
                    result.add(new PolicePost(id, userId, lat, lon, type, regTime, comment, expired, distanceStr));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isPostExpired(String postType, Instant postTime, Instant now) {
        long hoursPassed = ChronoUnit.HOURS.between(postTime, now);
        long minutesPassed = ChronoUnit.MINUTES.between(postTime, now);

        return ("Пост ДПС".equals(postType) && hoursPassed >= 4) ||
                ("Патрульная машина".equals(postType) && minutesPassed >= 15);
    }
    
    public List<PolicePost> getNearbyPosts(double centerLat, double centerLon) throws SQLException {
        List<PolicePost> posts = new ArrayList<>();
        Instant now = Instant.now();

        String query = "SELECT id, user_id, latitude, longitude, post_type, registration_time, comment FROM Police";

        try (Connection conn = TelegramBot.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lon = rs.getDouble("longitude");
                String type = rs.getString("post_type");
                Timestamp regTime = rs.getTimestamp("registration_time");

                // Проверяем срок годности
                Instant postTime = regTime.toInstant();
                long hoursPassed = ChronoUnit.HOURS.between(postTime, now);
                if (hoursPassed >= 24) continue;

                String distanceStr = Distance.getDistance(centerLat, centerLon, lat, lon);
                
                posts.add(new PolicePost(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        lat,
                        lon,
                        type,
                        regTime,
                        rs.getString("comment"),
                        isPostExpired(type, postTime, now),
                        distanceStr
                ));
            }
        }
        // Сортируем по расстоянию
        posts.sort((p1, p2) -> {
            double dist1 = Distance.getDistanceInKm(centerLat, centerLon, p1.latitude, p1.longitude);
            double dist2 = Distance.getDistanceInKm(centerLat, centerLon, p2.latitude, p2.longitude);
            return Double.compare(dist1, dist2);
        });
        return posts;
    }


    public void incrementGenMap(long userId) throws SQLException {
        String sql = "UPDATE Users SET gen_map = gen_map + 1 WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Пользователь с ID " + userId + " не найден");
            }
        }
    }

}