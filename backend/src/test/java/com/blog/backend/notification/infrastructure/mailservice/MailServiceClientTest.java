package com.blog.backend.notification.infrastructure.mailservice;

import com.blog.backend.notification.application.service.MailServiceImpl;
import com.blog.backend.notification.domain.exception.MailDeliveryException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs MailServiceImpl against a fake mail-service (no Spring context, no database). */
class MailServiceClientTest {

    private HttpServer server;
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> actor = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>();
    private volatile int status = 201;
    private volatile String responseBody = "{\"id\":\"1\",\"status\":\"SENT\"}";

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            method.set(exchange.getRequestMethod());
            path.set(exchange.getRequestURI().getPath());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            actor.set(exchange.getRequestHeaders().getFirst("X-Actor"));
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, out.length);
            exchange.getResponseBody().write(out);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private MailServiceImpl service() {
        MailServiceProperties properties = new MailServiceProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api");
        properties.setApiKey("msk_test_abc");
        MailServiceApi api = new MailServiceClientConfig().mailServiceApi(properties);
        return new MailServiceImpl(api);
    }

    @Test
    void sendsPlainTextMailWithBearerKeyAndActor() {
        service().sendMail("khach@example.com", "Tiêu đề", "Nội dung\nxuống dòng");

        assertEquals("POST", method.get());
        assertEquals("/api/mails/send", path.get());
        assertEquals("Bearer msk_test_abc", authorization.get());
        assertEquals("blog-backend", actor.get());
        assertEquals("application/json", contentType.get());
        assertTrue(body.get().contains("\"to\":\"khach@example.com\""), body.get());
        assertTrue(body.get().contains("\"subject\":\"Tiêu đề\""), body.get());
        assertTrue(body.get().contains("\"body\":\"Nội dung\\nxuống dòng\""), body.get());
    }

    @Test
    void validationErrorBecomesMailDeliveryException() {
        status = 400;
        responseBody = "{\"error\":\"Validation failed\",\"issues\":[{\"path\":\"to\",\"message\":\"Must be a valid email address\"}]}";

        MailDeliveryException e = assertThrows(MailDeliveryException.class,
                () -> service().sendMail("sai", "s", "b"));
        assertTrue(e.getMessage().contains("400"), e.getMessage());
        assertTrue(e.getMessage().contains("Validation failed"), e.getMessage());
    }

    @Test
    void smtpFailureBecomesMailDeliveryException() {
        status = 502;
        responseBody = "{\"error\":\"Failed to deliver email via SMTP: ECONNREFUSED\"}";

        MailDeliveryException e = assertThrows(MailDeliveryException.class,
                () -> service().sendMail("a@example.com", "s", "b"));
        assertTrue(e.getMessage().contains("502"), e.getMessage());
    }

    @Test
    void unreachableServiceBecomesMailDeliveryException() {
        MailServiceImpl service = service();
        server.stop(0);

        assertThrows(MailDeliveryException.class, () -> service.sendMail("a@example.com", "s", "b"));
    }
}
