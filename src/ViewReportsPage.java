import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewReportsPage {

    private Stage stage;

    public ViewReportsPage(Stage stage) {
        this.stage = stage;
    }

    public void show() {

        TableView<LoginLog> table = new TableView<>();

        TableColumn<LoginLog, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<LoginLog, String> colTime = new TableColumn<>("Login Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("loginTime"));

        table.getColumns().addAll(colUser, colTime);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setItems(fetchLogs());

        Button back = new Button("Back to Dashboard");
        back.setOnAction(e -> new AdminDashboard(stage, "admin").show());

        VBox layout = new VBox(15, new Label("Login Logs Report"), table, back);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 600, 400));
        stage.show();
    }

    private ObservableList<LoginLog> fetchLogs() {
        ObservableList<LoginLog> list = FXCollections.observableArrayList();

        String sql = "SELECT username, login_time FROM login_logs ORDER BY login_time DESC";

        try (Connection conn = DBUtils.establishConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(new LoginLog(
                        rs.getString("username"),
                        rs.getString("login_time")
                ));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public static class LoginLog {
        private String username;
        private String loginTime;

        public LoginLog(String username, String loginTime) {
            this.username = username;
            this.loginTime = loginTime;
        }

        public String getUsername() {
            return username;
        }

        public String getLoginTime() {
            return loginTime;
        }
    }
}
