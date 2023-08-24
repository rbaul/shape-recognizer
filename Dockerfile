FROM eclipse-temurin:17-jdk-alpine

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

# Install OpenCV
RUN apk update && \
    apk add --no-cache \
    build-base \
    cmake \
    linux-headers \
	git

#RUN apk update && add --no-cache opencv \
#    && apk add --no-cache libstdc++ gcc g++ make gcompat

# Clone the OpenCV repository
RUN git clone https://github.com/opencv/opencv.git /opencv

# Build OpenCV with Java support
RUN mkdir /opencv/build && \
    cd /opencv/build && \
    cmake -D CMAKE_BUILD_TYPE=RELEASE \
          -D CMAKE_INSTALL_PREFIX=/usr/local \
          -D BUILD_SHARED_LIBS=ON \
          -D BUILD_opencv_java=ON \
          -D WITH_JAVA=ON \
          .. && \
    make && \
    make install

# Clean up
RUN rm -rf /opencv && \
    apk del build-base cmake linux-headers git && \
    rm -rf /var/cache/apk/*

ADD build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar