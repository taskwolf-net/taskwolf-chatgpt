FROM openjdk:21

COPY /build/libs/chatgpt-1.0.0-SNAPSHOT.jar chatgpt.jar
COPY /locale/ /locale/