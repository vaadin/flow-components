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

import org.apache.poi.ss.util.CellReference;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeListener;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData.DataSelectListener;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;

public abstract class AbstractSeriesReader<CT_SER_TYPE extends XmlObject, SERIES_DATA_TYPE extends AbstractSeriesData> {

    private final XmlObject ctChart;
    private final Spreadsheet spreadsheet;
    private final boolean is3d;

    public enum ValueUpdateMode {
        Y_VALUES, X_VALUES, Z_VALUES, CATEGORIES
    };

    public AbstractSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet) {
        this(ctChart, spreadsheet, false);
    }

    public AbstractSeriesReader(XmlObject ctChart, Spreadsheet spreadsheet,
            boolean is3d) {
        this.ctChart = ctChart;
        this.spreadsheet = spreadsheet;
        this.is3d = is3d;
    }

    protected abstract SERIES_DATA_TYPE createSeriesDataObject(CT_SER_TYPE serie);

    public List<SERIES_DATA_TYPE> getSeries() {
        List<SERIES_DATA_TYPE> list = new ArrayList<SERIES_DATA_TYPE>();

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
        seriesData.categories = new ArrayList<String>(
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
            return Utils.getAllReferencedCells(formula);
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
                .getAllReferencedCells(formula);

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

        final List<CellReference> ptList = Utils.getAllReferencedCells(formula);

        List<SeriesPoint> list = new ArrayList<SeriesPoint>();

        for (int i = 0; i < ptList.size(); i++) {
            Double cellNumericValue = getNumericValueFromCellRef(ptList.get(i));
            list.add(new SeriesPoint(i, cellNumericValue));
        }

        seriesData.seriesData = list;

        handleReferencedValueUpdates(ptList, seriesData,
                ValueUpdateMode.Y_VALUES);

        seriesData.dataSelectListener = new DataSelectListener() {
            @Override
            public void dataSelected() {
                spreadsheet.setSelection(formula);
            }
        };
    }

    @SuppressWarnings("serial")
    protected void handleReferencedValueUpdates(
            final List<CellReference> referencedCells,
            final SERIES_DATA_TYPE seriesData,
            final ValueUpdateMode updateMode) {
        spreadsheet.addCellValueChangeListener(new CellValueChangeListener() {
            @Override
            public void onCellValueChange(CellValueChangeEvent event) {
                if (seriesData.dataUpdateListener == null) {
                    return;
                }

                for (CellReference changedCell : event.getChangedCells()) {
                    // getChangedCell erroneously provides relative cell refs
                    // if this gets fixed, this conversion method should be
                    // removed
                    // https://dev.vaadin.com/ticket/19717
                    CellReference absoluteChangedCell = relativeToAbsolute(changedCell);
                    if (!referencedCells.contains(absoluteChangedCell)) {
                        continue;
                    }

                    final int index = referencedCells
                            .indexOf(absoluteChangedCell);

                    if (updateMode != ValueUpdateMode.CATEGORIES) {
                        final SeriesPoint item = seriesData.seriesData
                                .get(index);
                        final Double cellValue = Utils.getNumericValue(
                                absoluteChangedCell, spreadsheet);
                        if (updateMode == ValueUpdateMode.X_VALUES) {
                            item.xValue = cellValue;
                            seriesData.dataUpdateListener.xDataModified(index,
                                    cellValue);
                        }
                        if (updateMode == ValueUpdateMode.Y_VALUES) {
                            item.yValue = cellValue;
                            seriesData.dataUpdateListener.yDataModified(index,
                                    cellValue);
                        }
                        if (updateMode == ValueUpdateMode.Z_VALUES) {
                            item.zValue = cellValue;
                            seriesData.dataUpdateListener.zDataModified(index,
                                    cellValue);
                        }
                    } else {
                        final String cellValue = Utils.getStringValue(
                                absoluteChangedCell, spreadsheet);
                        seriesData.dataUpdateListener.categoryModified(index,
                                cellValue);
                    }
                }
            }

            private CellReference relativeToAbsolute(CellReference cell) {
                String sheetName = spreadsheet.getActiveSheet().getSheetName();
                return new CellReference(sheetName, cell.getRow(), cell
                        .getCol(), true, true);
            }
        });

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
