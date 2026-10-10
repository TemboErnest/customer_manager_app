package customer_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class LoginScreen {
    private static final File USER_FILE = new File("users.txt");

    private final Stage stage;
    private final Runnable onLoginSuccess;
    private final Map<String, String> users = new HashMap<>();

    public LoginScreen(Stage stage, Runnable onLoginSuccess) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
        loadUsers();
    }

    public void show() {
        Label title = new Label("Customer Manager Hub");
        title.getStyleClass().add("login-title");

        HBox brand = new HBox(7, buildBrandLogo(), title);
        brand.setAlignment(Pos.CENTER_LEFT);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("login-input");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        VBox usernameGroup = new VBox(3);
        usernameGroup.getChildren().addAll(
                buildFieldLabel("Username", buildUserIcon(Color.web("#435876"))),
                usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("login-input");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter your password");
        visiblePasswordField.getStyleClass().add("login-input");
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        Button visibilityButton = new Button("\u25c9");
        visibilityButton.getStyleClass().add("password-toggle");
        visibilityButton.setFocusTraversable(false);
        visibilityButton.setOnAction(event -> {
            boolean showPassword = !visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(showPassword);
            visiblePasswordField.setManaged(showPassword);
            passwordField.setVisible(!showPassword);
            passwordField.setManaged(!showPassword);
            visibilityButton.setText(showPassword ? "\u25ce" : "\u25c9");
        });

        StackPane passwordFields = new StackPane(passwordField, visiblePasswordField);
        HBox.setHgrow(passwordFields, Priority.ALWAYS);
        HBox passwordInput = new HBox(passwordFields, visibilityButton);
        passwordInput.setAlignment(Pos.CENTER);
        passwordInput.getStyleClass().add("login-input-wrap");

        VBox passwordGroup = new VBox(3);
        passwordGroup.getChildren().addAll(
                buildFieldLabel("Password", buildLockIcon()),
                passwordInput);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("login-status");
        statusLabel.setMinHeight(14);
        statusLabel.setWrapText(true);

        Button forgotPasswordButton = new Button("Forgot Password?");
        forgotPasswordButton.getStyleClass().add("login-link");
        forgotPasswordButton.setOnAction(event -> new Alert(
                Alert.AlertType.INFORMATION,
                "Password resets are not set up for local accounts. Please contact your administrator.")
                .showAndWait());

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-submit");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("login-register");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                setStatus(statusLabel, "Username and password are required.", true);
                return;
            }

            String storedHash = users.get(username);
            String attemptedHash = hashPassword(password);
            if (storedHash != null && storedHash.equals(attemptedHash)) {
                onLoginSuccess.run();
            } else {
                setStatus(statusLabel, "Invalid username or password.", true);
            }
        });

        registerButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                setStatus(statusLabel, "Enter both username and password to register.", true);
                return;
            }

            if (users.containsKey(username)) {
                setStatus(statusLabel, "That username already exists.", true);
                return;
            }

            saveUser(username, password);
            setStatus(statusLabel, "Registration successful. You can now log in.", false);
        });

        VBox card = new VBox(4,
                brand,
                new Separator(),
                usernameGroup,
                passwordGroup,
                forgotPasswordButton,
                loginButton,
                registerButton,
                statusLabel);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 10, 16));
        card.setPrefSize(240, 232);
        card.setMinSize(240, 232);
        card.setMaxSize(240, 232);

        StackPane root = new StackPane(buildBackdrop(), card);
        root.getStyleClass().add("login-root");
        Scene scene = new Scene(root, 360, 280);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Customer Manager Hub");
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private HBox buildFieldLabel(String text, Region icon) {
        Label label = new Label(text);
        label.getStyleClass().add("login-field-label");
        HBox row = new HBox(5, icon, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Region buildUserIcon(Color color) {
        StackPane icon = new StackPane();
        icon.setMinSize(10, 12);
        icon.setPrefSize(10, 12);
        icon.setMaxSize(10, 12);

        Circle head = new Circle(2.7, color);
        head.setTranslateY(-3);
        Arc shoulders = new Arc(5, 9, 4.2, 3.2, 0, 180);
        shoulders.setType(ArcType.ROUND);
        shoulders.setFill(color);
        icon.getChildren().addAll(shoulders, head);
        return icon;
    }

    private Region buildLockIcon() {
        StackPane icon = new StackPane();
        icon.setMinSize(10, 12);
        icon.setPrefSize(10, 12);
        icon.setMaxSize(10, 12);

        Arc shackle = new Arc(5, 5, 2.5, 2.7, 0, 180);
        shackle.setType(ArcType.OPEN);
        shackle.setStroke(Color.web("#435876"));
        shackle.setStrokeWidth(1.5);
        shackle.setFill(Color.TRANSPARENT);

        Rectangle body = new Rectangle(7, 5.5, Color.web("#435876"));
        body.setArcWidth(1.5);
        body.setArcHeight(1.5);
        body.setTranslateY(3);
        icon.getChildren().addAll(shackle, body);
        return icon;
    }

    private Region buildBrandLogo() {
        StackPane logo = new StackPane();
        logo.setMinSize(29, 25);
        logo.setPrefSize(29, 25);
        logo.setMaxSize(29, 25);

        VBox backPerson = buildPerson(Color.web("#05bfae"));
        backPerson.setTranslateX(6);
        backPerson.setTranslateY(1);

        VBox frontPerson = buildPerson(Color.web("#1261ca"));
        frontPerson.setTranslateX(-3);
        frontPerson.setTranslateY(-1);

        logo.getChildren().addAll(backPerson, frontPerson);
        return logo;
    }

    private VBox buildPerson(Color color) {
        Circle head = new Circle(4.2, color);
        Rectangle shoulders = new Rectangle(13, 10, color);
        shoulders.setArcWidth(5);
        shoulders.setArcHeight(5);

        VBox person = new VBox(1, head, shoulders);
        person.setAlignment(Pos.CENTER);
        return person;
    }

    private Pane buildBackdrop() {
        Pane backdrop = new Pane();
        backdrop.getStyleClass().add("login-backdrop");
        backdrop.setPrefSize(360, 280);
        backdrop.setMinSize(360, 280);
        backdrop.setMaxSize(360, 280);
        backdrop.getChildren().addAll(
                polygon(Color.rgb(0, 66, 158, 0.35), 0, 0, 112, 0, 172, 43, 0, 20),
                polygon(Color.rgb(23, 113, 207, 0.53), 0, 22, 102, 38, 190, 91, 0, 151),
                polygon(Color.rgb(12, 101, 186, 0.54), 0, 149, 106, 179, 156, 204, 0, 222),
                polygon(Color.rgb(6, 71, 147, 0.56), 0, 195, 155, 183, 247, 211, 360, 200, 360, 280, 0, 280),
                polygon(Color.rgb(130, 255, 248, 0.36), 171, 0, 360, 0, 360, 51, 259, 76, 205, 59),
                polygon(Color.rgb(0, 117, 157, 0.34), 204, 108, 360, 58, 360, 160, 278, 191),
                polygon(Color.rgb(0, 87, 127, 0.38), 0, 223, 123, 199, 232, 224, 360, 210, 360, 280, 0, 280));

        Circle corner = new Circle(22, Color.web("#08738a"));
        corner.setLayoutX(367);
        corner.setLayoutY(269);
        backdrop.getChildren().add(corner);
        return backdrop;
    }

    private Polygon polygon(Color color, double... points) {
        Polygon polygon = new Polygon(points);
        polygon.setFill(color);
        return polygon;
    }

    private void loadUsers() {
        if (!USER_FILE.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to load users: " + e.getMessage());
        }
    }

    private void saveUser(String username, String password) {
        String hashed = hashPassword(password);
        users.put(username, hashed);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + "," + hashed);
            writer.newLine();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not save user: " + e.getMessage()).showAndWait();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void setStatus(Label statusLabel, String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
    }
}
