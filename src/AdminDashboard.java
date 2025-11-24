import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AdminDashboard {
    private Stage stage;
    private String username;


    public AdminDashboard(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }


    public void show() {
        BorderPane root = new BorderPane();


        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");


        Button manageUsers = new Button("Manage Users");
        Button manageBooks = new Button("Manage Books");
        Button viewReports = new Button("View Reports");
        Button logout = new Button("Logout");


        logout.setOnAction(e -> {
            UserLogin login = new UserLogin(stage);
            login.initializeComponents();
        });


        VBox vbox = new VBox(15, title, manageUsers, manageBooks, viewReports, logout);
        vbox.setPadding(new Insets(20));


        root.setCenter(vbox);


        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Library - Admin Dashboard");
        manageBooks.setOnAction(e -> {
            ManageBooksPage mb = new ManageBooksPage(stage);
            mb.show();
        });

        manageUsers.setOnAction(e -> {
            new ManageUsersPage(stage).show();
        });

        stage.show();
    }
}