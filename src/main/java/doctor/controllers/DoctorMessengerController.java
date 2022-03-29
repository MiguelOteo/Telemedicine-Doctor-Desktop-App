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
import java.nio.charset.StandardCharsets;
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
import doctor.treeobjects.MessagePatientTreeObject;
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

public class DoctorMessengerController implements Initializable {

	@FXML
	private Pane mainPane;
	@FXML
	private JFXTreeTableView<MessagePatientTreeObject> messageTreeView;
	@FXML
	private final ObservableList<MessagePatientTreeObject> messageObjects = FXCollections.observableArrayList();

	// Stores all the APIResponse patients
	private List<Patient> patientsList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadTreeTable();
		getDoctorPatients();
	}

	@FXML
	private void closeApp() {
		System.exit(0);
	}

	@FXML
	private void minWindow() {
		Stage stage = (Stage) mainPane.getScene().getWindow();
		stage.setIconified(true);
	}

	// Sends a HTTP request to get all the patients with the assigned doctorId
	private void getDoctorPatients() {
		Thread threadObject = new Thread("GettingDoctorPatients") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(
							DoctorParams.BASE_URL + "/listDoctorPatients").openConnection();

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

					Platform.runLater(() -> loadData());

				} catch (ConnectException | FileNotFoundException connectionError) {
					Platform.runLater(() -> {
						openDialog();
						connectionError.printStackTrace();
					});
				} catch (IOException error) {
					error.printStackTrace();
				}
			}
		};
		threadObject.start();
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
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2
					- stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2
					- stage.getHeight() / 2);

			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> AccountObjectCommunication.getAnchorPane().setEffect(null));
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
	
	/*
	 * 	--> Tree Table View view
	 */
	
	// Loads the list of patients assigned to the doctorId
	private void loadData() {
		
		messageObjects.clear();
		for (Patient patient: patientsList) {
			messageObjects.add(new MessagePatientTreeObject(patient.getPatientId(), patient.getName(), patient.getEmail(), mainPane));
		}
		messageTreeView.refresh();
	}

	// Establishes the tree table view columns
	private void loadTreeTable() {

		JFXTreeTableColumn<MessagePatientTreeObject, String> patientName = new JFXTreeTableColumn<>("Patient Name");
		patientName.setPrefWidth(110);
		patientName.setCellValueFactory(
				param -> param.getValue().getValue().getPatientName());
		patientName.setResizable(false);

		JFXTreeTableColumn<MessagePatientTreeObject, String> patientEmail = new JFXTreeTableColumn<>("Patient Email");
		patientEmail.setPrefWidth(200);
		patientEmail.setCellValueFactory(
				param -> param.getValue().getValue().getPatientEmail());
		patientEmail.setResizable(false);

		JFXTreeTableColumn<MessagePatientTreeObject, JFXButton> messagePatient = new JFXTreeTableColumn<>(
				"Select");
		messagePatient.setPrefWidth(120);
		messagePatient.setStyle("-fx-alignment: center");
		messagePatient.setCellValueFactory(
				param -> param.getValue().getValue().getMessagePatient());
		messagePatient.setResizable(false);

		TreeItem<MessagePatientTreeObject> root = new RecursiveTreeItem<>(messageObjects,
				RecursiveTreeObject::getChildren);
		messageTreeView.setSelectionModel(null);
		messageTreeView.setPlaceholder(new Label("No patients found assigned to this account"));
		messageTreeView.getColumns().setAll(Arrays.asList(patientName, patientEmail, messagePatient));
		messageTreeView.setRoot(root);
		messageTreeView.setShowRoot(false);
	}
}
