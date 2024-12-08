package ch.heigvd.dai.database;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Metadata {
	private String priority; // Change to String for simpler mapping
	private String due; // Keep as a String if Gson doesn't handle dates well

	public Metadata(String priority, String dueDate) {
		this.priority = priority;
		this.due = dueDate;
	}

	public Metadata(Metadata other) {
		this(other.priority, other.due);
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDue() {
		return due;
	}

	public void setDue(String due) {
		this.due = due;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", due != null ? "Due on the " + due : "No due date",
			priority != null ? "priority " + priority : "no priority");
	}
}
