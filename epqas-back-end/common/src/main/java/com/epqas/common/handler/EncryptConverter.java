package com.epqas.common.handler;

import com.epqas.common.utils.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter：对 String 类型字段透明加解密。
 * <p>
 * INSERT/UPDATE 时自动加密明文为 Base64 密文；
 * SELECT 时自动解密 Base64 密文为明文。
 * <p>
 * 如果解密失败（例如数据尚未加密或密钥不匹配），将返回原始值而不是抛出异常。
 * <p>
 * 使用方式：在实体字段上标注
 * {@code @Convert(converter = EncryptConverter.class)}
 */
@Converter
public class EncryptConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return AESUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return AESUtil.decrypt(dbData);
        } catch (Exception e) {
            // Decryption failed — return raw value (likely unencrypted legacy data)
            return dbData;
        }
    }
}
