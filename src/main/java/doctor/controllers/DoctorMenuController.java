package doctor.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import doctor.communication.AccountObjectCommunication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import launch.LaunchApp;

public class DoctorMenuController implements Initializable {

	@FXML
	private AnchorPane menuWindow;
	@FXML
	private Pane menuMainPane;
	@FXML
	private JFXButton logOutButton;
	
	// Menu buttons
	@FXML
	private JFXButton doctorPatients;
	@FXML
	private JFXButton doctorAccount;

	public DoctorMenuController() {}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		AccountObjectCommunication.setAnchorPane(menuWindow);
		
		if(AccountObjectCommunication.getDoctor().getDoctorIdNumber() == null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					opendIdPopUp();
				}	
			});
		} else {
			openDoctorAccount();
		}
	}

	@FXML
	private void closeApp(MouseEvent event) {
		System.exit(0);
	}

	@FXML
	private void logOut(MouseEvent event) {
		Stage stage = (Stage) logOutButton.getScene().getWindow();
		stage.close();
		AccountObjectCommunication.setDoctor(null);
		LaunchApp.getStage().show();
	}

	@FXML
	private void minWindow(MouseEvent event) {
		Stage stage = (Stage) menuWindow.getScene().getWindow();
		stage.setIconified(true);
	}
	
	@FXML
	private void openDoctorPatients() {
		Pane doctorPatientsPane;
		try {
			doctorPatientsPane = FXMLLoader.load(getClass().getResource("/doctor/view/DoctorPatientsLayout.fxml"));
			menuMainPane.getChildren().removeAll();
			menuMainPane.getChildren().setAll(doctorPatientsPane);
			doctorPatients.setDisable(true);
			doctorAccount.setDisable(false);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	@FXML
	private void openDoctorAccount() {
		Pane doctorAccountPane;
		try {
			doctorAccountPane = FXMLLoader.load(getClass().getResource("/doctor/view/DoctorAccountLayout.fxml"));
			menuMainPane.getChildren().removeAll();
			menuMainPane.getChildren().setAll(doctorAccountPane);
			doctorPatients.setDisable(false);
			doctorAccount.setDisable(true);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	

	private void opendIdPopUp() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/doctor/view/InsertIdLayout.fxml"));
			Parent root = (Parent) loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			menuWindow.setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				menuWindow.setEffect(null);
				openDoctorAccount();
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
}





