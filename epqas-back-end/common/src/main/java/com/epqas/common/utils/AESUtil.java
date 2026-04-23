package com.epqas.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256-CBC 加密/解密工具类
 * <p>
 * 与 Python 迁移脚本 (02_encrypt_pii.py) 使用相同的密钥和 IV，
 * 确保 Java 和 Python 加密结果互相兼容。
 * <p>
 * 密钥和 IV 通过 application.yml 中的 encryption.aes.secret-key / iv 配置。
 * 初始化由 {@link AESUtilInitializer} 完成。
 */
public final class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // PKCS5 = PKCS7 for 16-byte blocks

    private static byte[] secretKey;
    private static byte[] iv;
    private static volatile boolean initialized = false;

    private AESUtil() {
        // Utility class, no instantiation
    }

    /**
     * 由 Spring 配置类调用，初始化密钥和 IV（仅执行一次）。
     *
     * @param hexKey 32字节密钥的十六进制字符串 (64个hex字符)
     * @param hexIv  16字节IV的十六进制字符串 (32个hex字符)
     */
    public static void init(String hexKey, String hexIv) {
        if (initialized) {
            return;
        }
        secretKey = hexStringToBytes(hexKey);
        iv = hexStringToBytes(hexIv);
        if (secretKey.length != 32) {
            throw new IllegalArgumentException("AES-256 requires a 32-byte (256-bit) key, got " + secretKey.length);
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("AES CBC IV must be 16 bytes, got " + iv.length);
        }
        initialized = true;
    }

    /**
     * AES-256-CBC 加密，返回 Base64 编码的密文。
     *
     * @param plaintext 明文字符串
     * @return Base64 编码的密文，若输入为 null 则返回 null
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        checkInitialized();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(secretKey, ALGORITHM),
                    new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * AES-256-CBC 解密，接受 Base64 编码的密文。
     *
     * @param ciphertextBase64 Base64 编码的密文
     * @return 解密后的明文字符串，若输入为 null 则返回 null
     */
    public static String decrypt(String ciphertextBase64) {
        if (ciphertextBase64 == null) {
            return null;
        }
        checkInitialized();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(secretKey, ALGORITHM),
                    new IvParameterSpec(iv));
            byte[] decoded = Base64.getDecoder().decode(ciphertextBase64);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "AESUtil has not been initialized. Ensure encryption.aes.secret-key and encryption.aes.iv " +
                    "are configured in application.yml and AESUtilInitializer is loaded.");
        }
    }

    /**
     * 将十六进制字符串转换为字节数组。
     */
    private static byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string: " + hex);
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
