package com.blog.be.storage.application.util;

import com.blog.be.storage.domain.constant.StorageConstants;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

public final class StorageUtils {

    private StorageUtils() {
        // Private constructor to prevent instantiation
    }

    public static String extractKey(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("/")) {
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        return fileUrl;
    }

    public record AwsTimeInfo(String amzDate, String dateStamp, String expiration) {}

    public static AwsTimeInfo prepareAwsTimeFormat() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String dateStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String expiration = now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        return new AwsTimeInfo(amzDate, dateStamp, expiration);
    }

    public static String generateObjectKey(String folder, String fileName, String tempFolderPrefix) {
        String safeFolder = (folder != null && !folder.isBlank()) ? folder.toLowerCase() : "others";
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        return String.format("%s%s/%s-%s", tempFolderPrefix, safeFolder, UUID.randomUUID(), safeFileName);
    }

    public static String buildPolicyJson(String expiration, String objectKey, String contentType, String credential, String amzDate, String bucketName, long maxFileSizeBytes, String algorithm) {
        return String.format(
                "{\n" +
                        "  \"expiration\": \"%s\",\n" +
                        "  \"conditions\": [\n" +
                        "    {\"bucket\": \"%s\"},\n" +
                        "    {\"key\": \"%s\"},\n" +
                        "    {\"Content-Type\": \"%s\"},\n" +
                        "    [\"content-length-range\", 0, %d],\n" +
                        "    {\"x-amz-credential\": \"%s\"},\n" +
                        "    {\"x-amz-algorithm\": \"%s\"},\n" +
                        "    {\"x-amz-date\": \"%s\"}\n" +
                        "  ]\n" +
                        "}",
                expiration, bucketName, objectKey, contentType, maxFileSizeBytes, credential, algorithm, amzDate
        );
    }

    public static String encodePolicyToBase64(String policyJson) {
        return Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
    }
}
