# 厂智访客 · 后端镜像（多阶段构建）
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY fv-common/pom.xml fv-common/
COPY fv-notify/pom.xml fv-notify/
COPY fv-system/pom.xml fv-system/
COPY fv-visitor/pom.xml fv-visitor/
COPY fv-steel/pom.xml fv-steel/
COPY fv-device/pom.xml fv-device/
COPY fv-ops/pom.xml fv-ops/
COPY fv-bootstrap/pom.xml fv-bootstrap/
# 预拉依赖（可选，失败不阻断）
RUN mvn -q -B dependency:go-offline || true
COPY . .
RUN mvn -q -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/fv-bootstrap/target/fv-server.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
