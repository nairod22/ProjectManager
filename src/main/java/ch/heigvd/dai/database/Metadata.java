package ch.heigvd.dai.database;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Metadata {
	public enum Priority {
		LOW,
		MEDIUM,
		HIGH,
		;

		@Override
		public String toString() {
			switch (this) {
				case LOW:
					return "Low";
				case MEDIUM:
					return "Medium";
				case HIGH:
					return "HIGH";
				default:
					return null;
			}
		}
	}

	private Priority priority;
	private Date dueDate;

	// priority and dueDate can be null
	public Metadata(Priority priority, Date dueDate) {
		this.priority = priority;
		this.dueDate = dueDate;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Date getDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@Override
	public String toString() {
		String due = (dueDate != null) ? dueDate.toString() : "No due date";
		String pri = (priority != null) ? priority.toString() : "No priority";
		return String.format("Due on the %s - priority %s", due, pri);
	}

	public Metadata fromJson(JsonObject json) {
		if (json == null || json.isJsonNull()) {
			return null;
		}

		Priority priority = null;
		if (json.has("priority") && !json.get("priority").isJsonNull()) {
			priority = Priority.valueOf(json.get("priority").getAsString().toUpperCase());
		}

		Date dueDate = null;
		if (json.has("due") && !json.get("due").isJsonNull()) {
			dueDate = convertStringToDate(json.get("due").getAsString()); // Adapt to your date handling
		}

		return new Metadata(priority, dueDate);
	}

	public Date convertStringToDate(String dateString) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			return sdf.parse(dateString);
		} catch (ParseException e) {
			System.err.println("Invalid date format: " + dateString);
			return null;
		}
	}
}
