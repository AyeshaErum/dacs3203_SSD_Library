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

public class SearchBooksPage {

    private Stage stage;

    public SearchBooksPage(Stage stage) {
        this.stage = stage;
    }

    public void show() {

        BorderPane root = new BorderPane();

        Label title = new Label("Search Books");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter title, author, or category");

        Button searchBtn = new Button("Search");
        Button backBtn = new Button("Back");

        TableView<Book> table = new TableView<>();

        TableColumn<Book, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().quantityProperty());

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(c -> c.getValue().authorProperty());

        TableColumn<Book, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());

        TableColumn<Book, Number> colQuantity = new TableColumn<>("Quantity");
        colQuantity.setCellValueFactory(c -> c.getValue().quantityProperty());

        table.getColumns().addAll(colTitle, colAuthor, colCategory, colQuantity);

        // --- SEARCH ACTION ---
        searchBtn.setOnAction(e -> {
            String keyword = "%" + searchField.getText().trim() + "%";
            table.getItems().clear();

            String sql = "SELECT book_id, title, author, category, quantity FROM books " +
                    "WHERE title LIKE ? OR author LIKE ? OR category LIKE ?";

            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, keyword);
                pstmt.setString(2, keyword);
                pstmt.setString(3, keyword);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    table.getItems().add(
                            new Book(
                                    rs.getInt("book_id"),
                                    rs.getString("title"),
                                    rs.getString("author"),
                                    rs.getString("category"),
                                    rs.getInt("quantity")
                            )
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Back → go back to dashboard
        backBtn.setOnAction(e -> {
            UserDashboard ud = new UserDashboard(stage, "User");
            ud.show();
        });

        HBox topBox = new HBox(10, searchField, searchBtn, backBtn);
        topBox.setPadding(new Insets(10));

        VBox vbox = new VBox(10, title, topBox, table);
        vbox.setPadding(new Insets(15));

        root.setCenter(vbox);

        stage.setScene(new Scene(root, 700, 450));
        stage.setTitle("Search Books");
        stage.show();
    }
}
