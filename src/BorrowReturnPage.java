import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * BorrowReturnPage - UI for borrowing and returning books (with due date + fines).
 * Usage: new BorrowReturnPage(stage, username).show();
 */
public class BorrowReturnPage {
    private Stage stage;
    private String username;

    private TableView<Book> availableTable = new TableView<>();
    private TableView<UserBorrowRow> borrowedTable = new TableView<>();

    // Business rules
    private final int BORROW_DAYS = 14;           // default borrow period
    private final double FINE_PER_DAY = 1.00;     // currency units per overdue day

    public BorrowReturnPage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        Label title = new Label("Borrow / Return Books - User: " + username);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // --- AVAILABLE BOOKS TABLE ---
        TableColumn<Book, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().quantityProperty()); // we'll show quantity in UI too

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(c -> c.getValue().authorProperty());

        TableColumn<Book, Number> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());

        availableTable.getColumns().addAll(colTitle, colAuthor, colQty);
        availableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- BORROWED BOOKS TABLE (for this user) ---
        TableColumn<UserBorrowRow, String> bTitle = new TableColumn<>("Title");
        bTitle.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<UserBorrowRow, String> bBorrowDate = new TableColumn<>("Borrowed On");
        bBorrowDate.setCellValueFactory(c -> c.getValue().borrowDateProperty());

        TableColumn<UserBorrowRow, String> bDueDate = new TableColumn<>("Due Date");
        bDueDate.setCellValueFactory(c -> c.getValue().dueDateProperty());

        TableColumn<UserBorrowRow, String> bStatus = new TableColumn<>("Status");
        bStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        TableColumn<UserBorrowRow, String> bFine = new TableColumn<>("Fine");
        bFine.setCellValueFactory(c -> c.getValue().fineProperty());

        borrowedTable.getColumns().addAll(bTitle, bBorrowDate, bDueDate, bStatus, bFine);
        borrowedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons
        Button refreshBtn = new Button("Refresh");
        Button borrowBtn = new Button("Borrow Selected");
        Button returnBtn = new Button("Return Selected");
        Button backBtn = new Button("Back to Dashboard");

        // Actions
        refreshBtn.setOnAction(e -> {
            loadAvailableBooks();
            loadBorrowedBooks();
        });

        borrowBtn.setOnAction(e -> borrowSelectedBook());
        returnBtn.setOnAction(e -> returnSelectedBorrow());

        backBtn.setOnAction(e -> {
            UserDashboard ud = new UserDashboard(stage, username);
            ud.show();
        });

        HBox actionRow = new HBox(10, refreshBtn, borrowBtn, returnBtn, backBtn);
        actionRow.setPadding(new Insets(8));

        VBox center = new VBox(10,
                new Label("Available Books (select to borrow):"), availableTable,
                new Label("Your Borrowed Books (select to return):"), borrowedTable,
                actionRow
        );
        center.setPadding(new Insets(10));

        root.setTop(title);
        root.setCenter(center);

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Borrow / Return");
        stage.show();

        // initial load
        loadAvailableBooks();
        loadBorrowedBooks();
    }

    private void loadAvailableBooks() {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT book_id, title, author, category, quantity FROM books WHERE quantity > 0 ORDER BY title";
        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getInt("quantity")
                ));
            }
            availableTable.setItems(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error loading available books. See console.");
        }
    }

    private void loadBorrowedBooks() {
        ObservableList<UserBorrowRow> list = FXCollections.observableArrayList();
        String sql = "SELECT br.id, b.title, br.borrow_date, br.due_date, br.return_date, br.fine_amount, br.status " +
                "FROM borrow_records br JOIN books b ON br.book_id = b.book_id " +
                "WHERE br.username = ? ORDER BY br.borrow_date DESC";

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime borrowDate = toLocal(rs.getTimestamp("borrow_date"));
                    LocalDateTime dueDate = toLocal(rs.getTimestamp("due_date"));
                    LocalDateTime returnDate = null;
                    if (rs.getTimestamp("return_date") != null) returnDate = toLocal(rs.getTimestamp("return_date"));

                    double fine = rs.getDouble("fine_amount");
                    String status = rs.getString("status");

                    String borrowStr = borrowDate.format(fmt);
                    String dueStr = dueDate.format(fmt);

                    list.add(new UserBorrowRow(
                            rs.getInt("id"),
                            rs.getString("title"),
                            borrowStr,
                            dueStr,
                            status,
                            String.format("%.2f", fine)
                    ));
                }
            }
            borrowedTable.setItems(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error loading borrowed books. See console.");
        }
    }

    private void borrowSelectedBook() {
        Book sel = availableTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Select a book from the Available table to borrow.");
            return;
        }

        // --- LIMIT: user can borrow max 5 books ---
        String countSql = "SELECT COUNT(*) FROM borrow_records WHERE username = ? AND status = 'borrowed'";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pst = conn.prepareStatement(countSql)) {

            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) >= 5) {
                showAlert("You have reached the maximum limit of 5 borrowed books.");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error checking borrow limit.");
            return;
        }

        // Check available quantity
        if (sel.getQuantity() <= 0) {
            showAlert("Selected book is not available.");
            loadAvailableBooks();
            return;
        }

        String insertSql = "INSERT INTO borrow_records (book_id, username, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'borrowed')";
        String updateQtySql = "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(BORROW_DAYS);

        try (Connection conn = DBUtils.establishConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pst = conn.prepareStatement(insertSql);
                 PreparedStatement pst2 = conn.prepareStatement(updateQtySql)) {

                pst.setInt(1, sel.getId());
                pst.setString(2, username);
                pst.setObject(3, java.sql.Timestamp.valueOf(now));
                pst.setObject(4, java.sql.Timestamp.valueOf(due));
                pst.executeUpdate();

                pst2.setInt(1, sel.getId());
                pst2.executeUpdate();

                conn.commit();
                showAlert("Book borrowed. Due date: " + due.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Failed to borrow book. See console.");
        }

        loadAvailableBooks();
        loadBorrowedBooks();
    }


    private void returnSelectedBorrow() {
        UserBorrowRow sel = borrowedTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Select a borrowed record to return.");
            return;
        }

        // Fetch the borrow record (id) and book_id via DB so we can increment qty and set return_date + fine
        String fetchSql = "SELECT book_id, borrow_date, due_date FROM borrow_records WHERE id = ? AND username = ? AND status = 'borrowed'";
        String updateRecordSql = "UPDATE borrow_records SET return_date = ?, fine_amount = ?, status = 'returned' WHERE id = ?";
        String updateQtySql = "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pstFetch = conn.prepareStatement(fetchSql)) {

            pstFetch.setInt(1, sel.getId());
            pstFetch.setString(2, username);

            try (ResultSet rs = pstFetch.executeQuery()) {
                if (!rs.next()) {
                    showAlert("Record not found or already returned.");
                    loadBorrowedBooks();
                    return;
                }

                int bookId = rs.getInt("book_id");
                LocalDateTime dueDate = toLocal(rs.getTimestamp("due_date"));
                LocalDateTime now = LocalDateTime.now();

                // compute fine
                long overdueDays = 0;
                if (now.isAfter(dueDate)) {
                    Duration dur = Duration.between(dueDate, now);
                    overdueDays = dur.toDays();
                    if (overdueDays < 1 && dur.toHours() > 0) {
                        // count as 1 day if any hours overdue — you can tweak this behavior
                        overdueDays = 1;
                    }
                }
                double fine = overdueDays * FINE_PER_DAY;

                // perform update in a transaction
                conn.setAutoCommit(false);
                try (PreparedStatement pstUpdateRecord = conn.prepareStatement(updateRecordSql);
                     PreparedStatement pstUpdateQty = conn.prepareStatement(updateQtySql)) {

                    pstUpdateRecord.setObject(1, java.sql.Timestamp.valueOf(now));
                    pstUpdateRecord.setDouble(2, fine);
                    pstUpdateRecord.setInt(3, sel.getId());
                    pstUpdateRecord.executeUpdate();

                    pstUpdateQty.setInt(1, bookId);
                    pstUpdateQty.executeUpdate();

                    conn.commit();
                    if (fine > 0) {
                        showAlert(String.format("Book returned. Overdue: %d days. Fine: %.2f", overdueDays, fine));
                    } else {
                        showAlert("Book returned. No fine.");
                    }
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Failed to return book. See console.");
        }

        loadAvailableBooks();
        loadBorrowedBooks();
    }

    /* Helper to convert SQL timestamp to LocalDateTime (handles null) */
    private LocalDateTime toLocal(java.sql.Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // small inner class representing a user's borrow row
    public static class UserBorrowRow {
        private final int id;
        private final javafx.beans.property.SimpleStringProperty title;
        private final javafx.beans.property.SimpleStringProperty borrowDate;
        private final javafx.beans.property.SimpleStringProperty dueDate;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty fine;

        public UserBorrowRow(int id, String title, String borrowDate, String dueDate, String status, String fine) {
            this.id = id;
            this.title = new javafx.beans.property.SimpleStringProperty(title);
            this.borrowDate = new javafx.beans.property.SimpleStringProperty(borrowDate);
            this.dueDate = new javafx.beans.property.SimpleStringProperty(dueDate);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.fine = new javafx.beans.property.SimpleStringProperty(fine);
        }



        public int getId() { return id; }
        public javafx.beans.property.SimpleStringProperty titleProperty() { return title; }
        public javafx.beans.property.SimpleStringProperty borrowDateProperty() { return borrowDate; }
        public javafx.beans.property.SimpleStringProperty dueDateProperty() { return dueDate; }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
        public javafx.beans.property.SimpleStringProperty fineProperty() { return fine; }
    }
}
