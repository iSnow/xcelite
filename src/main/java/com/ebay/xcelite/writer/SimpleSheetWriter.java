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
package com.ebay.xcelite.writer;

import com.ebay.xcelite.options.XceliteOptions;
import com.ebay.xcelite.sheet.XceliteSheet;
import com.ebay.xcelite.styles.CellStylesBank;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An concrete implementation of the {@link SheetWriter} interface that writes
 * 2-dimensional collections of simple, unannotated Java beans to Excel sheets.
 * Each entry in the outer collection represents one row in the spreadsheet
 * while each entry in the inner collection represents one cell (column) in the row.
 *
 * This writer class does not write a header row, as no column names are defined.
 *
 * Preferably, this class should not directly be instantiated, but you should
 * call {@link XceliteSheet#getSimpleWriter()}
 *
 * By default, a SheetWriter copies over the {@link com.ebay.xcelite.options.XceliteOptions options}
 * from the sheet it is constructed on. By this, the {@link com.ebay.xcelite.sheet.XceliteSheet}
 * become the default options, but the SheetWriter can modify option properties locally. However,
 * the user may use the {@link com.ebay.xcelite.writer.AbstractSheetWriter#AbstractSheetWriter(XceliteSheet,
 * com.ebay.xcelite.options.XceliteOptions) SimpleSheetWriter(XceliteSheet, XceliteOptions)}
 * constructor to use - for one writer only - a completely different set of options from
 * the sheet options.
 *
 * @author kharel (kharel@ebay.com)
 * @since 1.0
 * created Nov 10, 2013
 */
public class SimpleSheetWriter extends AbstractSheetWriter<Collection<Object>> {

    //TODO version 2.x remove "public"
    public SimpleSheetWriter(XceliteSheet sheet) {
        super(sheet);
        sheet.getOptions().setGenerateHeaderRow(false);
    }

    /**
     * Construct a {@link SimpleSheetWriter} on the given {@link XceliteSheet sheet} using
     * the given {@link XceliteOptions options}. Values from the options parameter
     * are copied over, later changes to the options object will not affect the
     * options of this writer.
     * @param sheet the sheet to construct the SimpleSheetWriter on.
     * @param options options for this SimpleSheetWriter.
     */
    SimpleSheetWriter(XceliteSheet sheet, XceliteOptions options) {
        super(sheet, options);
    }

    @Override
    public void write(Collection<Collection<Object>> data) {
        CellStyle boldStyle = CellStylesBank.get(sheet.getNativeSheet().getWorkbook()).getBoldStyle();
        final AtomicInteger i = new AtomicInteger(0);

        data.forEach(row -> {
            Row excelRow = sheet.getNativeSheet().createRow(i.intValue());
            final AtomicInteger j = new AtomicInteger(0);
            row.forEach(column -> {
                Cell cell = excelRow.createCell(j.intValue());
                if (options.isGenerateHeaderRow() && i.intValue() == 0) {
                    cell.setCellStyle(boldStyle);
                }
                writeToCell(cell, column, null);
                j.incrementAndGet();
            });
            i.incrementAndGet();
        });
    }
}
