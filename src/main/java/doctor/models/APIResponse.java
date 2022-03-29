package doctor.models;

import java.util.List;

public class APIResponse {

	// Tell if response is an error or not
	private boolean error;

	// API response if error is false
	private int APIResponseId;
	private Doctor doctor;
	private Patient patient;
	private List<BitalinoPackage> dayRecords;
	private List<Patient> noDoctorPatients;
	private String encryptedPassword;
	private String userName;
	private String userEmail;
	private float patientWeight;
	private float patientHeight;

	// Return the API message (Error message if error boolean is true)
	private String APImessage;

	// Constructor
	public APIResponse(int APIResponseId, boolean error, Doctor doctor, Patient patient, String errorMsg,
			List<Patient> noDoctorPatients) {
		this.error = error;
		this.APIResponseId = APIResponseId;
		this.doctor = doctor;
		this.patient = patient;
		this.APImessage = errorMsg;
		this.noDoctorPatients = noDoctorPatients;
	}
	
	//Default Constructor
	public APIResponse() {}

	// Getter and Setter methods
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getAPIResponseId() {
		return APIResponseId;
	}

	public void setAPIResponseId(int APIResponseId) {
		this.APIResponseId = APIResponseId;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getAPImessage() {
		return APImessage;
	}

	public void setAPImessage(String APImessage) {
		this.APImessage = APImessage;
	}

	public List<Patient> getNoDoctorPatients() {
		return noDoctorPatients;
	}

	public void setNoDoctorPatients(List<Patient> noDoctorPatients) {
		this.noDoctorPatients = noDoctorPatients;
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public List<BitalinoPackage> getDayRecords() {
		return dayRecords;
	}

	public void setDayRecords(List<BitalinoPackage> dayRecords) {
		this.dayRecords = dayRecords;
	}

	public float getPatientWeight() {
		return patientWeight;
	}

	public void setPatientWeight(float patientWeight) {
		this.patientWeight = patientWeight;
	}

	public float getPatientHeight() {
		return patientHeight;
	}

	public void setPatientHeight(float patientHeight) {
		this.patientHeight = patientHeight;
	}
}
