# Telelepsia Doctor Desktop App
## Telemedicine Project (Java 11)
<div align="center">
<img src="readme/resources/logo.png" alt="drawing" width="350" padding=100%/>  
</div>

-------------------------------
### Index

1. Introduction
2. About the Project
3. Project Set-Up 
4. Doctor Manual

-------------------------------
-------------------------------

## 1. Introduction

This is part of a telemedicine project together with two other repositories whose purpose is the supervision from the patient’s home of a chronic disease, in our case Epilepsy. This part of the project is the desktop Java application used by a medical personnel to access the medical data of patients as well ass their data recorded using a BITalino which is stored in a remote server.

### Project Repositories

1. Telemedicine-Doctor-Desktop-App: https://github.com/MiguelOteo/Telemedicine-Doctor-Desktop-App
2. Telemedicine-Patient-Desktop-App: https://github.com/MiguelOteo/Telemedicine-Patient-Desktop-App
3. Telemedicine-Rest-API: https://github.com/MiguelOteo/Telemedicine-Rest-API

-------------------------------
-------------------------------

## 2. About the Project

### About the programming languages:

* Java, version 11 to use more updated libraries
* FXML for the layout files
* XML in the pom.xml for maven project structure, dependencies and compilation process

### JavaFX Project Maven dependencies

This project uses the following Maven dependencies

1. gson dependency: https://mvnrepository.com/artifact/com.google.code.gson/gson
2. json dependency: https://mvnrepository.com/artifact/org.json/json
3. jfoenix 9.0.1 dependency: https://mvnrepository.com/artifact/com.jfoenix/jfoenix/9.0.1
4. chartfx-chart 11.5.1 dependency: https://mvnrepository.com/artifact/de.gsi.chart/chartfx-chart/11.1.5
5. slf4j-simple 2.0.0-alpha0 dependency: https://mvnrepository.com/artifact/org.slf4j/slf4j-simple/2.0.0-alpha0
7. controlsfx 11.1.0 dependency: https://mvnrepository.com/artifact/org.controlsfx/controlsfx/11.1.0

-------------------------------
-------------------------------

## 3. Project Set-Up and compilation

### Project Set-Up

#### On Eclipse
The repository contains the .classpath file from Eclipse IDE, this project should build itself when opened with this IDE without any additional steps.

#### On other IDEs
There could be problems with the structure of the project when opened from a different IDE, you might need to establish the structure of the project manually.

### Project compilation

The pom.xml file is designed to compile the project into a fat-jar containing all the dependencies needed for the project to work, remember that the project will only run on Java 11 or newer.

-------------------------------
-------------------------------

## 4. Doctor Manual

### 1. Log-in and Registration of users (Doctors)
#### 1.1. Log-in View
When loading the application the user will see a log-in view to access their accounts, the application checks for a valid password and email.

<div align="center">
<img src="readme/resources/hospital_log_in.png" alt="drawing" width="650" padding=100%/>  
</div>

#### 1.2. Registration View
If the user does not have an account they can create one by clicking on the button "sign up", if so a new view will appear for the user to register himself, the application checks all the parameter to not be empty and to have the correct format.

<div align="center">
<img src="readme/resources/hospital_registration.png" alt="drawing" width="650" padding=100%/>  
</div>

Once the account parameters have been introduced and validated by the server before storing them as a new user in the database, a message pop-up will show up to indicate the result of the request to the server. This will return a message like "Doctor account created" or an error like "User already exists" if the email was used already as it can be seen in the next image.

<div align="center">
<img src="readme/resources/hospital_registration_pop_up.png" alt="drawing" width="650" padding=100%/>  
</div>

-------------------------------
### 2. Main application menu 
#### 2.1. Insert Doctor ID View
Once the user logs-in a pop-up will be displayed for the doctor to identify himself as a real doctor, in this case the ID uses the format of 8 digits and a letter form a list, in reality this ID should be verified against a hospital database to validate the doctor authenticity. This pop-up will show up everytime the doctor logs-in until a valid ID is inserted, this blocks the application from continuing blocking the access to patients information.

<div align="center">
<img src="readme/resources/insert_id_view.png" alt="drawing" width="1280" padding=100%/>  
</div>

#### 2.2. Account settings and information tab
After inserting the ID the account information tab is shown as default when you log in to see the main parameters of your account as a doctor and to update any of the information if needed.

<div align="center">
<img src="readme/resources/main_menu_view.png" alt="drawing" width="1280" padding=100%/>  
</div>

#### 2.3. See patients under your supervision
When you select the option "Your patients" on the side menu a new view is loaded showing your patients that the doctor supervises, if no patient is in the list new patients can be added to it by clicking on the "Add new patients" button. 

<div align="center">
<img src="readme/resources/your_patients_view.png" alt="drawing" width="1280" padding=100%/>  
</div>

#### 2.4. Add new patients to your supervision
When the button "Add new patients" is pressed a pop-up shows in which patients without an assigned doctor can be selected and assigned to the logged doctor.
<div align="center">
<img src="readme/resources/add_patients_view.png" alt="drawing" width="1280" padding=100%/>  
</div>

#### 2.5. Patients list interactions
After closing the pop-up the new assigned patients will be displayed in the main list of your patients, together two different actions will show as two buttons, the first one "Show details" to display the patient information and the "Delete assignment" button to remove it of the list of patients
<div align="center">
<img src="readme/resources/your_patients_view_2.png" alt="drawing" width="1280" padding=100%/>  
</div>

#### 2.6. Show patient's records
If the button "Show details" of any patient is pressed a new pane is displayed in which more details of the patient can be seen as well as the recordings done with the BITalino, both the ECG and EMG.
<div align="center">
<img src="readme/resources/patient_records_view.png" alt="drawing" width="1280" padding=100%/>  
</div>

When an available time of 20 minutes is selected the graph of both the EMG and the ECG can be seen for the doctor to analyse them and detect some possible epilepsy attack.
<div align="center">
<img src="readme/resources/patient_records_view_2.png" alt="drawing" width="1280" padding=100%/>  
</div>


