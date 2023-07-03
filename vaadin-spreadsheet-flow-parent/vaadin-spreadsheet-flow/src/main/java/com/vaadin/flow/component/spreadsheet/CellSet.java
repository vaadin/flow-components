package com.vaadin.flow.component.spreadsheet;

import org.apache.poi.ss.util.CellReference;

import java.util.Collections;
import java.util.Set;

public class CellSet {

    private final Set<CellReference> cells;

    public CellSet(Set<CellReference> cells) {
        this.cells = cells;
    }

    public Set<CellReference> getCells() {
        return Collections.unmodifiableSet(cells);
    }

    public int getCellCount() {
        return cells.size();
    }

    public boolean containsCell(CellReference cell) {
        if (cells.isEmpty()) {
            return false;
        }
        if (cell.getSheetName() == null) {
            CellReference cellWithSheetName = new CellReference(
                    cells.iterator().next().getSheetName(), cell.getRow(),
                    cell.getCol(), cell.isRowAbsolute(), cell.isColAbsolute());
            return cells.contains(cellWithSheetName);
        }
        return cells.contains(cell);
    }
}
