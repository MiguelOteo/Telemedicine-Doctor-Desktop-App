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

import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.params.DoctorParams;
import doctor.utility.RegexValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class LogInController implements Initializable {

	private double xOffset = 0, yOffset = 0;
	
	// JavaFx layout elements
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private Pane logInPane;
	@FXML
	private JFXButton logInButton;
	@FXML
	private JFXButton signUpButton;
	@FXML
	private JFXTextField userEmailField;
	@FXML
	private JFXPasswordField userPasswordField;

	// Default constructor
	public LogInController() {}

	// Initialize method
	@Override
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
		
		// Initialize the logIn button on press event
		logInButton.setOnAction((ActionEvent event) -> {
			boolean resultEmail = userEmailField.validate();
			boolean resultPass = userPasswordField.validate();
			doRequest(resultEmail, resultPass);
		});
	}
	
	public void doRequest(boolean resultEmail, boolean resultPass) {
		
		if(resultEmail == true && resultPass == true) {
			loginUserRest(userEmailField.getText(), userPasswordField.getText());
			logInButton.setDisable(true);
		}
	}

	// Replace the login pane with the registration one
	@FXML
	private void openRegistration(MouseEvent event) throws IOException {
		Pane registrationPane = FXMLLoader.load(getClass().getResource(DoctorParams.REGISTRATION_VIEW));
		anchorPane.getChildren().remove(logInPane);
		anchorPane.getChildren().setAll(registrationPane);
	}

	@FXML
	private void closeApp(MouseEvent event) throws IOException {
		System.exit(0);
	}

	@FXML
	private void minWindow(MouseEvent event) {
		Stage stage = (Stage) logInPane.getScene().getWindow();
		stage.setIconified(true);
	}

	private void launchMenu(String fileName) {
		try {
			LaunchApp.getStage().hide();
			userEmailField.setText("");
			userPasswordField.setText("");
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName + ".fxml"));
			Parent root = (Parent) loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setScene(scene);
			
			// In case the user wants to move the app along the screen
			moveWindow(root, stage);
			
			stage.show();
			stage.setOnHiding(event -> {
				logInButton.setDisable(false);
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void openDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.DIALOG_POP_UP_VIEW));
			Parent root = (Parent) loader.load();
			DialogPopUpController controler = loader.getController();
			controler.setMessage(message);
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			anchorPane.setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				anchorPane.setEffect(null);
				logInButton.setDisable(false);
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void loginUserRest(String userEmail, String userPassword) {

		Thread threadObject = new Thread("AthentificatingUser") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/doctorLogIn")
							.openConnection();

					connection.setRequestMethod("POST");
					
					APIRequest requestAPI = new APIRequest();
					if(!userEmail.equals("")) {requestAPI.setUserEmail(userEmail);}
					if(!userPassword.equals("")) {requestAPI.setUserPassword(userPassword);}
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

					Gson gsonConverter = new Gson();
					APIResponse responseAPI = gsonConverter.fromJson(response.toString(), APIResponse.class);

					if (responseAPI.isError()) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								openDialog(responseAPI.getAPImessage());
							}
						});
					} else {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								AccountObjectCommunication.setDoctor(responseAPI.getDoctor());
								launchMenu("/doctor/view/DoctorMenuLayout");
							}
						});
					}
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							conncetionError.printStackTrace();
							openDialog("Failed to connect to the server");
						}
					});
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
	}
	
	public void moveWindow(Parent root, Stage stage) {
		
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setOpacity(0.8);
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});
		
		root.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setOpacity(1);
			}
		});
	}
}
