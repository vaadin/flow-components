/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.dom.Element;

/**
 * What the client is given of the sheet shown, as properties of the element:
 * each one declared here, by its name on the wire, with the value the client
 * starts from. It lives with the sheet's scope, and {@link #reset()}, which the
 * scope runs when the sheet is no longer shown, sets every one of them back:
 * nothing of a sheet is left on the client for the next, whatever its loading
 * sets or leaves out.
 * <p>
 * Each value is kept as the server last set it, and read back from here: the
 * server's helpers read the outline, the frozen panes and the style maps of the
 * sheet shown from what the client was given of them.
 */
final class SheetClientModel implements Serializable {

    /** How many rows and columns the sheet shows. */
    final ClientProperty<Integer> rows;
    final ClientProperty<Integer> cols;

    /** The heights of the rows, sent as runs of equal ones. */
    final ClientProperty<float[]> rowH;
    /** The widths of the columns, sent as runs of equal ones. */
    final ClientProperty<int[]> colW;
    /**
     * The height of a row the sheet gives none, in points: 15 on a client not
     * yet given one.
     */
    final ClientProperty<Float> defRowH;
    /** The rows and columns hidden, counted from 1. */
    final ClientProperty<ArrayList<Integer>> hiddenRowIndexes;
    final ClientProperty<ArrayList<Integer>> hiddenColumnIndexes;

    /** How many rows and columns are frozen. */
    final ClientProperty<Integer> verticalSplitPosition;
    final ClientProperty<Integer> horizontalSplitPosition;

    /** Whether the sheet is protected, and what its protection locks. */
    final ClientProperty<Boolean> sheetProtected;
    final ClientProperty<Boolean> lockFormatColumns;
    final ClientProperty<Boolean> lockFormatRows;

    /** Whether the gridlines, and the column letters and row numbers, show. */
    final ClientProperty<Boolean> displayGridlines;
    final ClientProperty<Boolean> displayRowColHeadings;

    /** The names of the ranges the name box offers. */
    final ClientProperty<List<String>> namedRanges;

    /** The outline: the groups, how deep they go, and their summary side. */
    final ClientProperty<List<GroupingData>> colGroupingData;
    final ClientProperty<List<GroupingData>> rowGroupingData;
    final ClientProperty<Integer> colGroupingMax;
    final ClientProperty<Integer> rowGroupingMax;
    final ClientProperty<Boolean> colGroupingInversed;
    final ClientProperty<Boolean> rowGroupingInversed;

    /**
     * The style of whole rows and columns, those locked, the rules of the
     * borders cells draw on others, and those of the conditional formats.
     */
    final ClientProperty<HashMap<Integer, Integer>> rowIndexToStyleIndex;
    final ClientProperty<HashMap<Integer, Integer>> columnIndexToStyleIndex;
    final ClientProperty<Set<Integer>> lockedColumnIndexes;
    final ClientProperty<Set<Integer>> lockedRowIndexes;
    final ClientProperty<ArrayList<String>> shiftedCellBorderStyles;
    final ClientProperty<HashMap<Integer, String>> conditionalFormattingStyles;

    /** The merged regions. */
    final ClientProperty<ArrayList<MergedRegion>> mergedRegions;

    /**
     * What the cells in view carry besides their values: link tooltips,
     * comments, their authors and which show always, and the cells marked as
     * holding an invalid formula.
     */
    final ClientProperty<HashMap<String, String>> hyperlinksTooltips;
    final ClientProperty<HashMap<String, String>> cellComments;
    final ClientProperty<HashMap<String, String>> cellCommentAuthors;
    final ClientProperty<ArrayList<String>> visibleCellComments;
    final ClientProperty<Set<String>> invalidFormulaCells;

    /** The pictures and charts in view, by id. */
    final ClientProperty<HashMap<String, OverlayInfo>> overlays;

    /** The cell of each component in view, by the node id of the component. */
    final ClientMapProperty<String, String> componentIDtoCellKeysMap;
    /** The node id of the editor of each cell, by cell key. */
    final ClientProperty<HashMap<String, String>> cellKeysToEditorIdMap;

