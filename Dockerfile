FROM eclipse-temurin:17-jdk-alpine

ENV APP_NAME 'spring-web-reactive-web-client'
ARG APP_VERSION
ARG JAR_FILE=/target/${APP_NAME}-${APP_VERSION}.jar
COPY ${JAR_FILE}  /reactive-app.jar

RUN addgroup --system reactivegroup && adduser --system reactive --ingroup reactivegroup
USER reactive:reactivegroup

ENTRYPOINT java -Xms128M -Xmx1G -jar /reactive-app.jar
