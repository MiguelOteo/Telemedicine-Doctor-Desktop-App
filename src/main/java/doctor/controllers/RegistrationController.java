package doctor.controllers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;

import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.params.DoctorParams;
import doctor.utility.RegexValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import launch.LaunchApp;

public class RegistrationController implements Initializable {

	@FXML
	private Pane registrationPane;
	@FXML
	private JFXTextField userNameField;
	@FXML
	private JFXTextField userEmailField;
	@FXML
	private JFXPasswordField userPasswordField;
	@FXML
	private JFXPasswordField userRepeatPasswordField;
	@FXML
	private JFXButton registerButton;
	@FXML
	private JFXButton goBackButton;

	// Default constructor
	public RegistrationController() {}

	public void initialize(URL location, ResourceBundle resources) {
		
		RegexValidator validator = new RegexValidator();
		validator.setRegexPattern("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		validator.setMessage("Email is not valid");
		userEmailField.getValidators().add(validator);
		userEmailField.focusedProperty().addListener((o, oldVal, newVal) ->{
			if(!newVal) {
				userEmailField.validate();
			}
		});
		
		RequiredFieldValidator validatorEmpty = new RequiredFieldValidator();
		validatorEmpty.setMessage("Password cannot be empty");
		userPasswordField.getValidators().add(validatorEmpty);
		userPasswordField.focusedProperty().addListener((o, oldVal, newVal) ->{
			if(!newVal) {
				userPasswordField.validate();
			}
		});
		
		RequiredFieldValidator validatorEmpty2 = new RequiredFieldValidator();
		validatorEmpty2.setMessage("Repeat password cannot be empty");
		userRepeatPasswordField.getValidators().add(validatorEmpty2);
		userRepeatPasswordField.focusedProperty().addListener((o, oldVal, newVal) ->{
			if(!newVal) {
				userRepeatPasswordField.validate();
			}
		});
		
		RequiredFieldValidator validatorEmpty3 = new RequiredFieldValidator(); 
		validatorEmpty3.setMessage("User name cannot be empty");
		userNameField.getValidators().add(validatorEmpty3);
		userNameField.focusedProperty().addListener((o, oldVal, newVal) ->{
			if(!newVal) {
				userNameField.validate();
			}
		});

		registerButton.setOnAction((ActionEvent event) -> {
			boolean resultEmail = userEmailField.validate();
			boolean resultPass = userPasswordField.validate();
			boolean resultPassRep = userRepeatPasswordField.validate();
			boolean resultName = userNameField.validate();
			doRequest(resultEmail, resultName, resultPass, resultPassRep);
		});
	}

	public void doRequest(boolean resultEmail, boolean resultName, boolean resultPass, boolean resultPassRep) {
		
		if(resultName == true && resultEmail == true && resultPass == true && resultPassRep == true) {
			resgisterUserRest(userNameField.getText(), userEmailField.getText(), userPasswordField.getText(), 
					userRepeatPasswordField.getText());
			registerButton.setDisable(true);
		}
	}
	
	// When press it goes back to the logIn pane
	@FXML
	private void backToMenu(MouseEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/doctor/view/LogInLayout.fxml"));
		LaunchApp.getStage().getScene().setRoot(root);
	}

	@FXML
	private void closeApp(MouseEvent event) {
		System.exit(0);
	}

	@FXML
	private void minWindow(MouseEvent event) {
		Stage stage = (Stage) registrationPane.getScene().getWindow();
		stage.setIconified(true);
	}

	private void openErrorDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/doctor/view/DialogPopUpLayout.fxml"));
			Parent root = (Parent) loader.load();
			DialogPopUpController controler = loader.getController();
			controler.setMessage(message);
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			registrationPane.getParent().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				registrationPane.getParent().setEffect(null);
				registerButton.setDisable(false);
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	private void openAccountCreatedDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/doctor/view/DialogPopUpLayout.fxml"));
			Parent root = (Parent) loader.load();
			DialogPopUpController controler = loader.getController();
			controler.setMessage(message);
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			registrationPane.getParent().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				registrationPane.getParent().setEffect(null);
				registerButton.setDisable(false);
				goBack();
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void goBack() {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/doctor/view/LogInLayout.fxml"));
			LaunchApp.getStage().getScene().setRoot(root);
			registerButton.setDisable(false);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void resgisterUserRest(String userName, String userEmail, String userPassword,
			String userPasswordRepeat) {

		Thread threadObject = new Thread("RegisteringUser") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/doctorRegistration")
							.openConnection();
					connection.setRequestMethod("POST");

					APIRequest requestAPI = new APIRequest();
					if(!userName.equals("")) {requestAPI.setUserName(userName);}
					if(!userEmail.equals("")) {requestAPI.setUserEmail(userEmail);}
					if(!userPassword.equals("")) {requestAPI.setUserPassword(userPassword);}
					if(!userPasswordRepeat.equals("")) {requestAPI.setUserPasswordRepeat(userPasswordRepeat);}
					String postData = "APIRequest=" + URLEncoder.encode(new Gson().toJson(requestAPI), "UTF-8");
					
					connection.setDoOutput(true);
					OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
					writer.write(postData);
					writer.flush();

					BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = inputReader.readLine()) != null) {
						response.append(inputLine);
					}
					inputReader.close();
					APIResponse responseAPI = new Gson().fromJson(response.toString(), APIResponse.class);

					if (responseAPI.isError()) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								openErrorDialog(responseAPI.getAPImessage());
								userPasswordField.setText("");
								userRepeatPasswordField.setText("");
							}
						});
					} else {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								openAccountCreatedDialog(responseAPI.getAPImessage());
							}
						});
					}
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							openErrorDialog("Failed to connect to the server");
						}
					});
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
	}
}
