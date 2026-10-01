/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.ColumnGroup;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

/**
 * Prototype view for dynamic column grouping (sponsored-development#58).
 */
@Route("spreadsheet-column-grouping-api")
public class ColumnGroupingApiView extends VerticalLayout {

    static final int COLUMNS = 12;

    private final Spreadsheet spreadsheet;
    private final Div groups = new Div();
    private final Div events = new Div();
    private List<ColumnGroup> snapshot = new ArrayList<>();

    public ColumnGroupingApiView() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");
        for (int r = 0; r < 30; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < COLUMNS; c++) {
                row.createCell(c).setCellValue(
                        r == 0 ? "Col " + CellReference.convertNumToColString(c)
                                : String.valueOf(r * 100 + c));
            }
        }

        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSizeFull();
        spreadsheet.setMaxColumns(COLUMNS);
        spreadsheet.setMaxRows(30);
        spreadsheet.addColumnGroupToggleListener(e -> {
            events.add(new Div((e.isFromClient() ? "client: " : "server: ")
                    + format(e.getGroup())));
            updateGroups();
        });

        groups.setId("groups");
        events.setId("events");

        FlexLayout buttons = new FlexLayout(
                button("group-outer", "Group B:G",
                        () -> spreadsheet.addColumnGroup(1, 6)),
                button("group-inner", "Group C:D",
                        () -> spreadsheet.addColumnGroup(2, 3)),
                button("group-last", "Group J:L (last)",
                        () -> spreadsheet.addColumnGroup(9, COLUMNS - 1)),
                button("ungroup-inner", "Ungroup C:D",
                        () -> spreadsheet.removeColumnGroup(2, 3)),
                button("clear", "Clear groups", spreadsheet::clearColumnGroups),
                button("toggle-first", "Toggle first group", () -> {
                    ColumnGroup g = spreadsheet.getColumnGroups().get(0);
                    spreadsheet.setColumnGroupCollapsed(g, !g.collapsed());
                }), button("toggle-last", "Toggle last group", () -> {
                    List<ColumnGroup> all = spreadsheet.getColumnGroups();
                    ColumnGroup g = all.get(all.size() - 1);
                    spreadsheet.setColumnGroupCollapsed(g, !g.collapsed());
                }),
                button("snapshot", "Snapshot state",
                        () -> snapshot = spreadsheet.getColumnGroups()),
                button("restore", "Restore state",
                        () -> snapshot.forEach(g -> spreadsheet
                                .setColumnGroupCollapsed(g, g.collapsed()))),
                button("freeze", "Freeze B",
                        () -> spreadsheet.createFreezePane(1, 2)),
                button("autofit", "Autofit B:L", () -> {
                    for (int c = 1; c < COLUMNS; c++) {
                        spreadsheet.autofitColumn(c);
                    }
                }));
        buttons.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        setSizeFull();
        add(buttons, groups, spreadsheet, events);
        expand(spreadsheet);
        updateGroups();
    }

    private NativeButton button(String id, String text, Runnable action) {
        NativeButton button = new NativeButton(text, e -> {
            action.run();
            updateGroups();
        });
        button.setId(id);
        return button;
    }

    private void updateGroups() {
        groups.setText(spreadsheet.getColumnGroups().stream()
                .map(ColumnGroupingApiView::format)
                .collect(Collectors.joining("; ")));
    }

    private static String format(ColumnGroup g) {
        return CellReference.convertNumToColString(g.firstColumn()) + ":"
                + CellReference.convertNumToColString(g.lastColumn()) + " L"
                + g.level() + (g.collapsed() ? " collapsed" : " expanded");
    }
}
