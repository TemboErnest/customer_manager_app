package customer_manager;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        new LoginScreen(primaryStage, this::showDashboard).show();
    }

    private void showDashboard() {
        new CustomerDashboard(primaryStage, this::showLoginScreen).show();
    }

    public static void setRoot(String fxml) {
        // Legacy support for older controller classes.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
