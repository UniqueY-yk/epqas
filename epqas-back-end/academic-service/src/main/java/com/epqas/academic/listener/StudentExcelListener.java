package com.epqas.academic.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.epqas.academic.dto.ExcelStudentDTO;
import com.epqas.academic.dto.StudentDTO;
import com.epqas.academic.service.StudentService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StudentExcelListener implements ReadListener<ExcelStudentDTO> {

    /**
     * Batch size for processing
     */
    private static final int BATCH_COUNT = 100;

    private List<ExcelStudentDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final StudentService studentService;

    public StudentExcelListener(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public void invoke(ExcelStudentDTO data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Save remaining data
        saveData();
        log.info("All data parsed!");
    }

    private void saveData() {
        log.info("Saving {} student records to database...", cachedDataList.size());
        for (ExcelStudentDTO excelData : cachedDataList) {
            try {
                StudentDTO dto = new StudentDTO();
                dto.setUsername(excelData.getUsername());
                dto.setPassword(excelData.getPassword());
                dto.setRealName(excelData.getRealName());
                dto.setEmail(excelData.getEmail());
                dto.setStudentNumber(excelData.getStudentNumber());
                dto.setClassId(excelData.getClassId());

                // Real implementation should probably have a batch insert method, but calling
                // one by one
                // ensures we hit the UserFeignClient for each to get distinct User IDs
                studentService.createStudentWithUser(dto);
            } catch (Exception e) {
                log.error("Failed to import student {}: {}", excelData.getStudentNumber(), e.getMessage());
            }
        }
        log.info("Save successful!");
    }
}
