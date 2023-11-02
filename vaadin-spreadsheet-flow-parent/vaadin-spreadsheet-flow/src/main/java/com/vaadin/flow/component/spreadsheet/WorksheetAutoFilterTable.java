/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.component.spreadsheet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;

/**
 * Spreadsheet table that is created from an CTWorksheet().getAutoFilter().
 * Marks Popup buttons active after they are initialized according to
 * autoFilter.getFilterColumnList()
 */
public class WorksheetAutoFilterTable extends SpreadsheetFilterTable {

    private final CTAutoFilter autoFilter;

    public WorksheetAutoFilterTable(Spreadsheet spreadsheet, Sheet sheet,
            CTAutoFilter autoFilter, CellRangeAddress fullTableRegion) {
        super(spreadsheet, sheet, fullTableRegion);
        this.autoFilter = autoFilter;
    }

    @Override
    protected void initClearAllButtons() {
        super.initClearAllButtons();
        markActiveButtons(autoFilter);
    }

    private void markActiveButtons(CTAutoFilter autoFilter) {
        final int offset = getFullTableRegion().getFirstColumn();

        for (CTFilterColumn column : autoFilter.getFilterColumnList()) {
            final int colId = offset + (int) column.getColId();
            getPopupButton(colId).markActive(true);
            popupButtonToClearButtonMap.get(getPopupButton(colId))
                    .setEnabled(true);
        }
    }
}
