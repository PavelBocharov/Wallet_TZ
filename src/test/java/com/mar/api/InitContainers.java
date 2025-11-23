package com.mar.api;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public class InitContainers {
    // Application
    public static final String APP_ALIAS = "wallet_app";
    public static final int APP_MAPPED_PORT = 8080;
    // PostgreSQL
    public static final String DB_ALIAS = "pgsql";
    public static final int DB_MAPPED_PORT = 5432;
    public static final String DB_LOGIN = "test_user_login";
    public static final String DB_PWD = "test_user_pwd";
    public static final String DB_NAME = "wallet";

    protected static Network bridge = Network.newNetwork();
    protected static HttpClient httpClient = HttpClient.newHttpClient();

    @Container
    protected static GenericContainer<?> pgsql = new GenericContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withExposedPorts(DB_MAPPED_PORT)
            .withNetwork(bridge)
            .withNetworkAliases(DB_ALIAS)
            .withEnv(Map.of(
                    "POSTGRES_USER", DB_LOGIN,
                    "POSTGRES_PASSWORD", DB_PWD,
                    "POSTGRES_DB", DB_NAME
            ));
    @Container
    protected static GenericContainer<?> app = new GenericContainer<>(
            new ImageFromDockerfile("wallet/test/" + Base58.randomString(16).toLowerCase(), true)
                    .withDockerfile(Path.of("./Dockerfile"))
    )
            .withExposedPorts(APP_MAPPED_PORT)
            .withNetwork(bridge)
            .withNetworkAliases(APP_ALIAS)
            .withEnv(Map.of(
                    "DB_USER", DB_LOGIN,
                    "DB_PASSWORD", DB_PWD,
                    "DB_NAME", DB_NAME,
                    "DB_URL", "jdbc:postgresql://" + DB_ALIAS + ":" + DB_MAPPED_PORT + "/" + DB_NAME
            ))
            .dependsOn(pgsql)
            .waitingFor(
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .forPort(APP_MAPPED_PORT)
                            .withReadTimeout(Duration.ofSeconds(10))
            );

}
