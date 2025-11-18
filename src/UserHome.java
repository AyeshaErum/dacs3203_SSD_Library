import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserHome {

    private Stage stage;
    private Scene homeScene;
    private String username;

    public UserHome(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void initializeComponents() {

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label welcome = new Label("Welcome, " + username + "!");
        welcome.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Button changePasswordBtn = new Button("Change Password");
        Button logoutBtn = new Button("Logout");

        changePasswordBtn.setOnAction(e -> {
            UserChangePassword cp = new UserChangePassword(stage, username);
            cp.initializeComponents();
        });

        logoutBtn.setOnAction(e -> {
            UserLogin loginPage = new UserLogin(stage);
            loginPage.initializeComponents();
        });

        layout.getChildren().addAll(
                welcome,
                changePasswordBtn,
                logoutBtn
        );

        homeScene = new Scene(layout, 350, 200);
        stage.setScene(homeScene);
        stage.setTitle("User Home");
        stage.show();
    }
}
