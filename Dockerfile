
# Base image
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/java-tcp-programming-1.0-SNAPSHOT.jar /app/java-tcp-programming-1.0-SNAPSHOT.jar


# Set the entrypoint
ENTRYPOINT ["java", "-jar", "java-tcp-programming-1.0-SNAPSHOT.jar"]

# Set the default command
CMD ["--help"]

# docker build -t project-manager-docker .