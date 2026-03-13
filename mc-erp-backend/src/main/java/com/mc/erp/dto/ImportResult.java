package com.mc.erp.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果
 */
@Data
public class ImportResult {
    private int successCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();

    public void addError(int rowNum, String message) {
        errors.add("第" + rowNum + "行: " + message);
        errorCount++;
    }
}
