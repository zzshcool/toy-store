FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM ibm-semeru-runtimes:open-21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV PORT=8080
# OpenJ9 Optimized Settings:
# -Xss512k: Reduce thread stack size to 512KB (save memory per thread)
# -Xgcpolicy:gencon: Generational Concurrent GC (standard for most apps, good balance)
# -XX:MaxRAMPercentage=75.0: Respect container memory limits
ENV JAVA_OPTS="-Xss512k -Xgcpolicy:gencon -Xtune:virtualized -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]