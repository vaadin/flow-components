package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vaadin.addon.spreadsheet.client.GroupingWidget.GroupingData;
import com.vaadin.shared.annotations.DelegateToWidget;
import com.vaadin.shared.ui.TabIndexState;

@SuppressWarnings("serial")
public class SpreadsheetState extends TabIndexState {

    @DelegateToWidget
    public int rowBufferSize = 200;

    @DelegateToWidget
    public int columnBufferSize = 200;

    @DelegateToWidget
    public int rows;

    @DelegateToWidget
    public int cols;

    @DelegateToWidget
    public List<GroupingData> colGroupingData;
    @DelegateToWidget
    public List<GroupingData> rowGroupingData;

    @DelegateToWidget
    public int colGroupingMax;
    @DelegateToWidget
    public int rowGroupingMax;

    @DelegateToWidget
    public boolean colGroupingInversed;
    @DelegateToWidget
    public boolean rowGroupingInversed;

    @DelegateToWidget
    public float defRowH;
    @DelegateToWidget
    public int defColW;

    @DelegateToWidget
    public float[] rowH;
    @DelegateToWidget
    public int[] colW;

    /** should the sheet be reloaded on client side */
    public boolean reload;

    /** 1-based */
    public int sheetIndex = 1;

    public String[] sheetNames = null;

    @DelegateToWidget
    public HashMap<Integer, String> cellStyleToCSSStyle = null;

    @DelegateToWidget
    public ArrayList<String> shiftedCellBorderStyles = null;

    /**
     * All conditional formatting styles for this sheet.
     */
    @DelegateToWidget
    public HashMap<Integer, String> conditionalFormattingStyles = null;

    /** 1-based */
    @DelegateToWidget
    public ArrayList<Integer> hiddenColumnIndexes = null;

    /** 1-based */
    @DelegateToWidget
    public ArrayList<Integer> hiddenRowIndexes = null;

    @DelegateToWidget
    public int[] verticalScrollPositions;

    @DelegateToWidget
    public int[] horizontalScrollPositions;

    public boolean sheetProtected;

    @DelegateToWidget
    public boolean workbookProtected;

    public HashMap<String, String> cellKeysToEditorIdMap;

    public HashMap<String, String> componentIDtoCellKeysMap;

    /** Cell CSS key -> link tooltip (usually same as address) */
    @DelegateToWidget
    public HashMap<String, String> hyperlinksTooltips;

    public HashMap<String, String> cellComments;
    public HashMap<String, String> cellCommentAuthors;

    public ArrayList<String> visibleCellComments;

    public boolean hasActions;

    public HashMap<String, ImageInfo> resourceKeyToImage;

    public ArrayList<MergedRegion> mergedRegions;

    @DelegateToWidget
    public boolean displayGridlines = true;

    @DelegateToWidget
    public boolean displayRowColHeadings = true;

    @DelegateToWidget
    public int verticalSplitPosition = 0;
    @DelegateToWidget
    public int horizontalSplitPosition = 0;

    @DelegateToWidget
    public String infoLabelValue;

    public boolean workbookChangeToggle;

}
