FROM eclipse-temurin:25-jre-alpine

WORKDIR /opt/app

COPY target/Wallet_TZ*.jar /opt/app/wallet.jar

ENV DB_USER=pg_user
ENV DB_PASSWORD=pg_pwd
ENV DB_NAME=pg_table_name
ENV DB_URL=jdbc:postgresql://pgsql:5432/wallet

CMD java -jar /opt/app/wallet.jar --spring.datasource.url=${DB_URL} --spring.datasource.username=${DB_USER} --spring.datasource.password=${DB_PASSWORD}