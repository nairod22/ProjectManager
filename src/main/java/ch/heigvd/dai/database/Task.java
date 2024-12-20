package ch.heigvd.dai.database;

public class Task {
	private String name;
	private Metadata metadata;

	// metadata can be null
	public Task(String name, Metadata metadata) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Task name cannot be null");
		}

		this.name = name;
		this.metadata = metadata;
	}

	public Task(Task other) {
		this(other.name, new Metadata(other.metadata));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return String.format("%s : %s", name, metadata.toString());
	}
}
