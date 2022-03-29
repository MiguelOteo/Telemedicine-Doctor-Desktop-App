package doctor.treeobjects;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import doctor.communication.AccountObjectCommunication;
import doctor.controllers.DialogPopUpController;
import doctor.controllers.LogInController;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.params.DoctorParams;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PatientTreeObject extends RecursiveTreeObject<PatientTreeObject> {
	
	private final Pane mainPane;
	private final int patientId;
	
	private final StringProperty patientName;
	private final StringProperty patientEmail;
	private final StringProperty patientIdNumber;
	private final ObjectProperty<JFXButton> showBITalinoRecords;
	private final ObjectProperty<JFXButton> unsignedPatient;

	public PatientTreeObject(int patientId, String patientName, String patientEmail, String patientIdNumber, Pane pane) {
		
		this.mainPane = pane;
		this.patientId = patientId;
		
		this.patientName = new SimpleStringProperty(patientName);
		this.patientEmail = new SimpleStringProperty(patientEmail);
		this.patientIdNumber = new SimpleStringProperty(patientIdNumber);
		
		JFXButton showDetails = new JFXButton("Show details");
		showDetails.getStyleClass().add("tree_table_button");
		showDetails.setOnAction((ActionEvent event) -> openPatientRecords());
		
		JFXButton deleteAssignment = new JFXButton("Delete assignment");
		deleteAssignment.getStyleClass().add("tree_table_button");
		deleteAssignment.setOnAction((ActionEvent event) -> deletePatient());
		
		this.showBITalinoRecords = new SimpleObjectProperty<>(showDetails);
		this.unsignedPatient = new SimpleObjectProperty<>(deleteAssignment);
	}
	
	private void openPatientRecords() {
		Pane patientRecordsPane;	
		try {
			AccountObjectCommunication.setDatabaseId(patientId);
			patientRecordsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(DoctorParams.PATIENT_RECORDS_VIEW)));
			mainPane.getChildren().removeAll();
			mainPane.getChildren().setAll(patientRecordsPane);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	private void deletePatient() {
		unsignedPatient.getValue().setDisable(true);
		showBITalinoRecords.getValue().setDisable(true);
		Thread threadObject = new Thread("deletingPatient") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/deletePatientAssignment")
							.openConnection();
					
					connection.setRequestMethod("POST");
					
					Gson gsonConverter = new Gson();
					APIRequest requestAPI = new APIRequest();
					requestAPI.setPatientId(patientId);
					
					String postData = "APIRequest=" + URLEncoder.encode(gsonConverter.toJson(requestAPI), StandardCharsets.UTF_8);
					
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

					APIResponse responseAPI = gsonConverter.fromJson(response.toString(), APIResponse.class);
					
					Platform.runLater(() -> openDialog(responseAPI.getAPImessage(), responseAPI.isError()));
					
				} catch (ConnectException | FileNotFoundException connectionError) {
					Platform.runLater(() -> openDialog("Failed to connect to the server", true));
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
	}
	
	// Displays any error returned form the Rest API
	private void openDialog(String message, boolean error) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.DIALOG_POP_UP_VIEW));
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
			stage.getIcons().add(new Image(DoctorParams.APP_ICON));
			
			// Set the pop-up in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				AccountObjectCommunication.getAnchorPane().setEffect(null);
				if(!error) {
					Pane doctorPatientsPane;
					try {
						doctorPatientsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(DoctorParams.DOCTOR_PATIENTS_VIEW)));
						mainPane.getChildren().removeAll();
						mainPane.getChildren().setAll(doctorPatientsPane);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
			});
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	// Getters methods
	public int getPatientId() {return patientId;}

	public StringProperty getPatientName() {return patientName;}

	public StringProperty getPatientEmail() {return patientEmail;}

	public StringProperty getPatientIdNumber() {return patientIdNumber;}

	public ObjectProperty<JFXButton> getShowBITalinoRecords() {return showBITalinoRecords;}

	public ObjectProperty<JFXButton> getUnsignedPatient() {return unsignedPatient;}
}