package dev.priyme.legitcrystalmacro.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class ConfigSignatureUtil {
   private static final byte[] SECRET_KEY = "CPvP_Macros_v3.5_Integrity_Secret_Key_9f8d7c6b5a".getBytes(StandardCharsets.UTF_8);

   private ConfigSignatureUtil() {
   }

   public static String computeSignature(String payload) {
      try {
         Mac mac = Mac.getInstance("HmacSHA256");
         SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY, "HmacSHA256");
         mac.init(secretKey);
         byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
         return HexFormat.of().formatHex(hmacBytes);
      } catch (Exception var4) {
         throw new RuntimeException("Failed to compute HMAC signature", var4);
      }
   }

   public static boolean verifySignature(String payload, String expectedSignature) {
      if (expectedSignature != null && !expectedSignature.isBlank()) {
         String actualSignature = computeSignature(payload);
         return MessageDigest.isEqual(actualSignature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8));
      } else {
         return false;
      }
   }
}
