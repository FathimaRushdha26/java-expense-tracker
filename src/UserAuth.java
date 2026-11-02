
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.HexFormat;

public class UserAuth {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    public static String register(String username, String password) {

        username = username.trim().toLowerCase();

        if (!username.matches("[a-z0-9_]{3,20}")) {
            return "Username must contain 3-20 letters, numbers or underscores.";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            String saltHex = HexFormat.of().formatHex(salt);
            String hashHex = hashPassword(password, salt);

            String sql = """
                    INSERT INTO users (username, password_salt, password_hash)
                    VALUES (?, ?, ?)
                    """;

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, saltHex);
                statement.setString(3, hashHex);

                statement.executeUpdate();

                return null;
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            return "Username already exists.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Registration failed. Check database connection.";
        }
    }

    public static boolean authenticate(String username, String password) {

        String sql = """
                SELECT password_salt, password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim().toLowerCase());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String saltHex = result.getString("password_salt");
                    String storedHash = result.getString("password_hash");

                    byte[] salt = HexFormat.of().parseHex(saltHex);
                    String enteredHash = hashPassword(password, salt);

                    return java.security.MessageDigest.isEqual(
                            HexFormat.of().parseHex(storedHash),
                            HexFormat.of().parseHex(enteredHash)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static int getUserId(String username) {

        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim().toLowerCase());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("id");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static String hashPassword(String password, byte[] salt)
            throws Exception {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        spec.clearPassword();

        return HexFormat.of().formatHex(hash);
    }
}
