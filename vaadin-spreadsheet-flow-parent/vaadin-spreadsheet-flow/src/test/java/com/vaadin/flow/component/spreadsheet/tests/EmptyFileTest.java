package com.vaadin.flow.component.spreadsheet.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class EmptyFileTest {

    @Test
    public void loadFile_emptySheet_firstRowRendered() {
        var s = TestHelper.createSpreadsheet("empty.xlsx");

        var rowH = getSpreadsheetRowH(s);

        Assert.assertTrue("Row heights not sent to client", rowH != null);

        for (int i = 0; i < rowH.length; i++) {
            Assert.assertTrue("Row is zero height, should be default",
                    rowH[i] > 0);
        }

    }

    private float[] getSpreadsheetRowH(Spreadsheet s) {
        Method method = null;
        try {
            method = s.getClass().getDeclaredMethod("getRowH");
            method.setAccessible(true);
            Object val = method.invoke(s);
            return (float[]) val;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            if (method != null) {
                method.setAccessible(false);
            }

        }
        Assert.fail("Could not get RowH with reflection");
        return null;
    }
}
