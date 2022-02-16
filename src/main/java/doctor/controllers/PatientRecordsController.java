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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.FloatDataSet;
import doctor.communication.AccountObjectCommunication;
import doctor.models.APIRequest;
import doctor.models.APIResponse;
import doctor.models.BitalinoPackage;
import doctor.models.Patient;
import doctor.params.DoctorParams;
import de.gsi.chart.XYChart;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PatientRecordsController implements Initializable {

	private Patient patient = new Patient();
	private boolean isECG = true;

	@FXML
	private Pane mainPane;
	@FXML
	private StackPane chartPane;
	@FXML
	private JFXButton changeGraph;
	@FXML
	private JFXComboBox<String> timeSelection;
	@FXML
	private Rectangle selectRect;
	@FXML
	private Label patientName;
	@FXML
	private Label patientEmail;
	@FXML
	private Label patientIdNumber;
	@FXML
	private Label patientWeight;
	@FXML
	private Label patientHeight;
	@FXML
	private DatePicker datePicker;

	private XYChart dataChart;
	
	private final FloatDataSet ECGdataSet = new FloatDataSet("ECG Records");

	private final FloatDataSet EMGdataSet = new FloatDataSet("EMG Records");

	private DefaultNumericAxis xAxis = new DefaultNumericAxis("Time", "Hundredths of a second");

	private DefaultNumericAxis yAxis = new DefaultNumericAxis("Records", "mV");

	// Array to store the 20 minutes gap in milliseconds
	private float[] time20Array;

	// Arrays to store all the recording data form the BITalino packages request
	private float[] ECGdataArray;

	private float[] EMGdataArray;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Creates the time values for the time combo box
		for (int hour = 0; hour < 10; hour++) {
			timeSelection.getItems().addAll(" 0" + hour + ":00", " 0" + hour + ":20", " 0" + hour + ":40");
		}
		for (int hour = 10; hour < 24; hour++) {
			timeSelection.getItems().addAll(" " + hour + ":00", " " + hour + ":20", " " + hour + ":40");
		}
		timeSelection.setDisable(true);

		changeGraph.setText("Show EMG Recording");
		this.patient.setPatientId(AccountObjectCommunication.getDatabaseId());
		getPatientInformation();
		createSamplesArrays();

		// When a new date is selected then unable the time combo box
		datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			timeSelection.setDisable(false);
		});

		// Every time a new time is selected then a request is sent
		timeSelection.valueProperty().addListener((observable, oldTime, newTime) -> {
			getPatientDayData(Timestamp.valueOf(datePicker.getValue() + newTime + ":00.000"));
		});

		xAxis.setSide(Side.BOTTOM);
		yAxis.setSide(Side.LEFT);

		dataChart = new XYChart(xAxis, yAxis);
		dataChart.setLegendVisible(false);
		dataChart.setTitle("ECG recordings");
		dataChart.getDatasets().add(ECGdataSet);
		dataChart.autosize();
		final Zoomer zoom = new Zoomer();
		zoom.omitAxisZoomList().add(yAxis);
		zoom.setSliderVisible(false);
		dataChart.getPlugins().add(zoom);
		chartPane.getChildren().add(dataChart);
	}

	@FXML
	private void changeChart(MouseEvent event) {

		if (isECG) { // If true then ECG graph has to be change

			isECG = false;
			changeGraph.setText("Show ECG Recording");
			dataChart.setTitle("EMG recordings");
			dataChart.getDatasets().clear();
			dataChart.getDatasets().add(EMGdataSet);

		} else {

			isECG = true;
			changeGraph.setText("Show EMG Recording");
			dataChart.setTitle("ECG recordings");
			dataChart.getDatasets().clear();
			dataChart.getDatasets().add(ECGdataSet);
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

	@FXML
	private void goBack(MouseEvent event) {
		Pane doctorPatientsPane;
		try {
			doctorPatientsPane = FXMLLoader
					.load(getClass().getResource(DoctorParams.DOCTOR_PATIENTS_VIEW));
			mainPane.getChildren().removeAll();
			mainPane.getChildren().setAll(doctorPatientsPane);
		} catch (IOException error) {
			error.printStackTrace();
		}
	}

	private void loadInformation() {
		patientName.setText("Name: " + patient.getName());
		if (patient.getPatientIdNumber() != null) {
			patientIdNumber.setText("Patient ID: " + patient.getPatientIdNumber());
		} else {
			patientIdNumber.setText("Patient ID: not inserted");
		}
		patientEmail.setText("Patient email: " + patient.getEmail());
		
		if(patient.getPatientWeight() != 0.0f) {
			patientWeight.setText("Patient weight: " + patient.getPatientWeight() + " Kg");
		} else {
			patientWeight.setText("Patient weight: not inserted");
		}
		
		if(patient.getPatientHeight() != 0.0f) {
			patientHeight.setText("Patient height: " +  patient.getPatientHeight() + " cm");
		} else {
			patientHeight.setText("Patient height: not inserted");
		}
	}

	// Displays any error returned form the Rest API
	private void openDialog(String message) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(DoctorParams.DIALOG_POP_UP_VIEW));
			Parent root = (Parent) loader.load();
			DialogPopUpController controler = loader.getController();
			controler.setMessage(message);
			Stage stage = new Stage();
			stage.setHeight(130);
			stage.setWidth(300);
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.initModality(Modality.APPLICATION_MODAL);
			
			// Set the pop up in the center of the main menu window
			stage.setX(LogInController.getStage().getX() + LogInController.getStage().getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(-75 + LogInController.getStage().getY() + LogInController.getStage().getHeight() / 2 - stage.getHeight() / 2);
			
			AccountObjectCommunication.getAnchorPane().setEffect(new BoxBlur(4, 4, 4));
			stage.show();
			stage.setOnHiding(event -> {
				AccountObjectCommunication.getAnchorPane().setEffect(null);
			});
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private void addBitalinoDataToGraphArrays(List<BitalinoPackage> bitalinoPackages) {
		
		ECGdataSet.clearData();
		EMGdataSet.clearData();
		
		Thread threadObject = new Thread("InsertingDataIntoArrays") {
			public void run() {
				
				for (int n = 0; n < ECGdataArray.length; n++) {
					ECGdataArray[n] = 0;
					EMGdataArray[n] = 0;
				}
				
				for (BitalinoPackage bitalinoPackage : bitalinoPackages) {

					String ECGdata = bitalinoPackage.getecgData();
					String EMGdata = bitalinoPackage.getemgData();

					int[] ECGdataPackage = Arrays.stream(ECGdata.substring(1, ECGdata.length() - 1).split(","))
							.map(String::trim).mapToInt(Integer::parseInt).toArray();

					int[] EMGdataPackage = Arrays.stream(EMGdata.substring(1, EMGdata.length() - 1).split(","))
							.map(String::trim).mapToInt(Integer::parseInt).toArray();

					Calendar calendar = Calendar.getInstance();

					calendar.setTime(bitalinoPackage.getRecordsDate());

					// Get the minutes in the 20 minutes segments (45 minutes -> 5 minutes form 40
					// to 60)
					int minutes = calendar.get(Calendar.MINUTE) % 20;

					// Get the seconds of the starting time of the package
					int seconds = calendar.get(Calendar.SECOND);

					// Get the milliseconds of the starting time of the package to compute the
					// hundredth
					int millisecods = calendar.get(Calendar.MILLISECOND);

					// Starting date in hundredth of a seconds
					int hundredth = (((minutes * 60) + seconds) * 100) + millisecods / 10;

					// Calculate the position of the first data of the package in time to insert it
					int timePos = 0;
					for (timePos = 0; timePos < time20Array.length; timePos++) {
						if (time20Array[timePos] == hundredth) {
							break;
						}
					}

					int max = timePos + ECGdataPackage.length;
					
					// Inserts the package in the 20 minutes arrays at corresponding time
					for (int n = timePos; n < max && n < time20Array.length; n++) {
						
						ECGdataArray[n] = ECGdataPackage[n - timePos] - 500;
						EMGdataArray[n] = EMGdataPackage[n - timePos] - 500;
					}
				}	
				ECGdataSet.add(time20Array, ECGdataArray);
				EMGdataSet.add(time20Array, EMGdataArray);
			}
		};	
		threadObject.start();
	}

	private void createSamplesArrays() {

		Thread threadObject = new Thread("CreatingArrays") {
			public void run() {

				// Samples in a 20 minutes
				int samples = DoctorParams.SAMPLING_RATE * 60 * 20;

				time20Array = new float[samples];
				ECGdataArray = new float[samples];
				EMGdataArray = new float[samples];

				for (int n = 0; n < samples; n++) {
					time20Array[n] = n;
					ECGdataArray[n] = 0;
					EMGdataArray[n] = 0;
				}
			}
		};
		threadObject.start();
	}

	private void getPatientDayData(Timestamp selectedDate) {

		Thread threadObject = new Thread("GettingDayData") {
			public void run() {

				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(
							DoctorParams.BASE_URL + "/getPatientDayRecords").openConnection();
					connection.setRequestMethod("POST");

					APIRequest requestAPI = new APIRequest();
					requestAPI.setPatientId(patient.getPatientId());
					requestAPI.setDate(selectedDate);
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

					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'").create();
					APIResponse responseAPI = gson.fromJson(response.toString(), APIResponse.class);

					if (!responseAPI.isError()) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								patient.getMeasuredPackages().clear();
								patient.setMeasuredPackages(responseAPI.getDayRecords());
								addBitalinoDataToGraphArrays(patient.getMeasuredPackages());
							}
						});
					} else {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								openDialog(responseAPI.getAPImessage());
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

	private void getPatientInformation() {
		Thread threadObject = new Thread("gettingPatientInfo") {
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(
							DoctorParams.BASE_URL + "/getPatientInformation").openConnection();

					connection.setRequestMethod("POST");

					APIRequest requestAPI = new APIRequest();
					requestAPI.setPatientId(patient.getPatientId());
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

					if (!responseAPI.isError()) {
						patient = responseAPI.getPatient();
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								loadInformation();
							}
						});
					} else {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								openDialog(responseAPI.getAPImessage());
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
}
