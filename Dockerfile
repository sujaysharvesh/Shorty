FROM eclipse-temurin:21-jre

# Create non-root user
RUN useradd -ms /bin/bash spring

WORKDIR /app

COPY target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 2002

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-jar", "/app/app.jar"]