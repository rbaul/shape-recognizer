FROM eclipse-temurin:17-jre-alpine

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

ADD build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar