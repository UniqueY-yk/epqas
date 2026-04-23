package com.epqas.common.handler;

import com.epqas.common.utils.AESUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler：对 String 类型字段透明加解密。
 * <p>
 * INSERT/UPDATE 时自动加密明文为 Base64 密文；
 * SELECT 时自动解密 Base64 密文为明文。
 * <p>
 * 如果解密失败（例如数据尚未加密或密钥不匹配），将返回原始值而不是抛出异常。
 * <p>
 * 注意：不使用 @MappedTypes(String.class)，避免全局拦截所有 String 字段。
 * 仅通过 @TableField(typeHandler = EncryptTypeHandler.class) 显式绑定到目标字段。
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, AESUtil.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decryptSafely(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decryptSafely(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decryptSafely(value);
    }

    /**
     * 安全解密：如果解密失败，返回原始值（兼容未加密的历史数据）。
     */
    private String decryptSafely(String value) {
        if (value == null) {
            return null;
        }
        try {
            return AESUtil.decrypt(value);
        } catch (Exception e) {
            // Decryption failed — return raw value (likely unencrypted legacy data)
            return value;
        }
    }
}
