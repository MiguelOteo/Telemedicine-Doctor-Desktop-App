package doctor.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogPopUpController implements Initializable {

	@FXML
	private JFXButton closeDialog;
	@FXML
	private Label message;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {}

	@FXML
	private void close_dialog() {
		Stage stage = (Stage) closeDialog.getScene().getWindow();
		stage.close();
	}
	
	public void setMessage(String message) {
		this.message.setText(message);
	}
}
