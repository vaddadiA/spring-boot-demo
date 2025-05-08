FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build context
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
