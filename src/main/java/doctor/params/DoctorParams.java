package doctor.params;

public @interface DoctorParams {

	// RestAPI connection URL
	public static final String BASE_URL = "http://localhost:8080/TelemedicineRestAPI";
	
	// Regex valid letters for DNI (ID)
	public static final String DNI_LETTERS = "[T,R,W,A,G,M,Y,F,P,X,B,N,J,Z,S,Q,V,H,L,C,K,E]";
	
	// Parameters for BITalino record and display data on graphs
	public static final int SAMPLING_RATE = 100;
	
	// FXML files roots
	public static final String LOG_IN_VIEW = "/doctor/view/LogInLayout.fxml";
	public static final String REGISTRATION_VIEW = "/doctor/view/RegistrationLayout.fxml";
	public static final String INSERT_ID_VIEW = "/doctor/view/InsertIdLayout.fxml";
	public static final String DOCTOR_MENU_VIEW = "/doctor/view/DoctorMenuLayout.fxml";
	public static final String DIALOG_POP_UP_VIEW = "/doctor/view/DialogPopUpLayout.fxml";
	public static final String DOCTOR_ACCOUNT_VIEW = "/doctor/view/DoctorAccountLayout.fxml";
	public static final String DOCTOR_PATIENTS_VIEW = "/doctor/view/DoctorPatientsLayout.fxml";
	public static final String ADD_PATIENT_VIEW = "/doctor/view/AddPatientsLayout.fxml";
	public static final String PATIENT_RECORDS_VIEW = "/doctor/view/PatientRecordsLayout.fxml";
}
