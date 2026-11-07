package com.blog.backend.notification.infrastructure.mailservice;

import com.blog.backend.notification.domain.exception.MailDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Configuration
@EnableConfigurationProperties(MailServiceProperties.class)
public class MailServiceClientConfig {

    /** mail-service answers errors as { "error": "..." } and validation errors with an "issues" array. */
    private static final Pattern ERROR = Pattern.compile("\"error\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    @Bean
    public MailServiceApi mailServiceApi(MailServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        // mail-service sends synchronously over SMTP, so allow more than the usual read timeout.
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader("X-Actor", properties.getActor())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw toException(response.getStatusCode(), readBody(response.getBody()));
                })
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(MailServiceApi.class);
    }

    private static MailDeliveryException toException(HttpStatusCode status, String body) {
        String message = extractError(body);
        if (status.value() == 401 || status.value() == 403) {
            log.error("mail-service rejected the API key ({}); check MAIL_SERVICE_API_KEY and its mail:send scope. Response: {}",
                    status.value(), message);
        } else {
            log.error("mail-service returned {}: {}", status.value(), body);
        }
        return new MailDeliveryException("mail-service returned " + status.value()
                + (message != null ? ": " + message : ""));
    }

    private static String readBody(InputStream body) throws IOException {
        return new String(body.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractError(String body) {
        if (body == null) {
            return null;
        }
        Matcher matcher = ERROR.matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\n", " ").replace("\\\\", "\\");
    }
}
