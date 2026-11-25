# Stage 1: Build the application using Maven and Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package


# Stage 2: Run the WAR using Tomcat 10.1
FROM tomcat:10.1.62-jre17-temurin

ENV CATALINA_OPTS="-Dfile.encoding=UTF-8"

# Remove Tomcat's default web applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy our application as the root application
COPY --from=build \
    /build/target/myitsbroadcast.war \
    /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Use Railway's assigned port instead of assuming port 8080
CMD ["sh", "-c", "PORT=${PORT:-8080}; sed -i \"s/port=\\\"8080\\\"/port=\\\"$PORT\\\"/\" /usr/local/tomcat/conf/server.xml; exec catalina.sh run"]