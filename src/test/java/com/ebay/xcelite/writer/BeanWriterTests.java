package com.ebay.xcelite.writer;

import com.ebay.xcelite.model.BeanWriterTestsBean;
import com.ebay.xcelite.writer.AbstractTestBaseForWriterTests;
import lombok.SneakyThrows;
import org.apache.poi.ss.SpreadsheetVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class BeanWriterTests extends AbstractTestBaseForWriterTests {
    private static BeanWriterTestsBean bean = new BeanWriterTestsBean();

    @BeforeAll
    @SneakyThrows
    static void setup() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SpreadsheetVersion.EXCEL2007.getMaxTextLength(); i++) {
            sb.append("a");
        }
        bean.setLongString(sb.toString());
        setup(bean);
    }

    @Test
    @DisplayName("Must correctly write 32KB strings (max length of Excel 2007 format)")
    void mustWriteLongStringsOK() {
        List<Map<String, Object>> rowList = extractCellValues (workbook);
        Map<String, Object> columnsMap = rowList.get(0);
        String val = bean.getLongString();
        Object obj = columnsMap.get("LONG_STRING");
        Assertions.assertEquals(val, obj);
    }


}