    private final List<ClientProperty<?>> properties = new ArrayList<>();

    SheetClientModel(Element element) {
        rows = add(ClientProperty.integer(element, "rows", 0));
        cols = add(ClientProperty.integer(element, "cols", 0));
        rowH = add(ClientProperty.json(element, "rowH", SheetSizes::runs));
        colW = add(ClientProperty.json(element, "colW", SheetSizes::runs));
        defRowH = add(ClientProperty.decimal(element, "defRowH", 15f));
        hiddenRowIndexes = add(
                ClientProperty.json(element, "hiddenRowIndexes"));
        hiddenColumnIndexes = add(
                ClientProperty.json(element, "hiddenColumnIndexes"));
        verticalSplitPosition = add(
                ClientProperty.integer(element, "verticalSplitPosition", 0));
        horizontalSplitPosition = add(
                ClientProperty.integer(element, "horizontalSplitPosition", 0));
        sheetProtected = add(
                ClientProperty.bool(element, "sheetProtected", false));
        lockFormatColumns = add(
                ClientProperty.bool(element, "lockFormatColumns", false));
        lockFormatRows = add(
                ClientProperty.bool(element, "lockFormatRows", false));
        displayGridlines = add(
                ClientProperty.bool(element, "displayGridlines", true));
        displayRowColHeadings = add(
                ClientProperty.bool(element, "displayRowColHeadings", true));
        namedRanges = add(ClientProperty.json(element, "namedRanges"));
        colGroupingData = add(ClientProperty.json(element, "colGroupingData"));
        rowGroupingData = add(ClientProperty.json(element, "rowGroupingData"));
        colGroupingMax = add(
                ClientProperty.integer(element, "colGroupingMax", 0));
        rowGroupingMax = add(
                ClientProperty.integer(element, "rowGroupingMax", 0));
        colGroupingInversed = add(
                ClientProperty.bool(element, "colGroupingInversed", false));
        rowGroupingInversed = add(
                ClientProperty.bool(element, "rowGroupingInversed", false));
        rowIndexToStyleIndex = add(
                ClientProperty.json(element, "rowIndexToStyleIndex"));
        columnIndexToStyleIndex = add(
                ClientProperty.json(element, "columnIndexToStyleIndex"));
        lockedColumnIndexes = add(
                ClientProperty.json(element, "lockedColumnIndexes"));
        lockedRowIndexes = add(
                ClientProperty.json(element, "lockedRowIndexes"));
        shiftedCellBorderStyles = add(
                ClientProperty.json(element, "shiftedCellBorderStyles"));
        conditionalFormattingStyles = add(
                ClientProperty.json(element, "conditionalFormattingStyles"));
        mergedRegions = add(ClientProperty.json(element, "mergedRegions"));
        hyperlinksTooltips = add(
                ClientProperty.json(element, "hyperlinksTooltips"));
        cellComments = add(ClientProperty.json(element, "cellComments"));
        cellCommentAuthors = add(
                ClientProperty.json(element, "cellCommentAuthors"));
        visibleCellComments = add(
                ClientProperty.json(element, "visibleCellComments"));
        invalidFormulaCells = add(
                ClientProperty.json(element, "invalidFormulaCells"));
        overlays = add(ClientProperty.json(element, "overlays"));
        componentIDtoCellKeysMap = new ClientMapProperty<>(element,
                "componentIDtoCellKeysMap");
        cellKeysToEditorIdMap = add(
                ClientProperty.json(element, "cellKeysToEditorIdMap"));
    }

    private <T> ClientProperty<T> add(ClientProperty<T> property) {
        properties.add(property);
        return property;
    }

    /**
     * The sheet is no longer shown: every property goes back to the value the
     * client starts from. What the next sheet sets of them in the same round
     * trip is all the client is sent of them.
     */
    void reset() {
        properties.forEach(ClientProperty::reset);
        componentIDtoCellKeysMap.reset();
    }
}
