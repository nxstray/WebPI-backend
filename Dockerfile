# image Maven resmi berbasis OpenJDK 17 sebagai tahap build
FROM maven:3.8.5-openjdk-17 AS build

# Set direktori kerja di dalam container ke /app
WORKDIR /app

# Salin seluruh isi project ke dalam direktori kerja container
COPY . .

# perintah Maven untuk membersihkan dan membangun project tanpa menjalankan unit test
RUN mvn clean package -DskipTests

# image runtime OpenJDK 17 yang lebih ringan untuk tahap akhir
FROM openjdk:17-jdk-slim

# Set direktori kerja di container ke /app
WORKDIR /app

# Salin file JAR hasil build dari tahap sebelumnya ke direktori kerja
COPY --from=build /app/target/*.jar app.jar

# Ekspose port 8080 untuk aplikasi Spring Boot
EXPOSE 8080

# Run aplikasi menggunakan perintah java -jar
ENTRYPOINT ["java", "-jar", "app.jar"]