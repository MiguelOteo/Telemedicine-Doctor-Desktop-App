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

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import doctor.communication.AccountObjectCommunication;
import doctor.controllers.DialogPopUpController;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PatientTreeObject extends RecursiveTreeObject<PatientTreeObject> {
	
	private Pane mainPane;
	private int patientId;
	
	private StringProperty patientName;
	private StringProperty patientEmail;
	private StringProperty patientIdNumber;
	private ObjectProperty<JFXButton> showBITalinoRecords;
	private ObjectProperty<JFXButton> unsignPatient;

	public PatientTreeObject(int patientId, String patientName, String patientEmail, String patientIdNumber, Pane pane) {
		
		this.mainPane = pane;
		this.patientId = patientId;
		
		this.patientName = new SimpleStringProperty(patientName);
		this.patientEmail = new SimpleStringProperty(patientEmail);
		this.patientIdNumber = new SimpleStringProperty(patientIdNumber);
		
		JFXButton showDetails = new JFXButton("Show details");
		showDetails.getStyleClass().add("tree_table_button");
		showDetails.setOnAction((ActionEvent event) -> {
			openPatientRecords();
		});
		
		JFXButton deleteAsingment = new JFXButton("Delete assignment"); 
		deleteAsingment.getStyleClass().add("tree_table_button");
		deleteAsingment.setOnAction((ActionEvent event) -> {
			deletePatient();
		});
		
		this.showBITalinoRecords = new SimpleObjectProperty<JFXButton>(showDetails);	
		this.unsignPatient = new SimpleObjectProperty<JFXButton>(deleteAsingment);
	}
	
	private void openPatientRecords() {
		Pane patientRecordsPane;	
		try {
			AccountObjectCommunication.setDatabaseId(patientId);
			patientRecordsPane = FXMLLoader.load(getClass().getResource("/doctor/view/PatientRecordsLayout.fxml"));
			mainPane.getChildren().removeAll();
			mainPane.getChildren().setAll(patientRecordsPane);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	private void deletePatient() {
		unsignPatient.getValue().setDisable(true);
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
					
					String postData = "APIRequest=" + URLEncoder.encode(gsonConverter.toJson(requestAPI), "UTF-8");
					
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

					APIResponse responseAPI = gsonConverter.fromJson(response.toString(), APIResponse.class);
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {	
							openDialog(responseAPI.getAPImessage(), responseAPI.isError());
						}
					});
					
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							openDialog("Failed to connect to the server", true);
						}
					});
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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/common/view/dialogPopUpLayout.fxml"));
			Parent root = (Parent) loader.load();
			DialogPopUpController controler = loader.getController();
			controler.setMessage(message);
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				AccountObjectCommunication.getAnchorPane().setEffect(null);
				if(!error) {
					Pane doctorPatientsPane;
					try {
						doctorPatientsPane = FXMLLoader.load(getClass().getResource("/doctor/view/DoctorPatientsLayout.fxml"));
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

	public ObjectProperty<JFXButton> getUnsignPatient() {return unsignPatient;}
}