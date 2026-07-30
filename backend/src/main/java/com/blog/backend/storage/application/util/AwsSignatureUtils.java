package com.blog.backend.storage.application.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class AwsSignatureUtils {

    private AwsSignatureUtils() {
        // Private constructor to prevent instantiation
    }

    public static byte[] hmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        return hmacSHA256("aws4_request", kService);
    }

    public static String calculateSignatureV4(String base64Policy, String dateStamp, String secretKey, String regionName, String serviceName) throws Exception {
        byte[] signatureKey = getSignatureKey(secretKey, dateStamp, regionName, serviceName);
        byte[] signatureBytes = hmacSHA256(base64Policy, signatureKey);

        StringBuilder signatureHex = new StringBuilder();
        for (byte b : signatureBytes) {
            signatureHex.append(String.format("%02x", b));
        }
        return signatureHex.toString();
    }
}
