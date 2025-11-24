import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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


import java.sql.*;

public class ManageBooksPage {

    private Stage stage;
    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    private TableView<Book> table = new TableView<>();

    public ManageBooksPage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();

        Label title = new Label("Manage Books");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // -------------------- TABLE COLUMNS ----------------------
        TableColumn<Book, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colId.setPrefWidth(60);

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colTitle.setPrefWidth(200);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(c -> c.getValue().authorProperty());
        colAuthor.setPrefWidth(150);

        TableColumn<Book, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colCategory.setPrefWidth(120);

        TableColumn<Book, Number> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colQty.setPrefWidth(100);

        table.getColumns().addAll(colId, colTitle, colAuthor, colCategory, colQty);
        table.setItems(booksList);

        loadBooks();

        // -------------------- FORM INPUTS ----------------------
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        // -------------------- BUTTONS ----------------------
        Button addBtn = new Button("Add Book");
        Button updateBtn = new Button("Update Book");
        Button deleteBtn = new Button("Delete Book");
        Button backBtn = new Button("Back");

        // Add
        addBtn.setOnAction(e -> addBook(titleField, authorField, categoryField, qtyField));

        // Update (select row)
        updateBtn.setOnAction(e -> updateBook(titleField, authorField, categoryField, qtyField));

        // Delete
        deleteBtn.setOnAction(e -> deleteBook());

        // Back to Admin Dashboard
        backBtn.setOnAction(e -> {
            AdminDashboard ad = new AdminDashboard(stage, "Admin");
            ad.show();
        });

        // -------------------- LAYOUT FORM ----------------------
        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Title:"), 0, 0);
        form.add(titleField, 1, 0);

        form.add(new Label("Author:"), 0, 1);
        form.add(authorField, 1, 1);

        form.add(new Label("Category:"), 0, 2);
        form.add(categoryField, 1, 2);

        form.add(new Label("Quantity:"), 0, 3);
        form.add(qtyField, 1, 3);

        HBox actions = new HBox(15, addBtn, updateBtn, deleteBtn, backBtn);

        VBox left = new VBox(15, title, form, actions);
        left.setPadding(new Insets(20));

        root.setLeft(left);
        root.setCenter(table);

        stage.setScene(new Scene(root, 900, 500));
        stage.setTitle("Library - Manage Books");
        stage.show();
    }

    // -------------------- LOAD BOOKS ----------------------
    private void loadBooks() {
        booksList.clear();

        String sql = "SELECT * FROM books";
        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                booksList.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getInt("quantity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- ADD BOOK ----------------------
    private void addBook(TextField title, TextField author, TextField category, TextField qty) {
        if (title.getText().isEmpty() || author.getText().isEmpty() || qty.getText().isEmpty()) {
            showAlert("Please fill required fields.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qty.getText());
            if (quantity < 0) {
                showAlert("Quantity cannot be negative!");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Quantity must be a number.");
            return;
        }

        String sql = "INSERT INTO books (title, author, category, quantity) VALUES (?,?,?,?)";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title.getText());
            ps.setString(2, author.getText());
            ps.setString(3, category.getText());
            ps.setInt(4, Integer.parseInt(qty.getText()));

            ps.executeUpdate();
            loadBooks();
            showAlert("Book added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- UPDATE BOOK ----------------------
    private void updateBook(TextField title, TextField author, TextField category, TextField qty) {
        Book selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Select a book to update.");
            return;
        }

        String sql = "UPDATE books SET title=?, author=?, category=?, quantity=? WHERE book_id=?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title.getText());
            ps.setString(2, author.getText());
            ps.setString(3, category.getText());
            int quantity;
            try {
                quantity = Integer.parseInt(qty.getText());
                if (quantity < 0) {
                    showAlert("Quantity cannot be negative!");
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert("Quantity must be a number.");
                return;
            }

            ps.setInt(4, quantity);
            ps.setInt(5, selected.getId());

            ps.executeUpdate();
            loadBooks();
            showAlert("Book updated!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- DELETE BOOK ----------------------
    private void deleteBook() {
        Book selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Select a book to delete.");
            return;
        }

        String sql = "DELETE FROM books WHERE book_id=?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            loadBooks();
            showAlert("Book deleted!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- ALERT ----------------------
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
