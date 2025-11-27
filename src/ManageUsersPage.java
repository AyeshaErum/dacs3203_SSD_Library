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

    // Borrow info table (correct model)
    private TableView<UserBorrowInfo> borrowInfoTable = new TableView<>();

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
        Button viewUserBtn = new Button("View Selected User");   // ✅ ADDED BUTTON
        Button backBtn = new Button("Back");

        // Main Users Table
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

        TableColumn<UserTable, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        // Status dropdown
        colStatus.setCellFactory(column -> new TableCell<UserTable, String>() {
            private final ComboBox<String> combo = new ComboBox<>();

            {
                combo.getItems().addAll("active", "inactive", "blocked");
                combo.setOnAction(e -> {
                    UserTable user = getTableView().getItems().get(getIndex());
                    String newStatus = combo.getValue();
                    updateStatusInDB(user.getUsername(), newStatus);
                    user.statusProperty().set(newStatus);
                });
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                combo.setValue(status);
                setGraphic(combo);
            }
        });

        table.getColumns().addAll(colUsername, colFirst, colLast, colRole, colEmail, colPhone, colStatus);
        loadUsers();

        // View Selected User Button
        viewUserBtn.setOnAction(e -> {
            UserTable selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Please select a user first!");
                return;
            }
            loadBorrowInfo(selected.getUsername());
        });

        // Borrow Info Table
        TableColumn<UserBorrowInfo, String> bTitle = new TableColumn<>("Title");
        bTitle.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<UserBorrowInfo, String> bBorrow = new TableColumn<>("Borrowed On");
        bBorrow.setCellValueFactory(c -> c.getValue().borrowDateProperty());

        TableColumn<UserBorrowInfo, String> bDue = new TableColumn<>("Due Date");
        bDue.setCellValueFactory(c -> c.getValue().dueDateProperty());

        TableColumn<UserBorrowInfo, String> bStatus2 = new TableColumn<>("Status");
        bStatus2.setCellValueFactory(c -> c.getValue().statusProperty());

        TableColumn<UserBorrowInfo, String> bFine = new TableColumn<>("Fine");
        bFine.setCellValueFactory(c -> c.getValue().fineProperty());

        borrowInfoTable.getColumns().addAll(bTitle, bBorrow, bDue, bStatus2, bFine);

        addUserBtn.setOnAction(e -> openAddUserForm());
        deleteBtn.setOnAction(e -> deleteSelectedUser());
        backBtn.setOnAction(e -> new AdminDashboard(stage, "Admin").show());

        HBox topBtns = new HBox(10, addUserBtn, deleteBtn, viewUserBtn, backBtn);
        topBtns.setPadding(new Insets(10));

        VBox vbox = new VBox(10, title, topBtns, table,
                new Label("Borrow & Return History for Selected User:"),
                borrowInfoTable);

        vbox.setPadding(new Insets(15));
        root.setCenter(vbox);

        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    // Load Users
    private void loadUsers() {
        table.getItems().clear();

        String sql = "SELECT username, firstname, lastname, role, email, phone, status FROM users";

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
                        rs.getString("phone"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void openAddUserForm() {
        Stage popup = new Stage();
        popup.setTitle("Add New User");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField firstname = new TextField();
        TextField lastname = new TextField();
        TextField role = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();

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

            if (username.getText().isEmpty() || password.getText().isEmpty() ||
                    firstname.getText().isEmpty() || lastname.getText().isEmpty() ||
                    role.getText().isEmpty() || email.getText().isEmpty()) {
                alert("Please fill all fields.");
                return;
            }

            String sql = "INSERT INTO users (username, password, role, firstname, lastname, salt, email, phone) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String salt = SecurityUtils.generateSalt();
                String hashed = SecurityUtils.hashPassword(password.getText(), salt);

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

    // User Table Model
    public static class UserTable {
        private SimpleStringProperty username, firstname, lastname, role, email, phone, status;

        public UserTable(String username, String firstname, String lastname, String role,
                         String email, String phone, String status) {
            this.username = new SimpleStringProperty(username);
            this.firstname = new SimpleStringProperty(firstname);
            this.lastname = new SimpleStringProperty(lastname);
            this.role = new SimpleStringProperty(role);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.status = new SimpleStringProperty(status);
        }

        public String getUsername() { return username.get(); }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty firstnameProperty() { return firstname; }
        public SimpleStringProperty lastnameProperty() { return lastname; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty statusProperty() { return status; }
    }

    private void updateStatusInDB(String username, String newStatus) {
        String sql = "UPDATE users SET status = ?, failed_attempts = 0 WHERE username = ?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, username);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            alert("Failed to update status!");
        }
    }

    private void loadBorrowInfo(String username) {
        borrowInfoTable.getItems().clear();

        String sql = "SELECT b.title, br.borrow_date, br.due_date, br.return_date, br.status, br.fine_amount "
                + "FROM borrow_records br "
                + "JOIN books b ON br.book_id = b.book_id "
                + "WHERE br.username = ? ORDER BY br.borrow_date DESC";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    borrowInfoTable.getItems().add(new UserBorrowInfo(
                            rs.getString("title"),
                            String.valueOf(rs.getTimestamp("borrow_date")),
                            String.valueOf(rs.getTimestamp("due_date")),
                            rs.getString("status"),
                            String.format("%.2f", rs.getDouble("fine_amount"))
                    ));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Borrow Info Model
    public static class UserBorrowInfo {
        private SimpleStringProperty title, borrowDate, dueDate, status, fine;

        public UserBorrowInfo(String title, String borrowDate, String dueDate, String status, String fine) {
            this.title = new SimpleStringProperty(title);
            this.borrowDate = new SimpleStringProperty(borrowDate);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.status = new SimpleStringProperty(status);
            this.fine = new SimpleStringProperty(fine);
        }

        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty borrowDateProperty() { return borrowDate; }
        public SimpleStringProperty dueDateProperty() { return dueDate; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty fineProperty() { return fine; }
    }
}
