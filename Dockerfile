# 使用更完整的基础镜像（包含 bash）
FROM openjdk:21-jdk-slim

WORKDIR /app

# 复制 JAR 文件
COPY target/ultraproject-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8009

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]