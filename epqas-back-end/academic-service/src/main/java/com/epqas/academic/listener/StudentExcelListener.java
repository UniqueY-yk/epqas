package com.epqas.academic.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.epqas.academic.dto.ExcelStudentDTO;
import com.epqas.academic.dto.StudentDTO;
import com.epqas.academic.service.StudentService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Excel学生数据监听器
 */
@Slf4j
public class StudentExcelListener implements ReadListener<ExcelStudentDTO> {

    private static final int BATCH_COUNT = 100;

    private List<ExcelStudentDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final StudentService studentService;

    /**
     * 构造函数
     * 
     * @param studentService 学生服务
     */
    public StudentExcelListener(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 覆写invoke方法，处理Excel每一行数据
     * 
     * @param data    Excel学生数据
     * @param context Excel分析上下文
     */
    @Override
    public void invoke(ExcelStudentDTO data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 覆写doAfterAllAnalysed方法，处理Excel所有数据解析完成后的操作
     * 
     * @param context Excel分析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("All data parsed!");
    }

    /**
     * 保存数据
     */
    private void saveData() {
        log.info("Saving {} student records to database...", cachedDataList.size());
        for (ExcelStudentDTO excelData : cachedDataList) {
            try {
                StudentDTO dto = new StudentDTO();
                dto.setUsername(excelData.getUsername());
                dto.setRealName(excelData.getRealName());
                dto.setEmail(excelData.getEmail());
                dto.setClassId(excelData.getClassId());

                // 实际应该有一个批量插入的方法，但是逐个调用
                // 确保我们命中 UserFeignClient 以获取不同的用户 ID
                studentService.createStudentWithUser(dto);
            } catch (Exception e) {
                log.error("Failed to import student {}: {}", excelData.getUsername(), e.getMessage());
            }
        }
        log.info("Save successful!");
    }
}
