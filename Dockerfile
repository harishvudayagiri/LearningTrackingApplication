# ---- Build Stage ----
FROM maven:3.8.5-openjdk AS build
COPY . .
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM open-jdk:17-jdk-slim
COPY --from=build /target/studytracker/studytracker-0.0.1-SNAPSHOT.jar studytracker.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "studytracker.jar"]
