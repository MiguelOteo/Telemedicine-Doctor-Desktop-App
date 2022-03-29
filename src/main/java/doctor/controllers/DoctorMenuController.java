package doctor.controllers;

import com.jfoenix.controls.JFXButton;
import doctor.communication.AccountObjectCommunication;
import doctor.params.DoctorParams;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import launch.LaunchApp;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

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
	@FXML
	private JFXButton doctorMessenger;

	public DoctorMenuController() {}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		AccountObjectCommunication.setAnchorPane(menuWindow);
		
		if(AccountObjectCommunication.getDoctor().getDoctorIdNumber() == null) {
			Platform.runLater(this::openIdPopUp);
		} else {
			openDoctorAccount();
		}
	}

	@FXML
	private void logOut() {
		Stage stage = (Stage) logOutButton.getScene().getWindow();
		stage.close();
		AccountObjectCommunication.setDoctor(null);
		LaunchApp.getStage().show();
	}

	@FXML
	private void openDoctorPatients() {
		Pane doctorPatientsPane;
		try {
			doctorPatientsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(DoctorParams.DOCTOR_PATIENTS_VIEW)));
			menuMainPane.getChildren().removeAll();
			menuMainPane.getChildren().setAll(doctorPatientsPane);
			doctorPatients.setDisable(true);
			doctorAccount.setDisable(false);
			doctorMessenger.setDisable(false);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	@FXML
	private void openDoctorAccount() {
		Pane doctorAccountPane;
		try {
			doctorAccountPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(DoctorParams.DOCTOR_ACCOUNT_VIEW)));
			menuMainPane.getChildren().removeAll();
			menuMainPane.getChildren().setAll(doctorAccountPane);
			doctorPatients.setDisable(false);
			doctorAccount.setDisable(true);
			doctorMessenger.setDisable(false);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	@FXML
	private void openDoctorMessenger() {
		Pane doctorMessengerPane;
		try {
			doctorMessengerPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(DoctorParams.DOCTOR_MESSENGER_VIEW)));
			menuMainPane.getChildren().removeAll();
			menuMainPane.getChildren().setAll(doctorMessengerPane);
			doctorPatients.setDisable(false);
			doctorAccount.setDisable(false);
			doctorMessenger.setDisable(true);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}	

	private void openIdPopUp() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.INSERT_ID_VIEW));
			Parent root = loader.load();
			Stage stage = new Stage();
			stage.setHeight(160);
			stage.setWidth(310);
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Insert ID Number");
			stage.getIcons().add(new Image(DoctorParams.APP_ICON));
			
			// Set the pop-up in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
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





