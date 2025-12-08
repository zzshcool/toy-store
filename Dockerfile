# 用 Java 21 版本的 Maven 建構環境
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY . .

# 跳過測試打包
RUN mvn clean package -DskipTests

# 第二階段用 OpenJDK 21 來執行 jar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 7788

ENV PORT=7788
ENV JAVA_OPTS="-Xmx500m -Xms64m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]