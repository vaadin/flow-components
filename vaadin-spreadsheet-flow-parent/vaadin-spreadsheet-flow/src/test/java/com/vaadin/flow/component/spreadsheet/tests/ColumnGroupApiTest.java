/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.ColumnGroup;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.ColumnGroupToggleEvent;

class ColumnGroupApiTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0).setCellValue("x");
        spreadsheet = new Spreadsheet(workbook);
    }

    @Test
    void addNested_readGroups() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        Assertions.assertEquals(
                List.of(new ColumnGroup(1, 6, 1, false),
                        new ColumnGroup(2, 3, 2, false)),
                spreadsheet.getColumnGroups());
    }

    @Test
    void collapseInner_collapseOuter_expandOuter_innerStaysCollapsed() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        collapse(2, 3, 2, true);
        collapse(1, 6, 1, true);
        assertHidden(1, 2, 3, 4, 5, 6);

        collapse(1, 6, 1, false);
        assertHidden(2, 3);
        Assertions.assertEquals(
                List.of(new ColumnGroup(1, 6, 1, false),
                        new ColumnGroup(2, 3, 2, true)),
                spreadsheet.getColumnGroups());
    }

    @Test
    void restoreNestedStates_anyOrder() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        List<ColumnGroup> saved = List.of(new ColumnGroup(1, 6, 1, true),
                new ColumnGroup(2, 3, 2, true));
        // outer first, then inner that is hidden by the collapsed outer
        saved.forEach(g -> spreadsheet.setColumnGroupCollapsed(g, true));
        Assertions.assertEquals(saved, spreadsheet.getColumnGroups());
        assertHidden(1, 2, 3, 4, 5, 6);
    }

    @Test
    void groupEndingAtLastColumn_collapseAndExpand() {
        int last = 16383;
        spreadsheet.addColumnGroup(last - 3, last);
        collapse(last - 3, last, 1, true);
        Assertions.assertTrue(spreadsheet.getColumnGroups().get(0).collapsed());
        Assertions.assertTrue(spreadsheet.isColumnHidden(last));
        collapse(last - 3, last, 1, false);
        Assertions.assertFalse(spreadsheet.isColumnHidden(last));
    }

    @Test
    void clear_showsColumns() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        collapse(1, 6, 1, true);
        spreadsheet.clearColumnGroups();
        Assertions.assertTrue(spreadsheet.getColumnGroups().isEmpty());
        assertHidden();
    }

    @Test
    void collapse_clear_regroup_restore() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        collapse(2, 3, 2, true);
        collapse(1, 6, 1, true);
        List<ColumnGroup> saved = spreadsheet.getColumnGroups();
        spreadsheet.clearColumnGroups();

        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        saved.forEach(
                g -> spreadsheet.setColumnGroupCollapsed(g, g.collapsed()));
        Assertions.assertEquals(saved, spreadsheet.getColumnGroups());
        assertHidden(1, 2, 3, 4, 5, 6);
    }

    @Test
    void refresh_keepsMaxColumns() {
        spreadsheet.setMaxColumns(12);
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.clearColumnGroups();
        Assertions.assertEquals(12, spreadsheet.getColumns());
    }

    @Test
    void ungroupInner_keepsOuter() {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        collapse(2, 3, 2, true);
        spreadsheet.removeColumnGroup(2, 3);
        Assertions.assertEquals(List.of(new ColumnGroup(1, 6, 1, false)),
                spreadsheet.getColumnGroups());
        assertHidden();
    }

    @Test
    void maxLevel_throws() {
        for (int i = 0; i < 7; i++) {
            spreadsheet.addColumnGroup(1, 20 - i);
        }
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.addColumnGroup(1, 2));
    }

    @Test
    void toggle_firesEvent() {
        List<ColumnGroupToggleEvent> events = new ArrayList<>();
        spreadsheet.addColumnGroupToggleListener(events::add);
        spreadsheet.addColumnGroup(1, 6);
        collapse(1, 6, 1, true);
        Assertions.assertEquals(1, events.size());
        Assertions.assertTrue(events.get(0).isCollapsed());
        Assertions.assertFalse(events.get(0).isFromClient());
    }

    @Test
    void collapsedState_survivesSaveAndReload() throws Exception {
        spreadsheet.addColumnGroup(1, 6);
        spreadsheet.addColumnGroup(2, 3);
        collapse(2, 3, 2, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        spreadsheet.getWorkbook().write(out);
        Spreadsheet reloaded = new Spreadsheet(
                new ByteArrayInputStream(out.toByteArray()));
        Assertions.assertEquals(spreadsheet.getColumnGroups(),
                reloaded.getColumnGroups());
    }

    private void collapse(int first, int last, int level, boolean collapsed) {
        spreadsheet.setColumnGroupCollapsed(
                new ColumnGroup(first, last, level, !collapsed), collapsed);
    }

    private void assertHidden(int... columns) {
        List<Integer> expected = new ArrayList<>();
        for (int c : columns) {
            expected.add(c);
        }
        for (int c = 0; c < 12; c++) {
            Assertions.assertEquals(expected.contains(c),
                    spreadsheet.isColumnHidden(c), "column " + c);
        }
    }
}
