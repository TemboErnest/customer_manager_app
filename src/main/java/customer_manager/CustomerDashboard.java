package customer_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CustomerDashboard {
    private static final File CUSTOMER_FILE = new File("customers.txt");

    private final Stage stage;
    private final Runnable onLogout;
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    public CustomerDashboard(Stage stage, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        loadCustomers();
    }

    public void show() {
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        MenuBar menuBar = buildMenuBar();
        root.setTop(menuBar);

        VBox content = new VBox(18);

        Label title = new Label("Customer Manager");
        title.getStyleClass().add("label-header");

        HBox summaryCards = new HBox(16);
        summaryCards.setPadding(new Insets(6, 0, 0, 0));
        summaryCards.getChildren().addAll(
                statCard("Total", String.valueOf(customers.size())),
                statCard("Provinces", String.valueOf(countDistinctProvinces())),
                statCard("Newest", newestCustomerLabel())
        );

        HBox body = new HBox(20);
        body.setAlignment(Pos.TOP_LEFT);

        VBox formCard = buildFormCard();
        formCard.setPrefWidth(380);

        VBox tableCard = buildTableCard();
        tableCard.setPrefWidth(700);

        body.getChildren().addAll(formCard, tableCard);
        content.getChildren().addAll(title, summaryCards, body);
        root.setCenter(content);

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Customer Manager Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem exportCsv = new MenuItem("Export CSV");
        exportCsv.setOnAction(e -> exportCustomers());
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(exportCsv, exit);

        Menu accountMenu = new Menu("Account");
        MenuItem logout = new MenuItem("Log out");
        logout.setOnAction(e -> onLogout.run());
        accountMenu.getItems().add(logout);

        Menu helpMenu = new Menu("Help");
        MenuItem faqItem = new MenuItem("FAQ");
        faqItem.setOnAction(e -> showFaqDialog());
        MenuItem contactItem = new MenuItem("Contact Owner");
        contactItem.setOnAction(e -> showContactDialog());
        MenuItem reportIssue = new MenuItem("Report Issue");
        reportIssue.setOnAction(e -> reportIssue());
        helpMenu.getItems().addAll(faqItem, contactItem, reportIssue);

        menuBar.getMenus().addAll(fileMenu, accountMenu, helpMenu);
        return menuBar;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");

        Label header = new Label("Add Customer");
        header.getStyleClass().add("label-header");
        header.setStyle("-fx-font-size: 20px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Customer name");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getItems().addAll(
                "Central",
                "Copperbelt",
                "Eastern",
                "Luapula",
                "Lusaka",
                "Muchinga",
                "Northern",
                "North-Western",
                "Southern",
                "Western");
        provinceBox.setPromptText("Select province");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(5);
        notesArea.setPromptText("Notes or remarks");

        Label status = new Label();
        status.setWrapText(true);
        status.getStyleClass().add("status-label");

        Button addButton = new Button("Add Customer");
        addButton.setDefaultButton(true);

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("secondary");

        GridPane form = new GridPane();
        form.setVgap(12);
        form.setHgap(10);
        form.add(fieldLabel("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(fieldLabel("Province"), 0, 1);
        form.add(provinceBox, 1, 1);
        form.add(fieldLabel("Phone"), 0, 2);
        form.add(phoneField, 1, 2);
        form.add(fieldLabel("Email"), 0, 3);
        form.add(emailField, 1, 3);
        form.add(fieldLabel("Notes"), 0, 4);
        form.add(notesArea, 1, 4);

        HBox actions = new HBox(12, addButton, clearButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        addButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String notes = notesArea.getText().trim();

            if (name.isEmpty()) {
                setStatus(status, "Customer name is required.", true);
                return;
            }

            if (province == null || province.isBlank()) {
                setStatus(status, "Please choose a province.", true);
                return;
            }

            Customer customer = new Customer(name, province, phone, email, notes);
            customers.add(customer);
            saveCustomers();
            setStatus(status, "Customer added successfully.", false);
            clearForm(nameField, provinceBox, phoneField, emailField, notesArea);
        });

        clearButton.setOnAction(e -> {
            clearForm(nameField, provinceBox, phoneField, emailField, notesArea);
            status.setText("");
        });

        card.getChildren().addAll(header, form, actions, status);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Customer List");
        tableTitle.getStyleClass().add("label-header");
        tableTitle.setStyle("-fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or province");
        searchField.setPrefWidth(220);

        headerRow.getChildren().addAll(tableTitle, spacer, searchField);

        TableView<Customer> table = new TableView<>();
        table.setItems(customers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<Customer, String> provinceColumn = new TableColumn<>("Province");
        provinceColumn.setCellValueFactory(cell -> cell.getValue().provinceProperty());

        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());

        table.getColumns().addAll(nameColumn, provinceColumn, phoneColumn, emailColumn);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterCustomers(newValue, table);
        });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("danger");
        deleteButton.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            deleteButton.setDisable(newSel == null);
        });

        deleteButton.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this customer?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("Confirm Delete");
            confirm.setContentText("Are you sure you want to remove " + selected.getName() + "?");

            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    customers.remove(selected);
                    saveCustomers();
                }
            });
        });

        card.getChildren().addAll(headerRow, table, deleteButton);
        return card;
    }

    private VBox statCard(String label, String value) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-box");
        Label number = new Label(value);
        number.getStyleClass().add("stat-number");
        Label text = new Label(label);
        text.getStyleClass().add("stat-label");
        box.getChildren().addAll(number, text);
        box.setPrefWidth(160);
        return box;
    }

    private void filterCustomers(String searchText, TableView<Customer> table) {
        if (searchText == null || searchText.isBlank()) {
            table.setItems(customers);
            return;
        }

        String lower = searchText.toLowerCase();
        List<Customer> filtered = customers.stream()
                .filter(customer -> customer.getName().toLowerCase().contains(lower)
                        || customer.getProvince().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private int countDistinctProvinces() {
        return (int) customers.stream()
                .map(Customer::getProvince)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .count();
    }

    private String newestCustomerLabel() {
        if (customers.isEmpty()) {
            return "0";
        }
        Customer newest = customers.stream()
                .max(Comparator.comparing(Customer::getCreatedDate))
                .orElse(null);
        return newest == null ? "0" : newest.getName();
    }

    private void clearForm(TextField nameField, ComboBox<String> provinceBox, TextField phoneField, TextField emailField, TextArea notesArea) {
        nameField.clear();
        provinceBox.getSelectionModel().clearSelection();
        phoneField.clear();
        emailField.clear();
        notesArea.clear();
        nameField.requestFocus();
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text + ":");
        label.getStyleClass().add("field-label");
        return label;
    }

    private void setStatus(Label status, String text, boolean isError) {
        status.setText(text);
        status.getStyleClass().removeAll("status-error", "status-success");
        status.getStyleClass().add(isError ? "status-error" : "status-success");
    }

    private void loadCustomers() {
        if (!CUSTOMER_FILE.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CUSTOMER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Customer customer = Customer.fromStorageString(line);
                if (customer != null) {
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to load customers: " + e.getMessage());
        }
    }

    private void saveCustomers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CUSTOMER_FILE, false))) {
            for (Customer customer : customers) {
                writer.write(customer.toStorageString());
                writer.newLine();
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Problem saving customers: " + e.getMessage()).showAndWait();
        }
    }

    private void exportCustomers() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export customers");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        chooser.setInitialFileName("customers_export.csv");

        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Name,Province,Phone,Email,Notes,CreatedDate");
            writer.newLine();
            for (Customer customer : customers) {
                writer.write(String.join(",",
                        quote(customer.getName()),
                        quote(customer.getProvince()),
                        quote(customer.getPhone()),
                        quote(customer.getEmail()),
                        quote(customer.getNotes()),
                        quote(customer.getCreatedDate().toString())));
                writer.newLine();
            }
            new Alert(Alert.AlertType.INFORMATION, "Exported successfully.").showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage()).showAndWait();
        }
    }

    private String quote(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") || value.contains("\"") ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    private void showFaqDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("FAQ");
        alert.setHeaderText("Customer Manager Help");
        alert.setContentText("1. Add customers using the form to the left.\n2. Choose a province and save the record.\n3. Delete a selected customer from the table.\n4. Export your data to CSV from the File menu.");
        alert.showAndWait();
    }

    private void showContactDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Owner");
        alert.setHeaderText("Support Contact");
        alert.setContentText("Owner: TEMBO ERNEST\nPhone: 0767301330");
        alert.showAndWait();
    }

    private void reportIssue() {
        TextArea issueField = new TextArea();
        issueField.setPromptText("Describe your issue here...");
        issueField.setPrefRowCount(6);

        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Report Issue");
        alert.setHeaderText("Tell us what went wrong");
        alert.getDialogPane().setContent(issueField);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String issue = issueField.getText() == null ? "" : issueField.getText().trim();
                if (issue.isEmpty()) {
                    return;
                }

                File reportFile = new File("support_requests.txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile, true))) {
                    writer.write("Issue: " + issue);
                    writer.newLine();
                    writer.write("Reported: " + java.time.LocalDateTime.now());
                    writer.newLine();
                    writer.write("----");
                    writer.newLine();
                    new Alert(Alert.AlertType.INFORMATION, "Issue reported successfully.").showAndWait();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Unable to save issue report.").showAndWait();
                }
            }
        });
    }
}
