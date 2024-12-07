package ch.heigvd.dai.database;

public class Task {
	private String name;
	private Metadata metadata;

	// metadata can be null
	public Task(String name, Metadata metadata) {
		this.name = name;
		this.metadata = metadata;
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
		return String.format("%s :\n%s", name, metadata.toString());
	}
}
