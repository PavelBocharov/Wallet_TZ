package com.mar.api;

import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.api.dto.WalletOperations;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

import static com.mar.utils.TestUtils.fromJson;
import static com.mar.utils.TestUtils.getJson;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@Disabled("Full positive test. Need docker.")
@Testcontainers
class WalletControllerIntegrationTest extends InitContainers {

    @Test
    void initContainers_test() {
        assertAll(
                () -> assertTrue(pgsql.isCreated()),
                () -> assertTrue(app.isCreated())
        );
        app.followOutput(new Slf4jLogConsumer(log).withPrefix("initContainers_test"));

        String appHost = app.getHost();
        int port = app.getMappedPort(APP_MAPPED_PORT);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/actuator/health", appHost, port)))
                .GET()
                .build();

        await().untilAsserted(() -> {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("Init containers. Status Code: {}", response.statusCode());
            log.debug("Init containers. Headers: {}", response.headers().map());
            log.debug("Init containers. Response Body: {}", response.body());

            assertEquals(200, response.statusCode());
        });
    }

    @SneakyThrows
    @Test
    void positive_test() {
        assertAll(
                () -> assertTrue(pgsql.isCreated()),
                () -> assertTrue(app.isCreated())
        );
        app.followOutput(new Slf4jLogConsumer(log).withPrefix("positive_test"));

        String appHost = app.getHost();
        int port = app.getMappedPort(APP_MAPPED_PORT);
        // Create wallets
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(10, 100));
        String rqJson = getJson(WalletDto.builder().amount(amount).build());
        log.debug("Create wallet RQ: {}", rqJson);

        HttpRequest rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallet", appHost, port)))
                .POST(HttpRequest.BodyPublishers.ofString(rqJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        HttpResponse<String> rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("Create wallet. Status Code: {}", rs.statusCode());
        log.debug("Create wallet. Headers: {}", rs.headers().map());
        log.debug("Create wallet. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        WalletDto rsBody = fromJson(rs.body(), WalletDto.class);
        assertNotNull(rsBody);
        assertNotNull(rsBody.getId());
        assertEquals(amount, rsBody.getAmount());

        // Get all wallets
        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallets", appHost, port)))
                .GET()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());


        log.debug("Get all wallets. Status Code: {}", rs.statusCode());
        log.debug("Get all wallets. Headers: {}", rs.headers().map());
        log.debug("Get all wallets. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        WalletDto[] walletDtos = fromJson(rs.body(), WalletDto[].class);
        assertNotNull(walletDtos);
        assertEquals(1, walletDtos.length);
        assertEquals(rsBody, walletDtos[0]);

        // Get by ID
        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallets/" + rsBody.getId(), appHost, port)))
                .GET()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("Get wallet by ID. Status Code: {}", rs.statusCode());
        log.debug("Get wallet by ID. Headers: {}", rs.headers().map());
        log.debug("Get wallet by ID. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        WalletDto getByIdRS = fromJson(rs.body(), WalletDto.class);

        assertNotNull(getByIdRS);
        assertEquals(getByIdRS, rsBody);

        // Update amount = DEPOSIT
        BigDecimal newAmount = rsBody.getAmount().add(BigDecimal.TEN);
        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallet", appHost, port)))
                .PUT(HttpRequest.BodyPublishers.ofString(
                        getJson(UpdateWallet.builder()
                                .walletId(rsBody.getId())
                                .operationType(WalletOperations.DEPOSIT)
                                .amount(BigDecimal.TEN)
                                .build())
                ))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("DEPOSIT amount. Status Code: {}", rs.statusCode());
        log.debug("DEPOSIT amount. Headers: {}", rs.headers().map());
        log.debug("DEPOSIT amount. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        WalletDto addAmount = fromJson(rs.body(), WalletDto.class);

        assertNotNull(addAmount);
        assertEquals(addAmount.getId(), rsBody.getId());
        assertEquals(addAmount.getAmount(), newAmount);
        assertTrue(addAmount.getAmount().compareTo(rsBody.getAmount()) > 0);

        // Update amount = WITHDRAW
        newAmount = addAmount.getAmount().subtract(BigDecimal.ONE);
        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallet", appHost, port)))
                .PUT(HttpRequest.BodyPublishers.ofString(
                        getJson(UpdateWallet.builder()
                                .walletId(rsBody.getId())
                                .operationType(WalletOperations.WITHDRAW)
                                .amount(BigDecimal.ONE)
                                .build())
                ))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("WITHDRAW amount. Status Code: {}", rs.statusCode());
        log.debug("WITHDRAW amount. Headers: {}", rs.headers().map());
        log.debug("WITHDRAW amount. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        WalletDto minusAmount = fromJson(rs.body(), WalletDto.class);

        assertNotNull(minusAmount);
        assertEquals(minusAmount.getId(), rsBody.getId());
        assertEquals(minusAmount.getId(), addAmount.getId());
        assertEquals(minusAmount.getAmount(), newAmount);
        assertTrue(minusAmount.getAmount().compareTo(addAmount.getAmount()) < 0);

        // Remove wallet
        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallets/" + rsBody.getId(), appHost, port)))
                .DELETE()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("REMOVE wallet by ID. Status Code: {}", rs.statusCode());
        log.debug("REMOVE wallet by ID. Headers: {}", rs.headers().map());
        log.debug("REMOVE wallet by ID. Response Body: {}", rs.body());

        rq = HttpRequest.newBuilder()
                .uri(URI.create(format("http://%s:%d/api/v1/wallets", appHost, port)))
                .GET()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());

        log.debug("Get all after REMOVE. Status Code: {}", rs.statusCode());
        log.debug("Get all after REMOVE. Headers: {}", rs.headers().map());
        log.debug("Get all after REMOVE. Response Body: {}", rs.body());

        assertEquals(200, rs.statusCode());

        walletDtos = fromJson(rs.body(), WalletDto[].class);
        assertNotNull(walletDtos);
        assertEquals(0, walletDtos.length);
    }

}