import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserDashboard {
    private Stage stage;
    private String username;

    public UserDashboard(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void show() {
        BorderPane root = new BorderPane();

        Label title = new Label("User Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button searchBooks = new Button("Search Books");
        Button borrowBooks = new Button("Borrow / Return Books");
        Button logout = new Button("Logout");

        // Logout → go back to Login screen
        logout.setOnAction(e -> {
            UserLogin login = new UserLogin(stage);
            login.initializeComponents();
        });

        // Search Books page
        searchBooks.setOnAction(e -> {
            SearchBooksPage sb = new SearchBooksPage(stage);
            sb.show();
        });

        VBox vbox = new VBox(15, title, searchBooks, borrowBooks, logout);
        vbox.setPadding(new Insets(20));

        root.setCenter(vbox);

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Library - User Dashboard");
        stage.show();
    }
}
