package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.KnowledgePoint;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface KnowledgePointService extends IService<KnowledgePoint> {
    Page<KnowledgePoint> getKnowledgePointsPage(Integer page, Integer size, Integer courseId, String pointName);
}
