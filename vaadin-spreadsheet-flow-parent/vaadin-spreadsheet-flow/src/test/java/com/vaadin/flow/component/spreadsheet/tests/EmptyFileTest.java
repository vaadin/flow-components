/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

class EmptyFileTest {

    @Test
    void loadFile_emptySheet_firstRowRendered() {
        var s = TestHelper.createSpreadsheet("empty.xlsx");

        var rowH = getSpreadsheetRowH(s);

        Assertions.assertTrue(rowH != null, "Row heights not sent to client");

        for (int i = 0; i < rowH.length; i++) {
            Assertions.assertTrue(rowH[i] > 0,
                    "Row is zero height, should be default");
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
        Assertions.fail("Could not get RowH with reflection");
        return null;
    }
}
