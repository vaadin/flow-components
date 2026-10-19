/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.dom.Element;

import tools.jackson.databind.JsonNode;

/**
 * What the client is given of the sheet shown, as {@link SheetClientModel}
 * declares it: nothing until a value is set; each value kept and given to the
 * element; and every property set back to the value the client starts from by
 * {@link SheetClientModel#reset()}, which a sheet's scope runs as the sheet
 * goes.
 */
class SheetClientModelTest {

    private final Element element = new Element("vaadin-spreadsheet");
    private final SheetClientModel model = new SheetClientModel(element);

    /** Every property of the element, as the client would read it. */
    private Map<String, String> onTheElement() {
        Map<String, String> properties = new TreeMap<>();
        element.getPropertyNames().forEach(name -> {
            Object raw = element.getPropertyRaw(name);
            properties.put(name, raw instanceof JsonNode json ? json.toString()
                    : String.valueOf(raw));
        });
        return properties;
    }

    /**
     * Sets every property of the model to something the client does not start
     * from.
     */
    private void setEverything() {
        model.rows.set(40);
        model.cols.set(12);
        model.rowH.set(new float[] { 20, 20 });
        model.colW.set(new int[] { 80 });
        model.defRowH.set(18f);
        model.hiddenRowIndexes.set(new ArrayList<>(List.of(3)));
        model.hiddenColumnIndexes.set(new ArrayList<>(List.of(2)));
        model.verticalSplitPosition.set(2);
        model.horizontalSplitPosition.set(1);
        model.sheetProtected.set(true);
        model.lockFormatColumns.set(true);
        model.lockFormatRows.set(true);
        model.displayGridlines.set(false);
        model.displayRowColHeadings.set(false);
        model.namedRanges.set(List.of("Total"));
        model.colGroupingData.set(List.of(new GroupingData()));
        model.rowGroupingData.set(List.of(new GroupingData()));
        model.colGroupingMax.set(2);
        model.rowGroupingMax.set(3);
        model.colGroupingInversed.set(true);
        model.rowGroupingInversed.set(true);
        model.rowIndexToStyleIndex.set(new HashMap<>(Map.of(1, 2)));
        model.columnIndexToStyleIndex.set(new HashMap<>(Map.of(1, 3)));
        model.lockedColumnIndexes.set(Set.of(1));
        model.lockedRowIndexes.set(Set.of(2));
        model.shiftedCellBorderStyles.set(new ArrayList<>(List.of(".x{}")));
        model.conditionalFormattingStyles
                .set(new HashMap<>(Map.of(0, "color:red")));
        model.mergedRegions.set(new ArrayList<>(List.of(new MergedRegion())));
        model.hyperlinksTooltips.set(new HashMap<>(Map.of("col1 row1", "a")));
        model.cellComments.set(new HashMap<>(Map.of("col1 row1", "note")));
        model.cellCommentAuthors.set(new HashMap<>(Map.of("col1 row1", "me")));
        model.visibleCellComments.set(new ArrayList<>(List.of("col1 row1")));
        model.invalidFormulaCells.set(Set.of("col2 row2"));
        model.overlays.set(new HashMap<>(Map.of("1", new OverlayInfo())));
        model.componentIDtoCellKeysMap
                .set(new HashMap<>(Map.of("7", "col1 row1")));
        model.cellKeysToEditorIdMap
                .set(new HashMap<>(Map.of("col1 row1", "8")));
    }

    @Test
    void declaring_givesTheElementNothing() {
        assertEquals(Map.of(), onTheElement());
    }

    @Test
    void startsFromWhatTheClientStartsFrom() {
        assertEquals(0, model.rows.get());
        assertEquals(15f, model.defRowH.get());
        assertEquals(true, model.displayGridlines.get());
        assertEquals(true, model.displayRowColHeadings.get());
        assertEquals(false, model.sheetProtected.get());
        assertEquals(0, model.colGroupingMax.get());
        assertNull(model.colGroupingData.get());
        assertTrue(model.componentIDtoCellKeysMap.get().isEmpty());
    }

    @Test
    void set_keepsTheValue_andGivesItToTheElement() {
        List<GroupingData> groups = List.of(new GroupingData());

        model.colGroupingData.set(groups);
        model.colGroupingMax.set(2);
        model.sheetProtected.set(true);

        assertSame(groups, model.colGroupingData.get());
        assertEquals(2.0, element.getProperty("colGroupingMax", -1.0));
        assertEquals(true, element.getProperty("sheetProtected", false));
        assertTrue(onTheElement().get("colGroupingData").startsWith("[{"));
    }

    @Test
    void sizes_goAsRunsOfEqualOnes() {
        model.rowH.set(new float[] { 20, 20, 15 });

        assertEquals("{\"runs\":[20.0,2,15.0,1]}", onTheElement().get("rowH"));
    }

    @Test
    void reset_setsEveryPropertyBackToWhatTheClientStartsFrom() {
        setEverything();
        Map<String, String> set = onTheElement();

        model.reset();

        Map<String, String> reset = onTheElement();
        assertEquals(set.keySet(), reset.keySet(), "every property set");
        Map<String, String> expected = new TreeMap<>();
        for (String name : reset.keySet()) {
            expected.put(name, "null");
        }
        expected.putAll(Map.of("rows", "0.0", "cols", "0.0", "defRowH", "15.0",
                "verticalSplitPosition", "0.0", "horizontalSplitPosition",
                "0.0", "colGroupingMax", "0.0", "rowGroupingMax", "0.0"));
        expected.putAll(Map.of("sheetProtected", "false", "lockFormatColumns",
                "false", "lockFormatRows", "false", "displayGridlines", "true",
                "displayRowColHeadings", "true", "colGroupingInversed", "false",
                "rowGroupingInversed", "false"));
        assertEquals(expected, reset);
    }

    @Test
    void reset_emptiesTheComponentsMapHeld() {
        HashMap<String, String> held = new HashMap<>(Map.of("7", "col1 row1"));
        model.componentIDtoCellKeysMap.set(held);

        model.reset();

        assertSame(held, model.componentIDtoCellKeysMap.get());
        assertTrue(held.isEmpty());
    }
}
