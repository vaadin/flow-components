package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.annotations.DelegateToWidget;

public class SpreadsheetState extends AbstractComponentState {

    @DelegateToWidget
    public int rowBufferSize = 200;

    @DelegateToWidget
    public int columnBufferSize = 200;

    @DelegateToWidget
    public int rows;

    @DelegateToWidget
    public int cols;

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
    public int sheetIndex;

    public String[] sheetNames = null;

    @DelegateToWidget
    public HashMap<Integer, String> cellStyleToCSSStyle = null;

    @DelegateToWidget
    public ArrayList<String> shiftedCellBorderStyles = null;

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

}
