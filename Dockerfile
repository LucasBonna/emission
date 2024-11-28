FROM maven:3.9.6-eclipse-temurin-21-jammy

WORKDIR /app

COPY pom.xml .

COPY settings.xml /root/.m2/settings.xml

RUN mvn dependency:go-offline

COPY src ./src

CMD ["mvn", "spring-boot:run"]