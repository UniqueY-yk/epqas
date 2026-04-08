package com.epqas.academic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.academic.entity.KnowledgePoint;
import com.epqas.academic.mapper.KnowledgePointMapper;
import com.epqas.academic.service.KnowledgePointService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements KnowledgePointService {

    /**
     * 获取知识点分页数据
     * 
     * @param page      页码
     * @param size      每页数量
     * @param courseId  课程ID
     * @param pointName 知识点名称
     * @return 知识点分页数据
     */
    @Override
    public Page<KnowledgePoint> getKnowledgePointsPage(Integer page, Integer size, Integer courseId, String pointName) {
        Page<KnowledgePoint> kpPage = new Page<>(page, size);
        QueryWrapper<KnowledgePoint> queryWrapper = new QueryWrapper<>();

        if (courseId != null) {
            queryWrapper.eq("course_id", courseId);
        }
        if (pointName != null && !pointName.isEmpty()) {
            queryWrapper.like("point_name", pointName);
        }

        return this.page(kpPage, queryWrapper);
    }
}
