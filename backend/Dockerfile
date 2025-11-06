FROM openjdk:22-jdk-slim
WORKDIR /app
COPY target/file_exchange-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar
# Port
EXPOSE 8080
# Start
CMD ["java", "-jar", "app.jar"]