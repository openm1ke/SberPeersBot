# Используем официальный образ с OpenJDK
FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar-файл вашего приложения в контейнер
COPY target/s21meet-0.0.1-SNAPSHOT.jar /app/s21meet-0.0.1-SNAPSHOT.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "/app/s21meet-0.0.1-SNAPSHOT.jar"]
