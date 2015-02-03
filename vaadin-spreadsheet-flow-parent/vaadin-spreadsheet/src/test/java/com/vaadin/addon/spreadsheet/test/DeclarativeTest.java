package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.ui.declarative.Design;

/**
 * Tests the declarative support of Spreadsheet.
 * 
 * @author Vaadin Ltd.
 */
public class DeclarativeTest {

    @Test
    public void testSpreadsheetToFromDesign() throws Exception {
        File file = null;
        URL resource = null;
        try {
            ClassLoader classLoader = DeclarativeTest.class.getClassLoader();
            resource = classLoader.getResource("test_sheets" + File.separator
                    + "formulasheet.xlsx");
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Spreadsheet s = new Spreadsheet(file);
        s.setDefaultColumnWidth(500);
        s.setDefaultRowHeight(18);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Design.write(s, bos);

        Spreadsheet result = (Spreadsheet) Design
                .read(new ByteArrayInputStream(bos.toByteArray()));
        assertEquals(s.getDefaultColumnWidth(), result.getDefaultColumnWidth());
        assertEquals(s.getDefaultRowHeight(), result.getDefaultRowHeight(), 0);
    }

}
