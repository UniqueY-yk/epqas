package com.epqas.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.academic.entity.KnowledgePoint;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface KnowledgePointService extends IService<KnowledgePoint> {
    /**
     * 获取知识点分页数据
     * 
     * @param page      页码
     * @param size      每页数量
     * @param courseId  课程ID
     * @param pointName 知识点名称
     * @return 知识点分页数据
     */
    Page<KnowledgePoint> getKnowledgePointsPage(Integer page, Integer size, Integer courseId, String pointName);
}
