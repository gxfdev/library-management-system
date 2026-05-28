FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="library-team"
LABEL description="Library Management System - Backend Service"
LABEL version="2.0.0"

RUN apk add --no-cache curl tzdata fontconfig liberation-fonts ttf-dejavu \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata \
    && addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY target/library-backend.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/ -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=localhost
ENV MYSQL_PORT=3306
ENV MYSQL_DB=library_db
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=
ENV JWT_SECRET=changeThisToASecureRandomStringInProduction2024
ENV CORS_ORIGINS=http://localhost,http://localhost:80
ENV AES_SECRET_KEY=changeThisToA32ByteSecureKeyInProd
ENV ALERT_ENABLED=true
ENV ALERT_ERROR_THRESHOLD=10
ENV API_RATE_LIMIT=200

RUN mkdir -p /app/logs /app/uploads \
    && chown -R appuser:appgroup /app \
    && chmod 755 /app/logs /app/uploads

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
