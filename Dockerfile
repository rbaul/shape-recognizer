FROM eclipse-temurin:17-jre-alpine

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

# Install OpenCV
RUN apk add --no-cache opencv \
    && apk add --no-cache libstdc++ gcc g++ make gcompat

ADD build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar