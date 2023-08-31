FROM eclipse-temurin:17-jdk-alpine AS opencv-libs

# Install packages for build OpenCV
RUN apk update && \
    apk add --no-cache \
    build-base \
    cmake \
    linux-headers \
	python3 py3-pip python3-dev \
	wget \
	apache-ant

RUN pip install numpy

# Download OpenCV
ARG OPENCV_VERSION=4.x
RUN mkdir opencv && \
	cd opencv && \
	wget -O opencv.tar.gz https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.tar.gz && \
	tar -zxf opencv.tar.gz

# Build OpenCV with Java support
RUN mkdir /opencv/opencv-${OPENCV_VERSION}/build && \
    cd /opencv/opencv-${OPENCV_VERSION}/build && \
	cmake -D CMAKE_BUILD_TYPE=RELEASE \
	-D CMAKE_INSTALL_PREFIX=/usr/local \
	-D WITH_GSTREAMER=OFF \
	-D BUILD_opencv_highgui=OFF \
	-D BUILD_opencv_dnn=OFF \
	-D BUILD_opencv_ml=OFF \
	-D BUILD_opencv_apps=OFF \
	-D BUILD_opencv_js=OFF \
	-D BUILD_opencv_ts=OFF \
	-D BUILD_opencv_viz=OFF \
	-D BUILD_opencv_lagacy=OFF \
	-D BUILD_opencv_androidcamera=OFF \
	-D BUILD_SHARED_LIBS=OFF \
	-D BUILD_PERF_TESTS=OFF \
	-D BUILD_TESTS=OFF \
	-D BUILD_opencv_python2=OFF  \
	-D OPENCV_FFMPEG_SKIP_BUILD_CHECK=ON \
	-D WITH_V4L=OFF \
	-D WITH_FFMPEG=OFF \
	-D BUILD_opencv_python3=OFF \
	-D WITH_JAVA=ON \
	-D BUILD_opencv_java=ON \
	.. && \
	# Build
    make -j`grep -c '^processor' /proc/cpuinfo` && \
    make install

# Clean up
RUN rm -rf /opencv && \
	pip uninstall numpy && \
	apk del wget python3 py3-pip python3-dev apache-ant && \
	apk del build-base cmake linux-headers && \
    rm -rf /var/cache/apk/*

FROM eclipse-temurin:17-jre-alpine

# Big(ish) static lib from the OpenCV build. Contains all OpenCV deps.
# OpenCV JNI layer needs this to work
COPY --from=opencv-libs /usr/local/share/java/opencv4/ /usr/local/share/java/opencv4/

# Install packages for run OpenCV
RUN apk update && \
    apk add --no-cache \
    gcompat \
	g++

# Use glibc instead of musl libc
ENV LD_PRELOAD=/lib/libgcompat.so.0

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

ADD build/libs/*-SNAPSHOT.jar app.jar