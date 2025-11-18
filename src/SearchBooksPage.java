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
        searchField.setPromptText("Enter title, author, or ISBN");

        Button searchBtn = new Button("Search");
        Button backBtn = new Button("Back");

        TableView<BookTable> table = new TableView<>();
        TableColumn<BookTable, String> col1 = new TableColumn<>("Title");
        col1.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<BookTable, String> col2 = new TableColumn<>("Author");
        col2.setCellValueFactory(c -> c.getValue().authorProperty());

        TableColumn<BookTable, String> col3 = new TableColumn<>("ISBN");
        col3.setCellValueFactory(c -> c.getValue().isbnProperty());

        TableColumn<BookTable, String> col4 = new TableColumn<>("Available");
        col4.setCellValueFactory(c -> c.getValue().availableProperty());

        table.getColumns().addAll(col1, col2, col3, col4);

        // SEARCH ACTION
        searchBtn.setOnAction(e -> {
            String keyword = "%" + searchField.getText().trim() + "%";

            table.getItems().clear();

            String sql = "SELECT title, author, isbn, isAvailable FROM books " +
                    "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";

            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, keyword);
                pstmt.setString(2, keyword);
                pstmt.setString(3, keyword);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    table.getItems().add(
                            new BookTable(
                                    rs.getString("title"),
                                    rs.getString("author"),
                                    rs.getString("isbn"),
                                    rs.getBoolean("isAvailable") ? "Yes" : "No"
                            )
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // BACK BUTTON (Returns to user dashboard)
        backBtn.setOnAction(e -> {
            UserDashboard user = new UserDashboard(stage, "User");
            user.show();
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
