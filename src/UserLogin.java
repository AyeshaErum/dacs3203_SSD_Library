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

    public static boolean isValidUsername(String username) {
        return username.matches("^[a-z-_]{2,10}$");
    }

    public static boolean isValidPassword(String password) {
        // at least 8 chars, 1 letter, 1 digit
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static boolean isValidQatarPhone(String phone) {
        return phone.matches("^(\\+974|00974|\\(974\\))\\d{8}$");
    }


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

        String sql = "SELECT password, salt, role FROM users WHERE username = ?";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String storedSalt = rs.getString("salt");
                String role = rs.getString("role");

                String enteredHash = SecurityUtils.hashPassword(password, storedSalt);

                if (enteredHash.equals(storedHash)) {

                    // Login success → go to dashboard
                    if (role.equals("admin")) {
                        AdminDashboard admin = new AdminDashboard(stage, username);
                        admin.show();
                    } else {
                        UserDashboard user = new UserDashboard(stage, username);
                        user.show();
                    }

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
            String r = "user";   // default user role
            String fn = newFirst.getText().trim();
            String ln = newLast.getText().trim();
            String em = newEmail.getText().trim();
            String ph = newPhone.getText().trim();

            // 1) Required fields
            if (u.isEmpty() || p.isEmpty() || fn.isEmpty() || ln.isEmpty() || em.isEmpty() || ph.isEmpty()) {
                status.setText("All fields are required!");
                return;
            }

            // 2) Username validation
            if (!isValidUsername(u)) {
                status.setText("Invalid username! Use 2–10 lowercase letters or - or _.");
                return;
            }

            // 3) Password validation
            if (!isValidPassword(p)) {
                status.setText("Password must be 8+ characters with letters AND numbers.");
                return;
            }

            // 4) Email validation
            if (!isValidEmail(em)) {
                status.setText("Invalid email format.");
                return;
            }

            // 5) Qatar phone number validation
            if (!isValidQatarPhone(ph)) {
                status.setText("Phone must be Qatar format: +974 / 00974 / (974) + 8 digits.");
                return;
            }

            // 6) Hash + insert into DB
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
                pstmt.setString(7, em);
                pstmt.setString(8, ph);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    loginMessage.setText("User successfully registered!");
                    stage.setScene(loginScene);
                } else {
                    status.setText("Failed to create account.");
                }

            } catch (java.sql.SQLIntegrityConstraintViolationException exDup) {
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
