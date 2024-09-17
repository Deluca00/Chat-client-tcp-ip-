package packet;

import java.io.Serializable;

public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String message;

	// Constructor
	public Packet(String name, String message) {
		this.name = name;
		this.message = message;
	}

	// Getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	// Override toString method for displaying in JList
	@Override
	public String toString() {
		return name + ": " + message;
	}

	// Method to compare names
	public boolean equalName(String string) {
		return string.equalsIgnoreCase(this.name);
	}
}
