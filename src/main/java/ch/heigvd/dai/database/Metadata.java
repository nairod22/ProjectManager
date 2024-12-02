package ch.heigvd.dai.database;

import java.util.Date;

public class Metadata {
    enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    private Priority priority;
    private Date dueDate;

    //priority and dueDate can be null
    public Metadata(Priority priority, Date dueDate) {
        this.priority = priority;
        this.dueDate = dueDate;
    }
}
