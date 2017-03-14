package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeListener;
import com.vaadin.addon.spreadsheet.SpreadsheetUtil;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData.DataSelectListener;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;

public abstract class AbstractSeriesReader<CT_SER_TYPE extends XmlObject, SERIES_DATA_TYPE extends AbstractSeriesData> {

    private final XmlObject ctChart;
    private final Spreadsheet spreadsheet;
    private final boolean is3d;
    protected final boolean showDataInHiddenCells;

    public enum ValueUpdateMode {
        Y_VALUES, X_VALUES, Z_VALUES, CATEGORIES
    };

    public AbstractSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        this(ctChart, spreadsheet, false, showDataInHiddenCells);
    }

    public AbstractSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet,
            boolean is3d, boolean showDataInHiddenCells) {
        this.ctChart = ctChart;
        this.spreadsheet = spreadsheet;
        this.is3d = is3d;
        this.showDataInHiddenCells = showDataInHiddenCells;
    }

    protected abstract SERIES_DATA_TYPE createSeriesDataObject(CT_SER_TYPE serie);

    public List<SERIES_DATA_TYPE> getSeries() {
        List<SERIES_DATA_TYPE> list = new ArrayList<>();

        for (CT_SER_TYPE serie : getSerList()) {
            list.add(createAndFillSeriesDataObject(serie));
        }

        return list;
    }

    private SERIES_DATA_TYPE createAndFillSeriesDataObject(CT_SER_TYPE serie) {
        SERIES_DATA_TYPE seriesData = createSeriesDataObject(serie);
        fillSeriesData(seriesData, serie);
        return seriesData;
    }

    protected XmlObject getChart() {
        return ctChart;
    }

    @SuppressWarnings("unchecked")
    private List<CT_SER_TYPE> getSerList() {
        return (List<CT_SER_TYPE>) Utils.callMethodUsingReflection(ctChart,
                "getSerList");
    }

    protected void fillSeriesData(SERIES_DATA_TYPE seriesData, CT_SER_TYPE serie) {
        CTSerAdapter ctSerAdapter = new CTSerAdapter(serie);

        seriesData.name = tryGetSeriesName(ctSerAdapter.getTx());
        createCategories(ctSerAdapter.getCat(), seriesData);
        createSeriesDataPoints(ctSerAdapter.getVal(), seriesData);
        seriesData.is3d = this.is3d;
    }

    protected void createCategories(CTAxDataSource axisDataSource,
            SERIES_DATA_TYPE seriesData) {
        if (axisDataSource == null) {
            return;
        }

        final List<CellReference> cellReferences = getCategoryCellReferences(axisDataSource);

        // AbstractList is not serializable, so we wrap it into an ArrayList
        seriesData.categories = new ArrayList<>(
                new AbstractList<String>() {
                    @Override
                    public String get(int index) {
                        return Utils.getStringValue(cellReferences.get(index),
                                spreadsheet);
                    }

                    @Override
                    public int size() {
                        return cellReferences.size();
                    }
                });
        handleReferencedValueUpdates(cellReferences, seriesData,
                ValueUpdateMode.CATEGORIES);
    }

    private List<CellReference> getCategoryCellReferences(
            CTAxDataSource axisDataSource) {

        if (axisDataSource.isSetStrRef()) {
            String formula = axisDataSource.getStrRef().getF();
            return Utils.getAllReferencedCells(formula, spreadsheet,
                    showDataInHiddenCells);
        } else if (axisDataSource.isSetNumRef()) {
            String formula = axisDataSource.getNumRef().getF();
            return Utils.getAllReferencedCells(formula, spreadsheet,
                    showDataInHiddenCells);
        } else if (axisDataSource.isSetMultiLvlStrRef()) {
            return tryHandleMultilevelCategories(axisDataSource);
        } else {
            // others not supported yet
            return Collections.emptyList();
        }
    }

    private List<CellReference> tryHandleMultilevelCategories(
            CTAxDataSource axisDataSource) {
        // HighChart doesn't support multilevel, take only the first one
        String formula = axisDataSource.getMultiLvlStrRef().getF();

        final List<CellReference> allReferencedCells = Utils
                .getAllReferencedCells(formula, spreadsheet,
                        showDataInHiddenCells);

        final CellReference firstCell = allReferencedCells.get(0);
        final CellReference lastCell = allReferencedCells
                .get(allReferencedCells
                .size() - 1);

        final int width = lastCell.getCol() - firstCell.getCol() + 1;
        final int height = lastCell.getRow() - firstCell.getRow() + 1;

        final int numOfPointsInCache = (int) axisDataSource.getMultiLvlStrRef()
                .getMultiLvlStrCache().getPtCount().getVal();
        final int numOfLevels = allReferencedCells.size() / numOfPointsInCache;

        if (numOfLevels == width) {
            return new AbstractList<CellReference>() {
                @Override
                public CellReference get(int index) {
                    return allReferencedCells.get((numOfLevels - 1) + index
                            * numOfLevels);
                }

                @Override
                public int size() {
                    return numOfPointsInCache;
                }
            };
        } else if (numOfLevels == height) {
            return new AbstractList<CellReference>() {
                @Override
                public CellReference get(int index) {
                    return allReferencedCells.get(allReferencedCells.size()
                            - width + index);
                }

                @Override
                public int size() {
                    return numOfPointsInCache;
                }
            };
        } else {
            System.err.println("Could not handle multilevel categories");
            return Collections.emptyList();
        }
    }

    protected void createSeriesDataPoints(CTNumDataSource val,
            SERIES_DATA_TYPE seriesData) {
        final String formula = val.getNumRef().getF();

        final List<CellReference> ptList = Utils.getAllReferencedCells(formula,
                spreadsheet, showDataInHiddenCells);

        List<SeriesPoint> list = new ArrayList<>();

        for (int i = 0; i < ptList.size(); i++) {
            Double cellNumericValue = getNumericValueFromCellRef(ptList.get(i));
            list.add(new SeriesPoint(i, cellNumericValue));
        }

        seriesData.seriesData = list;
        seriesData.tooltipDecimals = calculateDecimalsForTooltip(ptList);

        handleReferencedValueUpdates(ptList, seriesData,
                ValueUpdateMode.Y_VALUES);

        seriesData.dataSelectListener = new DataSelectListener() {
            @Override
            public void dataSelected() {
                spreadsheet.setSelection(formula);
            }
        };
    }

    private int calculateDecimalsForTooltip(List<CellReference> ptList) {

        if (ptList.size() <= 0) {
            // No points, so go with the default number of decimals
            return -1;
        }

        CellReference ref = ptList.get(0);
        Sheet sheet = spreadsheet.getWorkbook().getSheet(ref.getSheetName());
        Cell cell = spreadsheet.getCell(ref, sheet);
        if (cell == null) {
            return -1;
        }
        CellStyle style = cell.getCellStyle();
        String styleString = style.getDataFormatString();
        if (styleString == null || styleString.isEmpty()
                || styleString.equals("General")) {
            // No formatting info given, so go with the default number of
            // decimals
            return -1;
        }

        //In formatting strings "." is always used it seems.
        char sep = '.';

        // Take the last occurrence if the user has the same symbol as thousand
        // separator (should not be possible)
        int sepIndex = styleString.trim().lastIndexOf(sep);
        int decimalCount;
        if (sepIndex < 0) {
            decimalCount = 0;
        } else {
            decimalCount = styleString.length() - sepIndex - 1;
        }
        return decimalCount;
    }

    private void onValueChange(final List<CellReference> referencedCells,
                               final SERIES_DATA_TYPE seriesData,
                               final ValueUpdateMode updateMode,
                               Spreadsheet.ValueChangeEvent event) {
        if (seriesData.dataUpdateListener == null) {
            return;
        }

        for (CellReference changedCell : event.getChangedCells()) {
            // getChangedCell erroneously provides relative cell refs
            // if this gets fixed, this conversion method should be
            // removed
            // https://dev.vaadin.com/ticket/19717
            updatePoint(referencedCells, seriesData, updateMode,
                    changedCell);
        }
    }

    @SuppressWarnings("serial")
    protected void handleReferencedValueUpdates(
            final List<CellReference> referencedCells,
            final SERIES_DATA_TYPE seriesData,
            final ValueUpdateMode updateMode) {

        spreadsheet.addCellValueChangeListener(new CellValueChangeListener() {
            @Override
            public void onCellValueChange(CellValueChangeEvent event) {
                onValueChange(referencedCells, seriesData, updateMode, event);
            }
        });

        spreadsheet.addFormulaValueChangeListener(new Spreadsheet.FormulaValueChangeListener() {
            @Override
            public void onFormulaValueChange(Spreadsheet.FormulaValueChangeEvent event) {
                onValueChange(referencedCells, seriesData, updateMode, event);
            }
        });
    }

    private void updatePoint(List<CellReference> referencedCells,
                             SERIES_DATA_TYPE seriesData, ValueUpdateMode updateMode,
                             CellReference changedCell) {
        CellReference absoluteChangedCell = SpreadsheetUtil.relativeToAbsolute(spreadsheet, changedCell);
        if (!referencedCells.contains(absoluteChangedCell)) {
            return;
        }

        final int index = referencedCells.indexOf(absoluteChangedCell);

        if (updateMode != ValueUpdateMode.CATEGORIES) {
            final SeriesPoint item = seriesData.seriesData.get(index);
            final Double cellValue = Utils.getNumericValue(absoluteChangedCell,
                    spreadsheet);
            if (updateMode == ValueUpdateMode.X_VALUES) {
                item.xValue = cellValue;
                seriesData.dataUpdateListener.xDataModified(index, cellValue);
            }
            if (updateMode == ValueUpdateMode.Y_VALUES) {
                item.yValue = cellValue;
                seriesData.dataUpdateListener.yDataModified(index, cellValue);
            }
            if (updateMode == ValueUpdateMode.Z_VALUES) {
                item.zValue = cellValue;
                seriesData.dataUpdateListener.zDataModified(index,
                        cellValue);
            }
        } else {
            final String cellValue = Utils.getStringValue(absoluteChangedCell,
                    spreadsheet);
            seriesData.dataUpdateListener.categoryModified(index, cellValue);
        }
    }

    protected String tryGetSeriesName(CTSerTx tx) {
        try {
            if (tx.isSetV()) {
                return tx.getV();
            }

            if (tx.isSetStrRef()) {
                String formula = tx.getStrRef().getF();

                return Utils.getStringValueFromFormula(formula, spreadsheet);
            }
        } catch (Exception e) {
        }
        return null;
    }

    protected Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    protected Double getNumericValueFromCellRef(CellReference cellRef) {
        return Utils.getNumericValue(cellRef, getSpreadsheet());
    }
}
