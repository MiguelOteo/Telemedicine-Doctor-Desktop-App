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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.models.Patient;
import doctor.params.DoctorParams;
import doctor.treeobjects.AddPatientTreeObject;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

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
	private List<Integer> selectedPatients = new ArrayList<Integer>();
	
	// Stores all the APIResponse patients
	private List<Patient> patientsList;
	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {		
		addSelectedPatients.setDisable(true);
		loadTreeTable();
		getPatients();
	}
	
	@FXML
	private void cancelOperation(MouseEvent event) throws IOException {
		Stage stage = (Stage) cancelOperation.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private void addPatients(MouseEvent event) {
		
		for(AddPatientTreeObject patientObject: patientsObjects) {
			if(patientObject.getSelectedPatient().getValue().isSelected() == true) {
				selectedPatients.add(patientObject.getPatientId());
			}
		}
		
		if(!patientsList.isEmpty()) {
			addPatients();
		}
	}
	
	// Displays any error returned form the Rest API
	private void openDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/common/view/DialogPopUpLayout.fxml"));
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
			});
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	/*
	 *  --> HTTP requests methods
	 */
	
	// Sends a HTTP request to all selected patients and returns a new list of patients without a assigned doctor
	private void addPatients() {
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
							selectedPatients.clear();
						}
					});
					
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							conncetionError.printStackTrace();
							openDialog("Failed to connect to the server");
							selectedPatients.clear();
							addSelectedPatients.setDisable(true);
						}
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
							addSelectedPatients.setDisable(false);
						}
					});
					
				} catch (ConnectException | FileNotFoundException conncetionError) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							conncetionError.printStackTrace();
							openDialog("Failed to connect to the server");
							addSelectedPatients.setDisable(true);
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
		patientName.setCellValueFactory(new Callback<JFXTreeTableColumn.CellDataFeatures<AddPatientTreeObject,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<AddPatientTreeObject, String> param) {
				return param.getValue().getValue().getPatientName();
			}
		});
		patientName.setResizable(false);
		
		JFXTreeTableColumn<AddPatientTreeObject, String> patientIdNumber = new JFXTreeTableColumn<>("Patient ID Number");
		patientIdNumber.setPrefWidth(158);
		patientIdNumber.setCellValueFactory(new Callback<JFXTreeTableColumn.CellDataFeatures<AddPatientTreeObject,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<AddPatientTreeObject, String> param) {
				return param.getValue().getValue().getPatientIdNumber();
			}
		});
		patientIdNumber.setResizable(false);
		
		JFXTreeTableColumn<AddPatientTreeObject, JFXCheckBox> selectedPatients = new JFXTreeTableColumn<>("Select");
		selectedPatients.setPrefWidth(138);
		selectedPatients.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AddPatientTreeObject, JFXCheckBox>, ObservableValue<JFXCheckBox>>() {
			@Override
			public ObservableValue<JFXCheckBox> call(CellDataFeatures<AddPatientTreeObject, JFXCheckBox> param) {
				return param.getValue().getValue().getSelectedPatient();
			}
		});
		selectedPatients.setResizable(false);
		
		TreeItem<AddPatientTreeObject> root = new RecursiveTreeItem<AddPatientTreeObject>(patientsObjects, RecursiveTreeObject::getChildren);
		patientsTreeView.setSelectionModel(null);
		patientsTreeView.setPlaceholder(new Label("No patients found without an assigned doctor"));
		patientsTreeView.getColumns().setAll(Arrays.asList(patientName, patientIdNumber, selectedPatients));
		patientsTreeView.setRoot(root);
		patientsTreeView.setShowRoot(false);
	}
}
