FROM openjdk:11-jdk-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8080
