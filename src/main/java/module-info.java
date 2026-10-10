module customer_manager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens customer_manager to javafx.fxml;
    exports customer_manager;
}
