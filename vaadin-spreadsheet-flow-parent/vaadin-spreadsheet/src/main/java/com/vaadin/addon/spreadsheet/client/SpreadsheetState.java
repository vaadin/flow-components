package com.vaadin.addon.spreadsheet.client;

import java.util.List;
import java.util.Map;

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
    public Map<Integer, String> indexToStyleMap = null;

    @DelegateToWidget
    public List<String> customCellBorderStyles = null;

    /** 1-based */
    @DelegateToWidget
    public List<Integer> hiddenColumnIndexes = null;

    /** 1-based */
    @DelegateToWidget
    public List<Integer> hiddenRowIndexes = null;

    @DelegateToWidget
    public int[] verticalScrollPositions;

    @DelegateToWidget
    public int[] horizontalScrollPositions;

    public boolean sheetProtected;

    @DelegateToWidget
    public boolean workbookProtected;

    public Map<String, String> cellKeysToEditorIdMap;

    public Map<String, String> componentIDtoCellKeysMap;

    /** Cell CSS key -> link tooltip (usually same as address) */
    @DelegateToWidget
    public Map<String, String> hyperlinksTooltips;

    @DelegateToWidget
    public Map<String, String> cellComments;

    public List<String> visibleCellComments;

    public boolean hasActions;

    public Map<String, ImageInfo> resourceKeyToImage;

    public List<MergedRegion> mergedRegions;
}
