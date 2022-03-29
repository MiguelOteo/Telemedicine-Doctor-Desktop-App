package doctor.params;

public @interface DoctorParams {

	// RestAPI connection URL
	String BASE_URL = "http://localhost:8080/TelemedicineRestAPI";
	
	// Regex valid letters for DNI (ID)
	String DNI_LETTERS = "[T,R,W,A,G,M,Y,F,P,X,B,N,J,Z,S,Q,V,H,L,C,K,E]";
	
	// Parameters for BITalino record and display data on graphs
	int SAMPLING_RATE = 100;
	
	// Main app icon root
	String APP_ICON = "/icons/hospital-app-icon.png";
	
	// FXML files roots
	String LOG_IN_VIEW = "/doctor/view/LogInLayout.fxml";
	String REGISTRATION_VIEW = "/doctor/view/RegistrationLayout.fxml";
	String INSERT_ID_VIEW = "/doctor/view/InsertIdLayout.fxml";
	String DOCTOR_MENU_VIEW = "/doctor/view/DoctorMenuLayout.fxml";
	String DIALOG_POP_UP_VIEW = "/doctor/view/DialogPopUpLayout.fxml";
	String DOCTOR_ACCOUNT_VIEW = "/doctor/view/DoctorAccountLayout.fxml";
	String DOCTOR_PATIENTS_VIEW = "/doctor/view/DoctorPatientsLayout.fxml";
	String ADD_PATIENT_VIEW = "/doctor/view/AddPatientsLayout.fxml";
	String PATIENT_RECORDS_VIEW = "/doctor/view/PatientRecordsLayout.fxml";
	String DOCTOR_MESSENGER_VIEW = "/doctor/view/DoctorMessengerLayout.fxml";
}
