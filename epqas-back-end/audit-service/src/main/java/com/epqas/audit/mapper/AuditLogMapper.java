package com.epqas.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
