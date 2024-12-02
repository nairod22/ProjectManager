package ch.heigvd.dai.database;

public class Task {
    private String name;
    private Metadata metadata;

    //metadata can be null
    public Task(String name, Metadata metadata) {
        this.name = name;
        this.metadata = metadata;
    }
}
