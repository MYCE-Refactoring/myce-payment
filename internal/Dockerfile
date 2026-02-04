FROM eclipse-temurin:21-jre

## 실행 경로
WORKDIR /app/myce-backend/payment

## 실행 파일
COPY ./internal/build/libs/*.jar myce-payment.jar

## 실행 포트
EXPOSE 8023

ENTRYPOINT ["java", "-jar", "myce-payment.jar", "--spring.profiles.active=product"]