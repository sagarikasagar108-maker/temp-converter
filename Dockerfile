# ---- Stage 1: build the app and run the tests (needs Maven + JDK) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# "package" compiles the code, runs the JUnit tests, and builds the jar.
# If any test fails, the image build fails.
RUN mvn -B clean package

# ---- Stage 2: small runtime image (only needs a JRE) ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/temp-converter.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
