
import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginWindow() {

        setTitle("Expense Tracker - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        );

        JLabel heading = new JLabel(
                "EXPENSE TRACKER LOGIN",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 20));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        panel.add(heading);
        panel.add(createField("Username:", usernameField));
        panel.add(createField("Password:", passwordField));
        panel.add(buttonPanel);

        JLabel note = new JLabel(
                "New user? Create an account.",
                SwingConstants.CENTER
        );

        panel.add(note);

        add(panel);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
    }

    private JPanel createField(String label, JComponent field) {

        JPanel panel = new JPanel(new BorderLayout(8, 5));

        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private void login() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please enter username and password."
            );

            return;
        }

        if (UserAuth.authenticate(username, password)) {

            JOptionPane.showMessageDialog(
                    this, "Login successful!"
            );

            Main.startUserSession(username.toLowerCase());
            dispose();

        } else {

            JOptionPane.showMessageDialog(
                    this, "Invalid username or password."
            );
        }
    }

    private void register() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Enter username and password to register."
            );

            return;
        }

        String error = UserAuth.register(username, password);

        if (error == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully! You can now login."
            );

            passwordField.setText("");

        } else {

            JOptionPane.showMessageDialog(this, error);
        }
    }
}
