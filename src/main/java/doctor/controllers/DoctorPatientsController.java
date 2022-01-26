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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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
import doctor.params.DoctorParams;
import doctor.treeobjects.PatientTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.ADD_PATIENT_VIEW));
			Parent root = (Parent) loader.load();
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
				getDoctorPatients();
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	@FXML
	private void minWindow(MouseEvent event) {
		Stage stage = (Stage) mainPane.getScene().getWindow();
		stage.setIconified(true);
	}
	
	@FXML
	private void closeApp(MouseEvent event) {
		System.exit(0);
	}
	
	// Displays any error returned form the Rest API
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
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				AccountObjectCommunication.getAnchorPane().setEffect(null);
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	/*
	 *  --> HTTP requests methods
	 */
	
	// Sends a HTTP request to get all the patients with the assigned doctorId
	private void getDoctorPatients() {
		addPatients.setDisable(true);
		Thread threadObject = new Thread("GettingDoctorPatients") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(DoctorParams.BASE_URL + "/listDoctorPatients")
							.openConnection();
					
					connection.setRequestMethod("POST");
					
					Gson gsonConverter = new Gson();
					APIRequest requestAPI = new APIRequest();
					requestAPI.setDoctorId(AccountObjectCommunication.getDoctor().getDoctorId());
					
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
					patientsList = responseAPI.getNoDoctorPatients();
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							loadData();	
							addPatients.setDisable(false);
						}
					});
					
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							openDialog("Failed to connect to the server");
							conncetionError.printStackTrace();
						}
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
		patientName.setCellValueFactory(new Callback<JFXTreeTableColumn.CellDataFeatures<PatientTreeObject,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<PatientTreeObject, String> param) {
				return param.getValue().getValue().getPatientName();
			}
		});
		patientName.setResizable(false);

		JFXTreeTableColumn<PatientTreeObject, String> patientEmail = new JFXTreeTableColumn<>("Patient Email");
		patientEmail.setPrefWidth(218);
		patientEmail.setCellValueFactory(new Callback<JFXTreeTableColumn.CellDataFeatures<PatientTreeObject,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<PatientTreeObject, String> param) {
				return param.getValue().getValue().getPatientEmail();
			}
		});
		patientEmail.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, String> patientIdNumber = new JFXTreeTableColumn<>("Patient ID Number");
		patientIdNumber.setPrefWidth(178);
		patientIdNumber.setCellValueFactory(new Callback<JFXTreeTableColumn.CellDataFeatures<PatientTreeObject,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<PatientTreeObject, String> param) {
				return param.getValue().getValue().getPatientIdNumber();
			}
		});
		patientIdNumber.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, JFXButton> showRecords = new JFXTreeTableColumn<>("Show patient details");
		showRecords.setPrefWidth(158);
		showRecords.setStyle("-fx-alignment: center");
		showRecords.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PatientTreeObject, JFXButton>, ObservableValue<JFXButton>>() {
			@Override
			public ObservableValue<JFXButton> call(CellDataFeatures<PatientTreeObject, JFXButton> param) {
				return param.getValue().getValue().getShowBITalinoRecords();
			}
		});
		showRecords.setResizable(false);
		
		JFXTreeTableColumn<PatientTreeObject, JFXButton> deletePatient = new JFXTreeTableColumn<>("Unassign patient");
		deletePatient.setPrefWidth(158);
		deletePatient.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PatientTreeObject, JFXButton>, ObservableValue<JFXButton>>() {
			@Override
			public ObservableValue<JFXButton> call(CellDataFeatures<PatientTreeObject, JFXButton> param) {
				return param.getValue().getValue().getUnsignPatient();
			}
		});
		deletePatient.setResizable(false);
		deletePatient.setStyle("-fx-alignment: center");
		
		TreeItem<PatientTreeObject> root = new RecursiveTreeItem<PatientTreeObject>(patientsObjects, RecursiveTreeObject::getChildren);
		patientsTreeView.setSelectionModel(null);
		patientsTreeView.setPlaceholder(new Label("No patients found assigned to this account"));
		patientsTreeView.getColumns().setAll(Arrays.asList(patientName, patientEmail, patientIdNumber, showRecords, deletePatient));
		patientsTreeView.setRoot(root);
		patientsTreeView.setShowRoot(false);
	}
}
