package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/*
 * Tests are performed with pure POI and Spreadsheet to find differences and bugs
 */
public class SpreadsheetReadWriteTest {

    @Test
    public void openAndSaveFileWithPOI_emptyXLSXFile_openAndSaveWorks()
            throws IOException {
        File testSheetFIle = TestHelper.getTestSheetFile("empty.xlsx");

        FileInputStream fis = new FileInputStream(testSheetFIle);
        XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis);
        fis.close();

        File tempFile = File.createTempFile("resultEmptyFile", "xlsx");
        FileOutputStream fos = new FileOutputStream(tempFile);
        workbook.write(fos);
        fos.close();
        tempFile.delete();
        // no exceptions, everything ok
    }

    @Test
    public void openAndSaveFile_emptyXLSXFile_openAndSaveWorks()
            throws IOException {
        var sheet = TestHelper.createSpreadsheet("empty.xlsx");

        File tempFile = File.createTempFile("resultEmptyFile", "xlsx");
        FileOutputStream tempOutputStream = new FileOutputStream(tempFile);
        sheet.write(tempOutputStream);
        tempOutputStream.close();
        tempFile.delete();

        // no exceptions, everything ok
    }

    @Test
    public void writeFileMultipleTimes() throws IOException {
        var sheet = TestHelper.createSpreadsheet("empty.xlsx");

        // Write the file
        sheet.write("resultEmptyFile.xlsx");
        // Write the same file again
        var tempFile = sheet.write("resultEmptyFile.xlsx");

        tempFile.delete();
        // no exceptions, everything ok
    }

    @Test
    public void openAndSaveFile_emptyXLSXFile_FileDoesNotContainAdditionalDrawing()
            throws IOException {
        var sheet = TestHelper.createSpreadsheet("empty.xlsx");

        File tempFile = File.createTempFile("resultEmptyFile", "xlsx");
        FileOutputStream tempOutputStream = new FileOutputStream(tempFile);
        sheet.write(tempOutputStream);
        tempOutputStream.close();

        ZipInputStream zipInputStream = new ZipInputStream(
                new FileInputStream(tempFile));

        ZipEntry entry = zipInputStream.getNextEntry();
        do {
            String entryName = entry.getName();
            assertFalse("Empty XLSX contains drawing after import/export: "
                    + entryName, entryName.contains("drawing"));
            entry = zipInputStream.getNextEntry();
        } while (entry != null);

        zipInputStream.close();
        tempFile.delete();
    }

    @Test
    public void openAndSaveFileWithPOI_emptyXLSXFile_FileDoesNotContainAdditionalDrawing()
            throws IOException {
        File testSheetFIle = TestHelper.getTestSheetFile("empty.xlsx");

        FileInputStream fis = new FileInputStream(testSheetFIle);
        XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis);
        fis.close();

        File tempFile = File.createTempFile("resultEmptyFile", "xlsx");
        FileOutputStream fos = new FileOutputStream(tempFile);
        workbook.write(fos);
        fos.close();

        ZipInputStream zipInputStream = new ZipInputStream(
                new FileInputStream(tempFile));

        ZipEntry entry = zipInputStream.getNextEntry();
        do {
            String entryName = entry.getName();
            assertFalse("Empty XLSX contains drawing after import/export: "
                    + entryName, entryName.contains("drawing"));
            entry = zipInputStream.getNextEntry();
        } while (entry != null);

        zipInputStream.close();
        tempFile.delete();
    }
}
