FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/notification-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
