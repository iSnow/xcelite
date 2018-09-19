/*
  Copyright [2013-2014] eBay Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ebay.xcelite.reader;

import com.ebay.xcelite.exceptions.EmptyRowException;
import com.ebay.xcelite.options.XceliteOptions;
import com.ebay.xcelite.sheet.XceliteSheet;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of the {@link SheetReader} interface. Extending
 * classes must override {@link SheetReader#read()}
 *
 * By default, a SheetReader copies over the {@link XceliteOptions options} from the
 * {@link com.ebay.xcelite.sheet.XceliteSheet} it is constructed on. This means the
 * options set on the sheet become the default options for the SheetReader, but it can
 * modify option properties locally. However, the user may use the
 * {@link #AbstractSheetReader(XceliteSheet, XceliteOptions)} constructor to
 * use - for one reader only - a completely different set of options.
 *
 * @author kharel (kharel@ebay.com)
 * @since 1.0
 * created Nov 11, 2013
 */

@Getter
public abstract class AbstractSheetReader<T> implements SheetReader<T> {
    protected final XceliteSheet sheet;
    final List<RowPostProcessor<T>> rowPostProcessors;

    protected XceliteOptions options;

    //TODO version 2.x remove if possible
    public AbstractSheetReader(XceliteSheet sheet) {
        this (sheet, new XceliteOptions());
    }

    public AbstractSheetReader(XceliteSheet sheet, XceliteOptions options) {
        this.sheet = sheet;
        this.options = new XceliteOptions(options);
        rowPostProcessors = new ArrayList<>();
    }

    /**
     * @deprecated since 1.2 use the constructor using {@link XceliteOptions}
     * and set {@link XceliteOptions#setFirstDataRowIndex(Integer) setFirstDataRowIndex}
     * to 1
     * @param sheet The sheet to read from
     * @param skipHeaderRow whether or not one header row should be skipped
     */
    @Deprecated
    public AbstractSheetReader(XceliteSheet sheet, boolean skipHeaderRow) {
        this (sheet);
        skipHeaderRow (skipHeaderRow);
    }

    public static Object readValueFromCell(Cell cell) {
        if (cell == null) return null;
        Object cellValue = null;
        switch (cell.getCellTypeEnum()) {
            case ERROR:
                break;
            case FORMULA:
                // Get the type of Formula
                switch (cell.getCachedFormulaResultTypeEnum()) {
                    case ERROR:
                        cellValue = null;
                        break;
                    case STRING:
                        cellValue = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        cellValue = cell.getNumericCellValue();
                        break;
                    case BOOLEAN:
                        cellValue = cell.getBooleanCellValue();
                        break;
                    default:
                        cellValue = cell.getStringCellValue();
                }
                break;
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                cellValue = cell.getNumericCellValue();
                break;
            default:
                cellValue = cell.getStringCellValue();
        }
        return cellValue;
    }

    /**
     * @deprecated since 1.2. Use {@link #getOptions()} instead and set
     * {@link XceliteOptions#setFirstDataRowIndex(Integer) setFirstDataRowIndex}
     * to 1
     * @param skipHeaderRow true to skip the header row, false otherwise
     */
    @Override
    @Deprecated
    public void skipHeaderRow(boolean skipHeaderRow) {
        if (skipHeaderRow)
            options.setFirstDataRowIndex(1);
    }

    @Override
    public void addRowPostProcessor(RowPostProcessor<T> rowPostProcessor) {
        rowPostProcessors.add(rowPostProcessor);
    }

    @Override
    public void removeRowPostProcessor(RowPostProcessor<T> rowPostProcessor) {
        rowPostProcessors.remove(rowPostProcessor);
    }

    boolean isBlankRow(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();
        boolean blankRow = true;
        while (cellIterator.hasNext()) {
            Object value = readValueFromCell(cellIterator.next());
            if (blankRow && value != null && !String.valueOf(value).isEmpty()) {
                blankRow = false;
            }
        }
        return blankRow;
    }

    boolean shouldKeepObject(T object, List<RowPostProcessor<T>> rowPostProcessors) {
        boolean keepObject = true;

        for (RowPostProcessor<T> rowPostProcessor: rowPostProcessors) {
            keepObject = rowPostProcessor.process(object);
            if (!keepObject) break;
        }

        return keepObject;
    }

    Collection<T> applyTrailingEmptyRowPolicy(List<T> data, int lastNonEmptyRowId) {
        if (lastNonEmptyRowId == data.size())
            return data;
        switch (options.getTrailingEmptyRowPolicy()) {
            case SKIP:
                return data.subList(0, lastNonEmptyRowId);
            case THROW:
                throw new EmptyRowException("Trailing empty rows found and TrailingEmptyRowPolicy.THROW active");
            case NULL:
                for (int i = lastNonEmptyRowId+1; i < data.size(); i++) {
                    data.set(i, null);
                }
                return data;
        }
        return data;
    }

    public void setOptions(XceliteOptions options) {
        this.options = new XceliteOptions(options);
    }
}
