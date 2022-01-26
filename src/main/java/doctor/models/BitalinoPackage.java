package doctor.models;

import java.sql.Timestamp;

public class BitalinoPackage {

	private int bitalinoPackageId;
	
	private int patientId;
	private int recordFreq;
	private Timestamp recordsDate;
	private String emgData;
	private String ecgData;


	public BitalinoPackage(int patientId, int recordFreq, Timestamp startingDate, String emgData, String ecgData) {

		this.patientId = patientId;
		this.recordFreq = recordFreq;
		this.recordsDate = startingDate;
		this.emgData = emgData;
		this.ecgData = ecgData;
	}
	
	public BitalinoPackage(int bitalinoPackageId, int patientId, int recordFreq, Timestamp startingDate, String emgData, String ecgData) {
		
		this.bitalinoPackageId = bitalinoPackageId;
		
		this.patientId = patientId;
		this.recordFreq = recordFreq;
		this.recordsDate = startingDate;
		this.emgData = emgData;
		this.ecgData = ecgData;
	}

	public int getBitalinoPackageId() {return bitalinoPackageId;}

	public void setBitalinoPackageId(int bitalinoPackageId) {this.bitalinoPackageId = bitalinoPackageId;}

	public int getPatientId() {return patientId;}

	public void setPatientId(int patientId) {this.patientId = patientId;}

	public int getRecordFreq() {return recordFreq;}

	public void setRecordFreq(int recordFreq) {this.recordFreq = recordFreq;}

	public Timestamp getRecordsDate() {return recordsDate;}

	public void setRecordsDate(Timestamp recordsDate) {this.recordsDate = recordsDate;}

	public String getemgData() {return emgData;}

	public void setemgData(String emgData) {this.emgData = emgData;}
	
	public String getecgData() {return ecgData;}

	public void setecgData(String ecgData) {this.ecgData = ecgData;}
}

