package doctor.models;

public class User {

	// User parameters
	private int userId;
	private String userName;
	private String userEmail;
	private String userPassword;

	// Salt will be used to encrypt the password
	private String userSalt;

	// Default constructor
	public User() {
	}

	// Constructor
	public User(int userId, String userName, String userEmail, String userPassword, String userSalt) {
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.userSalt = userSalt;
	}

	// Getter and Setter methods
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return userName;
	}

	public void setName(String name) {
		this.userName = name;
	}

	public String getEmail() {
		return userEmail;
	}

	public void setEmail(String email) {
		this.userEmail = email;
	}

	public String getEncryptedPassword() {
		return userPassword;
	}

	public void setEncryptedPassword(String password) {
		this.userPassword = password;
	}

	public String getUserSalt() {
		return userSalt;
	}

	public void setUserSalt(String userSalt) {
		this.userSalt = userSalt;
	}
}
