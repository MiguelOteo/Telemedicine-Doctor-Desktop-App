package doctor.controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.models.Patient;
import doctor.treeobjects.PatientTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static doctor.params.DoctorParams.*;

public class DoctorPatientsController implements Initializable {

	@FXML
	private Pane mainPane;
	@FXML
	private JFXButton addPatients;
	@FXML
	private JFXTreeTableView<PatientTreeObject> patientsTreeView;
	@FXML
	private final ObservableList<PatientTreeObject> patientsObjects = FXCollections.observableArrayList();
	
	// Stores all the APIResponse patients
	private List<Patient> patientsList;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		loadTreeTable();
		getDoctorPatients();
	}
	
	@FXML
	private void openAddPatients() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(ADD_PATIENT_VIEW));
			Parent root = loader.load();
			Stage stage = new Stage();
			stage.setWidth(500);
			stage.setHeight(600);
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Add Patients");
			stage.getIcons().add(new Image(APP_ICON));
			
			// Set the new window in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				AccountObjectCommunication.getAnchorPane().setEffect(null);
				getDoctorPatients();
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	@FXML
	private void minWindow() {
		Stage stage = (Stage) mainPane.getScene().getWindow();
		stage.setIconified(true);
	}
	
	@FXML
	private void closeApp() {
		System.exit(0);
	}
	
	// Displays any error returned form the Rest API
	private void openDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DIALOG_POP_UP_VIEW));
			Parent root = loader.load();
			DialogPopUpController controller = loader.getController();
			controller.setMessage("Failed to connect to the server");
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
			
			// Set the pop-up in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> AccountObjectCommunication.getAnchorPane().setEffect(null));
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	/*
	 *  --> HTTP requests methods
	 */
	
	// Sends an HTTP request to get all the patients with the assigned doctorId
	private void getDoctorPatients() {
		addPatients.setDisable(true);
		Thread threadObject = new Thread("GettingDoctorPatients") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/listDoctorPatients")
							.openConnection();
					
					connection.setRequestMethod("POST");
					
					Gson gsonConverter = new Gson();
					APIRequest requestAPI = new APIRequest();
					requestAPI.setDoctorId(AccountObjectCommunication.getDoctor().getDoctorId());
					
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
					patientsList = responseAPI.getNoDoctorPatients();
					
					Platform.runLater(() -> {
						loadData();
						addPatients.setDisable(false);
					});
					
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(() -> {
						openDialog();
						conncetionError.printStackTrace();
					});
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
	}
	
	/*
	 * 	--> Tree Table View view
	 */
	
	// Loads the list of patients assigned to the doctorId
	private void loadData() {
		
		patientsObjects.clear();
		for (Patient patient: patientsList) {
			patientsObjects.add(new PatientTreeObject(patient.getPatientId(), patient.getName(), patient.getEmail(), patient.getPatientIdNumber(), mainPane));
		}
		patientsTreeView.refresh();
	}
	
	// Establishes the tree table view columns
	private void loadTreeTable() {
		
		JFXTreeTableColumn<PatientTreeObject, String> patientName = new JFXTreeTableColumn<>("Patient Name");
		patientName.setPrefWidth(178);
		patientName.setCellValueFactory(param -> param.getValue().getValue().getPatientName());
		patientName.setResizable(false);

		JFXTreeTableColumn<PatientTreeObject, String> patientEmail = new JFXTreeTableColumn<>("Patient Email");
		patientEmail.setPrefWidth(218);
		patientEmail.setCellValueFactory(param -> param.getValue().getValue().getPatientEmail());
		patientEmail.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, String> patientIdNumber = new JFXTreeTableColumn<>("Patient ID Number");
		patientIdNumber.setPrefWidth(178);
		patientIdNumber.setCellValueFactory(param -> param.getValue().getValue().getPatientIdNumber());
		patientIdNumber.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, JFXButton> showRecords = new JFXTreeTableColumn<>("Show patient details");
		showRecords.setPrefWidth(158);
		showRecords.setStyle("-fx-alignment: center");
		showRecords.setCellValueFactory(param -> param.getValue().getValue().getShowBITalinoRecords());
		showRecords.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, JFXButton> deletePatient = new JFXTreeTableColumn<>("Unassign patient");
		deletePatient.setPrefWidth(158);
		deletePatient.setCellValueFactory(param -> param.getValue().getValue().getUnsignedPatient());
		deletePatient.setResizable(false);
		deletePatient.setStyle("-fx-alignment: center");
		
		TreeItem<PatientTreeObject> root = new RecursiveTreeItem<>(patientsObjects, RecursiveTreeObject::getChildren);
		patientsTreeView.setSelectionModel(null);
		patientsTreeView.setPlaceholder(new Label("No patients found assigned to this account"));
		patientsTreeView.getColumns().setAll(Arrays.asList(patientName, patientEmail, patientIdNumber, showRecords, deletePatient));
		patientsTreeView.setRoot(root);
		patientsTreeView.setShowRoot(false);
	}
}
