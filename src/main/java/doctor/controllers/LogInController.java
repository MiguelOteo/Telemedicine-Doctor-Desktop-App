package doctor.controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.utility.RegexValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

import static doctor.params.DoctorParams.*;

public class LogInController implements Initializable {

	private static Stage mainMenu;
	
	private double xOffset = 0, yOffset = 0;
	
	// JavaFx layout elements
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private Pane logInPane;
	@FXML
	private JFXButton logInButton;
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
		
		if(resultEmail && resultPass) {
			loginUserRest(userEmailField.getText(), userPasswordField.getText());
			logInButton.setDisable(true);
		}
	}

	// Replace the login pane with the registration one
	@FXML
	private void openRegistration() throws IOException {
		Pane registrationPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(REGISTRATION_VIEW)));
		anchorPane.getChildren().remove(logInPane);
		anchorPane.getChildren().setAll(registrationPane);
	}

	@FXML
	private void closeApp() {
		System.exit(0);
	}

	@FXML
	private void minWindow() {
		Stage stage = (Stage) logInPane.getScene().getWindow();
		stage.setIconified(true);
	}

	public static Stage getStage() {
		return mainMenu;
	}
	
	private void launchMenu() {
		try {
			LaunchApp.getStage().hide();
			userEmailField.setText("");
			userPasswordField.setText("");
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DOCTOR_MENU_VIEW));
			Parent root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setScene(scene);
			stage.setTitle("Telelepsia Hospitals Menu");
			stage.getIcons().add(new Image(APP_ICON));
			
			 mainMenu = stage;
			
			// In case the user wants to move the app along the screen
			moveWindow(root,  mainMenu);
			
			mainMenu.show();
			mainMenu.setOnHiding(event -> logInButton.setDisable(false));
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void openDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DIALOG_POP_UP_VIEW));
			Parent root = loader.load();
			DialogPopUpController controller = loader.getController();
			controller.setMessage(message);
			Stage stage = new Stage();
			stage.setHeight(130);
			stage.setWidth(300);
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Telelepsia Message");
			stage.getIcons().add(new Image(APP_ICON));
			
			// Set the pop-up in the center of the login window
			stage.setX(LaunchApp.getStage().getX() + LaunchApp.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-50 + LaunchApp.getStage().getY() + LaunchApp.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
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

		Thread threadObject = new Thread("AuthenticatingUser") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/doctorLogIn")
							.openConnection();

					connection.setRequestMethod("POST");
					
					APIRequest requestAPI = new APIRequest();
					if(!userEmail.equals("")) {requestAPI.setUserEmail(userEmail);}
					if(!userPassword.equals("")) {requestAPI.setUserPassword(userPassword);}
					String postData = "APIRequest=" + URLEncoder.encode(new Gson().toJson(requestAPI), StandardCharsets.UTF_8);
					
					connection.setDoOutput(true);
					OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
					writer.write(postData);
					writer.flush();

					BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = inputReader.readLine()) != null) {
						response.append(inputLine);
					}
					inputReader.close();

					Gson gsonConverter = new Gson();
					APIResponse responseAPI = gsonConverter.fromJson(response.toString(), APIResponse.class);

					if (responseAPI.isError()) {
						Platform.runLater(() -> openDialog(responseAPI.getAPImessage()));
					} else {
						Platform.runLater(() -> {
							AccountObjectCommunication.setDoctor(responseAPI.getDoctor());
							launchMenu();
						});
					}
				} catch (ConnectException | FileNotFoundException connectionError) {
					Platform.runLater(() -> {
						connectionError.printStackTrace();
						openDialog("Failed to connect to the server");
					});
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
	}
	
	public void moveWindow(Parent root, Stage stage) {
		
		root.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		
		root.setOnMouseDragged(event -> {
			stage.setOpacity(0.8);
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});
		
		root.setOnMouseReleased(event -> stage.setOpacity(1));
	}
}
