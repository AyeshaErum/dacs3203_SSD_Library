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
    private PasswordField confirmPasswordField = new PasswordField();
    private Stage stage;
    private String username;

    public UserChangePassword(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(12));

        Label welcome = new Label("Change Password for: " + username);
        welcome.setStyle("-fx-font-size: 16px; -fx-font-weight:bold;");

        newPasswordField.setPromptText("Enter new password");
        confirmPasswordField.setPromptText("Confirm new password");

        Button updateButton = new Button("Update Password");
        Button backButton = new Button("Back to Dashboard");

        updateButton.setOnAction(e -> changePassword());
        backButton.setOnAction(e -> goBackToDashboard());

        layout.getChildren().addAll(
                welcome,
                new Label("New Password:"), newPasswordField,
                new Label("Confirm Password:"), confirmPasswordField,
                updateButton,
                backButton
        );

        changePasswordScene = new Scene(layout, 350, 260);
        stage.setTitle("Change Password");
        stage.setScene(changePasswordScene);
        stage.show();
    }

    private void changePassword() {
        String pass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        // 1. Check empty
        if (pass.isEmpty() || confirm.isEmpty()) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        // 2. Check match
        if (!pass.equals(confirm)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        // 3. Check password rules (same as signup)
        if (!UserLogin.isValidPassword(pass)) {
            showAlert("Error",
                    "Password must be 8+ characters and include BOTH letters and numbers.");
            return;
        }

        // 4. Generate new salt + hashed password
        String newSalt = SecurityUtils.generateSalt();
        String newHashed = SecurityUtils.hashPassword(pass, newSalt);

        String sql = "UPDATE users SET password = ?, salt = ? WHERE username = ?";

        try (Connection con = DBUtils.establishConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, newHashed);
            pst.setString(2, newSalt);
            pst.setString(3, username);

            int result = pst.executeUpdate();

            if (result == 1) {
                showAlert("Success", "Password changed successfully!");
                goBackToDashboard();
            } else {
                showAlert("Error", "Could not update password.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Failed to update password in database.");
        }
    }

    private void goBackToDashboard() {
        UserDashboard ud = new UserDashboard(stage, username);
        ud.show();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}