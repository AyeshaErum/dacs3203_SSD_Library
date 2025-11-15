import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserChangePassword {
    private Scene changePasswordScene;
    private PasswordField newPasswordField = new PasswordField();
    private Stage stage;
    private String username;

    public UserChangePassword(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(e -> changePassword());

        layout.getChildren().addAll(
                new Label("Welcome " + username),
                new Label("New Password:"), newPasswordField,
                changePasswordButton
        );

        changePasswordScene = new Scene(layout, 300, 200);
        stage.setTitle("Change Password");
        stage.setScene(changePasswordScene);
        stage.show();
    }

    // Update password with new hash + salt
    private void changePassword() {
        String newPassword = newPasswordField.getText().trim();
        if (newPassword.isEmpty()) {
            showAlert("Error", "Please enter a new password.");
            return;
        }

        String newSalt = SecurityUtils.generateSalt();
        String newHashedPassword = SecurityUtils.hashPassword(newPassword, newSalt);

        String sql = "UPDATE users SET password = ?, salt = ? WHERE username = ?";

        try (Connection con = DBUtils.establishConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, newSalt);
            pstmt.setString(3, username);

            int result = pstmt.executeUpdate();
            if (result == 1) {
                showAlert("Success", "Password successfully changed!");
            } else {
                showAlert("Failure", "Failed to update password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect to the database.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
