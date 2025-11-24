import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageUsersPage {

    private Stage stage;
    private TableView<UserTable> table;

    public ManageUsersPage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();

        Label title = new Label("Manage Users");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Buttons
        Button addUserBtn = new Button("Add User");
        Button deleteBtn = new Button("Delete User");
        Button backBtn = new Button("Back");

        // TableView
        table = new TableView<>();

        TableColumn<UserTable, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(c -> c.getValue().usernameProperty());

        TableColumn<UserTable, String> colFirst = new TableColumn<>("First Name");
        colFirst.setCellValueFactory(c -> c.getValue().firstnameProperty());

        TableColumn<UserTable, String> colLast = new TableColumn<>("Last Name");
        colLast.setCellValueFactory(c -> c.getValue().lastnameProperty());

        TableColumn<UserTable, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(c -> c.getValue().roleProperty());

        TableColumn<UserTable, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());

        TableColumn<UserTable, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c -> c.getValue().phoneProperty());

        table.getColumns().addAll(colUsername, colFirst, colLast, colRole, colEmail, colPhone);
        loadUsers();

        // Button Actions
        addUserBtn.setOnAction(e -> openAddUserForm());
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        backBtn.setOnAction(e -> {
            AdminDashboard admin = new AdminDashboard(stage, "Admin");
            admin.show();
        });

        HBox topBtns = new HBox(10, addUserBtn, deleteBtn, backBtn);
        topBtns.setPadding(new Insets(10));

        VBox vbox = new VBox(10, title, topBtns, table);
        vbox.setPadding(new Insets(15));

        root.setCenter(vbox);

        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    // Load users from DB
    private void loadUsers() {
        table.getItems().clear();

        String sql = "SELECT username, firstname, lastname, role, email, phone FROM users";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                table.getItems().add(new UserTable(
                        rs.getString("username"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete selected user
    private void deleteSelectedUser() {
        UserTable selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            alert("Please select a user to delete.");
            return;
        }

        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selected.getUsername());
            stmt.executeUpdate();

            loadUsers();
            alert("User deleted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add User form popup
    private void openAddUserForm() {
        Stage popup = new Stage();
        popup.setTitle("Add New User");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField username = new TextField();
        TextField firstname = new TextField();
        TextField lastname = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        TextField role = new TextField();

        PasswordField password = new PasswordField();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);

        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        grid.add(new Label("First Name:"), 0, 2);
        grid.add(firstname, 1, 2);

        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastname, 1, 3);

        grid.add(new Label("Role:"), 0, 4);
        grid.add(role, 1, 4);

        grid.add(new Label("Email:"), 0, 5);
        grid.add(email, 1, 5);

        grid.add(new Label("Phone:"), 0, 6);
        grid.add(phone, 1, 6);

        Button saveBtn = new Button("Save");

        saveBtn.setOnAction(e -> {

            // 1️⃣ Basic validation
            if (username.getText().isEmpty() ||
                    password.getText().isEmpty() ||
                    firstname.getText().isEmpty() ||
                    lastname.getText().isEmpty() ||
                    role.getText().isEmpty() ||
                    email.getText().isEmpty()) {

                alert("Please fill all required fields.");
                return;
            }

            // 2️⃣ Check if username already exists
            String checkSQL = "SELECT COUNT(*) FROM users WHERE username = ?";

            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {

                checkStmt.setString(1, username.getText());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    alert("Username already exists! Choose a different one.");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // 3️⃣ If username is new → then insert
            String salt = SecurityUtils.generateSalt();
            String hashed = SecurityUtils.hashPassword(password.getText(), salt);



            String sql = "INSERT INTO users (username, password, role, firstname, lastname, salt, email, phone) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username.getText());
                stmt.setString(2, hashed);
                stmt.setString(3, role.getText());
                stmt.setString(4, firstname.getText());
                stmt.setString(5, lastname.getText());
                stmt.setString(6, salt);
                stmt.setString(7, email.getText());
                stmt.setString(8, phone.getText());

                stmt.executeUpdate();
                popup.close();
                loadUsers();

                alert("User added successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(15, grid, saveBtn);
        root.setPadding(new Insets(15));

        popup.setScene(new Scene(root, 350, 400));
        popup.show();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.show();
    }

    // Inner class representing one user row
    public static class UserTable {
        private SimpleStringProperty username, firstname, lastname, role, email, phone;

        public UserTable(String username, String firstname, String lastname, String role,
                         String email, String phone) {
            this.username = new SimpleStringProperty(username);
            this.firstname = new SimpleStringProperty(firstname);
            this.lastname = new SimpleStringProperty(lastname);
            this.role = new SimpleStringProperty(role);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
        }

        public String getUsername() { return username.get(); }
        public SimpleStringProperty usernameProperty() { return username; }

        public SimpleStringProperty firstnameProperty() { return firstname; }
        public SimpleStringProperty lastnameProperty() { return lastname; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
    }
}
