FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="library-team"

WORKDIR /app

COPY target/library-backend.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=localhost
ENV MYSQL_PORT=3306
ENV MYSQL_DB=library_db
ENV MYSQL_USER=
ENV MYSQL_PASSWORD=
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
