package com.blog.backend.storage.infrastructure.fileservice;

import com.blog.backend.storage.domain.exception.FileServiceException;
import com.blog.backend.storage.domain.exception.StorageErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties(FileServiceProperties.class)
public class FileServiceClientConfig {

    private static final Pattern MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    @Bean
    public FileServiceApi fileServiceApi(FileServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("x-api-key", properties.getApiKey())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw toException(response.getStatusCode(), readBody(response.getBody()));
                })
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(FileServiceApi.class);
    }

    private static FileServiceException toException(HttpStatusCode status, String body) {
        String message = extractMessage(body);
        if (status.value() == 401) {
            log.error("file-service rejected the API key; check FILE_SERVICE_API_KEY. Response: {}", message);
            return new FileServiceException(StorageErrorCode.FILE_SERVICE_MISCONFIGURED);
        }
        if (status.is4xxClientError()) {
            return new FileServiceException(StorageErrorCode.INVALID_UPLOAD,
                    message != null ? message : StorageErrorCode.INVALID_UPLOAD.getMessage());
        }
        log.error("file-service returned {}: {}", status.value(), body);
        return new FileServiceException(StorageErrorCode.FILE_SERVICE_UNAVAILABLE);
    }

    private static String readBody(InputStream body) throws IOException {
        return new String(body.readAllBytes(), StandardCharsets.UTF_8);
    }

    /** Pulls "message" out of file-service's JSON error body without depending on a JSON mapper. */
    private static String extractMessage(String body) {
        if (body == null) {
            return null;
        }
        Matcher matcher = MESSAGE.matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\n", " ")
                .replace("\\/", "/")
                .replace("\\\\", "\\");
    }
}
