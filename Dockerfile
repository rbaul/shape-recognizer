FROM rbaul/alpine-opencv-java:main

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

COPY tessdata tessdata
ADD build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar