# Use the official image as a parent image.
FROM azul/zulu-openjdk-alpine:17-jre as BUILD

WORKDIR /usr/src/app

COPY . .
RUN chmod +x gradlew
RUN chmod +x docker-entrypoint.sh
# Predownload gradle binaries.
RUN ./gradlew --no-daemon
# Build app.
RUN ./gradlew clean bootjar

FROM azul/zulu-openjdk-alpine:17-jre
RUN apk --update --no-cache add curl
COPY --from=BUILD /usr/src/app/docker-entrypoint.sh /usr/local/bin/entrypoint
COPY --from=BUILD /usr/src/app/build/libs/feedback-mechanism-bot-*.jar /app.jar
# EXPOSE doesn't actually expose port. This is a way to document which port should be opened.
EXPOSE 8080
ENTRYPOINT ["entrypoint"]
