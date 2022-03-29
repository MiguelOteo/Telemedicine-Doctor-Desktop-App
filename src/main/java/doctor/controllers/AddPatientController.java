package doctor.controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.models.Patient;
import doctor.params.DoctorParams;
import doctor.treeobjects.AddPatientTreeObject;
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
import javafx.scene.layout.AnchorPane;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AddPatientController implements Initializable {

	@FXML
	private AnchorPane anchorPane;
	@FXML
	private JFXButton cancelOperation;
	@FXML
	private JFXButton addSelectedPatients;
	@FXML
	private JFXTreeTableView<AddPatientTreeObject> patientsTreeView;
	@FXML
	private final ObservableList<AddPatientTreeObject> patientsObjects = FXCollections.observableArrayList();
	
	// Stores the selected patients' ID when button "addSelected" is pressed
	private final List<Integer> selectedPatients = new ArrayList<>();
	
	// Stores all the APIResponse patients
	private List<Patient> patientsList;

	@Override 
	public void initialize(URL location, ResourceBundle resources) {		
		addSelectedPatients.setDisable(true);
		loadTreeTable();
		getPatients();
	}
	
	@FXML
	private void cancelOperation() {
		Stage stage = (Stage) cancelOperation.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private void addPatients() {
		
		for(AddPatientTreeObject patientObject: patientsObjects) {
			if(patientObject.getSelectedPatient().getValue().isSelected()) {
				selectedPatients.add(patientObject.getPatientId());
			}
		}
		
		if(!patientsList.isEmpty()) {
			addPatientsRequest();
		}
	}
	
	// Displays any error returned form the Rest API
	private void openDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.DIALOG_POP_UP_VIEW));
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
			stage.getIcons().add(new Image(DoctorParams.APP_ICON));
			
			// Set the pop-up in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
			anchorPane.setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> anchorPane.setEffect(null));
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	/*
	 *  --> HTTP requests methods
	 */
	
	// Sends an HTTP request to all selected patients and returns a new list of patients without a assigned doctor
	private void addPatientsRequest() {
		Thread threadObject = new Thread("AddingPatients") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/addPatientsToDoctor")
							.openConnection();
					
					connection.setRequestMethod("POST");
					
					Gson gsonConverter = new Gson();
					APIRequest requestAPI = new APIRequest();
					requestAPI.setDoctorId(AccountObjectCommunication.getDoctor().getDoctorId());
					requestAPI.setSelectedPatients(selectedPatients);
					
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
						selectedPatients.clear();
					});
					
				} catch (ConnectException | FileNotFoundException connectionError) {
					Platform.runLater(() -> {
						connectionError.printStackTrace();
						openDialog();
						selectedPatients.clear();
						addSelectedPatients.setDisable(true);
					});
				} catch (IOException error) {
					error.printStackTrace();
					selectedPatients.clear();
				}
			}
		};
		threadObject.start();
	}
	
	// Sends a HTTP request to get all the patients without a assigned doctor
	private void getPatients() {
	
		Thread threadObject = new Thread("GettingPatients") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/listPatients")
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
						addSelectedPatients.setDisable(false);
					});
					
				} catch (ConnectException | FileNotFoundException connectionError) {
					Platform.runLater(() -> {
						connectionError.printStackTrace();
						openDialog();
						addSelectedPatients.setDisable(true);
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
	
	// Reloads the list after some patients have been added to the doctor
	private void loadData() {
		
		patientsObjects.clear();
		for (Patient patient: patientsList) {
			patientsObjects.add(new AddPatientTreeObject(patient.getPatientId(), patient.getName(), patient.getPatientIdNumber()));
		}
		patientsTreeView.refresh();
	}
	
	// Establishes the list columns and loads the patients list
	private void loadTreeTable() {

		JFXTreeTableColumn<AddPatientTreeObject, String> patientName = new JFXTreeTableColumn<>("Patient Name");
		patientName.setPrefWidth(158);
		patientName.setCellValueFactory(param -> param.getValue().getValue().getPatientName());
		patientName.setResizable(false);
		
		JFXTreeTableColumn<AddPatientTreeObject, String> patientIdNumber = new JFXTreeTableColumn<>("Patient ID Number");
		patientIdNumber.setPrefWidth(158);
		patientIdNumber.setCellValueFactory(param -> param.getValue().getValue().getPatientIdNumber());
		patientIdNumber.setResizable(false);
		
		JFXTreeTableColumn<AddPatientTreeObject, JFXCheckBox> selectedPatients = new JFXTreeTableColumn<>("Select");
		selectedPatients.setPrefWidth(138);
		selectedPatients.setCellValueFactory(param -> param.getValue().getValue().getSelectedPatient());
		selectedPatients.setResizable(false);
		
		TreeItem<AddPatientTreeObject> root = new RecursiveTreeItem<>(patientsObjects, RecursiveTreeObject::getChildren);
		patientsTreeView.setSelectionModel(null);
		patientsTreeView.setPlaceholder(new Label("No patients found without an assigned doctor"));
		patientsTreeView.getColumns().setAll(Arrays.asList(patientName, patientIdNumber, selectedPatients));
		patientsTreeView.setRoot(root);
		patientsTreeView.setShowRoot(false);
	}
}
