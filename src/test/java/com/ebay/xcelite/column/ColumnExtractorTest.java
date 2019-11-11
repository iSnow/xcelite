package com.ebay.xcelite.column;

import com.ebay.xcelite.annotations.Column;
import com.ebay.xcelite.model.CamelCaseXT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withName;

public class ColumnExtractorTest {
    private static String[] colNames = new String[]{
            "CompletelyDifferentColName", "testColNoAnnotationName1", "Name",
            "BirthDate", "Sex"
    } ;
    private static String[] fieldNames = new String[]{
            "surname", "testColNoAnnotationName1", "name",
            "birthDate", "sex"
    } ;

    @Test
    @DisplayName("Test method getAnnotationValue() to return the right names for @Column annotations with or without name")
    void testColumnExtractor_getAnnotationValue() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        List<String> testColNamesList = new ArrayList<>(Arrays.asList(colNames));
        List<String> testFieldNamesList = new ArrayList<>(Arrays.asList(fieldNames));
        CamelCaseXT testObj = new CamelCaseXT();
        ColumnsExtractor ext = new ColumnsExtractor(testObj.getClass());

        Set<java.lang.reflect.Field> fields = ReflectionUtils.getFields(testObj.getClass(), withAnnotation(Column.class));
        Method testMethod = ColumnsExtractor.class.getDeclaredMethod("getAnnotationValue", new Class[] { Column.class, Field.class });
        testMethod.setAccessible(true);

        List<Col> colList = new ArrayList<>();
        for (Field f : fields) {
            Column annotation = f.getAnnotation(Column.class);
            Col col = (Col)testMethod.invoke(ext, new Object[]{annotation, f});
            colList.add(col);
            Assertions.assertNotNull(col);
        }
        Assertions.assertEquals(colNames.length, colList.size());

        List<String> foundColNamesList = new ArrayList<>(
                colList.stream().map(Col::getName).collect(Collectors.toList()));
        foundColNamesList.removeAll(testColNamesList);
        Assertions.assertTrue(foundColNamesList.isEmpty());

        testColNamesList.removeAll(new ArrayList<>(
                colList.stream().map(Col::getName).collect(Collectors.toList())));
        Assertions.assertTrue(testColNamesList.isEmpty());

        List<String> foundFieldNamesList = new ArrayList<>(
                colList.stream().map(Col::getFieldName).collect(Collectors.toList()));
        foundFieldNamesList.removeAll(testFieldNamesList);
        Assertions.assertTrue(foundFieldNamesList.isEmpty());

        testFieldNamesList.removeAll(new ArrayList<>(
                colList.stream().map(Col::getFieldName).collect(Collectors.toList())));
        Assertions.assertTrue(testFieldNamesList.isEmpty());
    }
}
