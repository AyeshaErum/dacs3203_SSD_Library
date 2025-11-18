import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserLogin {

    private Stage stage;
    private Scene loginScene;
    private Scene signUpScene;

    // Login controls
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Label loginMessage = new Label();

    public UserLogin() {}

    public UserLogin(Stage stage) {
        this.stage = stage;
    }

    public void initializeComponents() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        usernameField.setPromptText("username");
        passwordField.setPromptText("password");

        Button loginBtn = new Button("Sign In");
        loginBtn.setOnAction(e -> authenticate());

        HBox orBox = new HBox(8);
        Label orLabel = new Label("or");
        Button signUpBtn = new Button("Sign up");
        signUpBtn.setOnAction(e -> showSignUpScene());
        orBox.getChildren().addAll(orLabel, signUpBtn);

        root.getChildren().addAll(new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                loginBtn, orBox, loginMessage);

        loginScene = new Scene(root, 360, 260);
        stage.setTitle("User Login");
        stage.setScene(loginScene);
        stage.show();
    }

    // Authenticate user (salted hash)
    private void authenticate() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessage.setText("Please enter username and password.");
            return;
        }

        String sql = "SELECT password, salt FROM users WHERE username = ?";
        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String storedSalt = rs.getString("salt");

                String enteredHash = SecurityUtils.hashPassword(password, storedSalt);

                if (enteredHash.equals(storedHash)) {
                    loginMessage.setText("Login successful!");
                    UserChangePassword cp = new UserChangePassword(stage, username);
                    cp.initializeComponents();
                } else {
                    loginMessage.setText("Invalid username or password.");
                }
            } else {
                loginMessage.setText("Invalid username or password.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            loginMessage.setText("Database error. Check console.");
        }
    }

    // Sign-up scene
    private void showSignUpScene() {
        TextField newUsername = new TextField();
        PasswordField newPassword = new PasswordField();
       // TextField newRole = new TextField();
        TextField newFirst = new TextField();
        TextField newLast = new TextField();
        TextField newEmail = new TextField();     // new email field
        TextField newPhone = new TextField();     // new phone field
        Label status = new Label();

        newUsername.setPromptText("username");
        newPassword.setPromptText("password");
        // newRole.setPromptText("role");
        newFirst.setPromptText("first name");
        newLast.setPromptText("last name");
        newEmail.setPromptText("email");
        newPhone.setPromptText("phone number");

        Button createBtn = new Button("Create Account");
        Button backToLogin = new Button("Back to Login");


        createBtn.setOnAction(e -> {
            String u = newUsername.getText().trim();
            String p = newPassword.getText();
            //String r = newRole.getText().trim();
            String r = "user";   // automatically assign normal user role
            String fn = newFirst.getText().trim();
            String ln = newLast.getText().trim();
            String em = newEmail.getText().trim();       // <-- new email field
            String ph = newPhone.getText().trim();       // <-- new phone field

            if (u.isEmpty() || p.isEmpty()) {
                status.setText("Username and password required.");
                return;
            }

            String salt = SecurityUtils.generateSalt();
            String hashedPassword = SecurityUtils.hashPassword(p, salt);

            String sql = "INSERT INTO users (username, password, salt, role, firstname, lastname, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBUtils.establishConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, u);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, salt);
                pstmt.setString(4, r);
                pstmt.setString(5, fn);
                pstmt.setString(6, ln);
                pstmt.setString(7, em); // new email
                pstmt.setString(8, ph); // new phone

                // ✅ Here is where you show registration success
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // Show success message in login scene instead of sign-up scene
                    loginMessage.setText("User successfully registered!");
                    stage.setScene(loginScene); // redirect to login
                } else {
                    status.setText("Failed to create user.");
                }


            } catch (java.sql.SQLIntegrityConstraintViolationException e2) {
                status.setText("Username already exists.");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Database error. Check console.");
            }
        });






        backToLogin.setOnAction(e -> stage.setScene(loginScene));

        VBox layout = new VBox(8,
                new Label("Create new user"),
                new Label("Username:"), newUsername,
                new Label("Password:"), newPassword,
                //new Label("Role:"), newRole,
                new Label("First Name:"), newFirst,
                new Label("Last Name:"), newLast,
                new Label("Email:"), newEmail,
                new Label("Phone:"), newPhone,
                createBtn, backToLogin, status
        );
        layout.setPadding(new Insets(12));

        signUpScene = new Scene(layout, 400, 500);
        stage.setScene(signUpScene);
        stage.setTitle("Sign Up");
        stage.show();
    }
}
