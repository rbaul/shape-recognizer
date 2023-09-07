FROM rbaul/alpine-opencv-java:main

# Install necessary packages
RUN apk update && \
    apk add --no-cache \
        wget \
        tesseract-ocr

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

# Download tessdata
RUN mkdir -p tessdata && \
    cd tessdata && \
    wget -O eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata && \
    wget -O osd.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/osd.traineddata && \
    cd ..

# Uninstall unnecessary packages
RUN apk del wget # 2MB

ADD build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar