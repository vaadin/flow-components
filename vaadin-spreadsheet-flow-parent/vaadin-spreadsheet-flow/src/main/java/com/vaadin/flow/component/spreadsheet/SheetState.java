package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.WeakHashMap;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * This Class keeps the last state of selected cell or selected range.
 * <p>
 * NOTICE 1: It ignores individualSelectedCells, moreover, just last range or
 * last selected cell is considered depends on which one happens later.
 */
class SheetState implements Serializable {
    private static Spreadsheet spreadSheet;
    private static WeakHashMap<Sheet, String> selectedCells = new WeakHashMap<Sheet, String>();

    SheetState(final Spreadsheet spreadSheet) {
        this.spreadSheet = spreadSheet;

        spreadSheet.addSelectionChangeListener(
                new Spreadsheet.SelectionChangeListener() {
                    @Override
                    public void onSelectionChange(
                            Spreadsheet.SelectionChangeEvent event) {
                        String lastSelectionState = extractStrCellRef(event);
                        selectedCells.put(spreadSheet.getActiveSheet(),
                                lastSelectionState);
                    }
                });
    }

    /**
     * @param event
     *            selection event
     * @return last selected cell or range.
     */
    private String extractStrCellRef(Spreadsheet.SelectionChangeEvent event) {
        String selectedCellRef = event.getSelectedCellReference()
                .getCellRefParts()[2]
                + event.getSelectedCellReference().getCellRefParts()[1];

        // Hackish way to check whether the last selection is cellSelection or
        // rangeSelection.
        // Last selected range is considered.
        if (!event.getCellRangeAddresses().isEmpty()) {
            String lastSelectedRange = event.getCellRangeAddresses()
                    .get(event.getCellRangeAddresses().size() - 1)
                    .formatAsString();
            if (selectedCellRef
                    .equals(lastSelectedRange.substring(0,
                            lastSelectedRange.indexOf(':')))
                    || selectedCellRef.equals(lastSelectedRange
                            .substring(lastSelectedRange.indexOf(':') + 1))) {
                selectedCellRef = lastSelectedRange;
            }
        }
        return selectedCellRef;
    }

    public String getSelectedCellsOnSheet(Sheet sheetIdx) {
        return selectedCells.get(sheetIdx);
    }

    public void clear() {
        selectedCells.clear();
    }
}
