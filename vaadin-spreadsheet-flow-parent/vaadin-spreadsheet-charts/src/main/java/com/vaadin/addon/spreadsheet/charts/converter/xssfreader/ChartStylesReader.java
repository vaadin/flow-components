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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.AxisProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.BackgroundProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.BorderStyle;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.ColorProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.TextProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.TitleProperties;

class ChartStylesReader {

    private static final double DEFAULT_BORDER_WIDTH = 0.75;

    private static final double FONT_SIZE_FACTOR = 100.0;

    // in excel it is a boolean option
    private static final int EXCEL_BORDER_RADIUS = 8;

    private static final double EMU_PER_PT = 12700.0;

    private final Spreadsheet spreadsheet;

    private XSSFChart xssfChart;

    private CTBaseStyles themeElements;

    private Map<String, byte[]> colorMap;

    private static Logger logger = Logger.getLogger(ChartStylesReader.class
            .getName());

    public ChartStylesReader(Spreadsheet spreadsheet, XSSFChart xssfChart) {
        this.spreadsheet = spreadsheet;
        this.xssfChart = xssfChart;
    }

    public BackgroundProperties getBackgroundProperties() {
        CTShapeProperties spPr = xssfChart.getCTChartSpace().getSpPr();

        if (spPr == null)
            return null;

        BackgroundProperties backgroundProperties = new BackgroundProperties();

        if (spPr.isSetNoFill())
            backgroundProperties.color = new ColorProperties(new int[] { 0xFF,
                    0xFF, 0xFF }, 0);
        else if (spPr.isSetSolidFill())
            backgroundProperties.color = ColorUtils
                    .createColorPropertiesFromFill(spPr.getSolidFill(),
                            getColorMap());
        else if (spPr.isSetGradFill()) {
            backgroundProperties.gradient = ColorUtils
                    .createGradientProperties(spPr.getGradFill(), getColorMap());
        } else {
            boolean onlyBorderIsSet = (spPr.getDomNode().getChildNodes()
                    .getLength() == 1)
                    && spPr.isSetLn();

            if (!onlyBorderIsSet)
                logger.warning("Unsupported fill for shape " + spPr);
        }

        return backgroundProperties;
    }

    public TitleProperties getTitleProperties() {
        TitleProperties result = new TitleProperties();

        try {
            CTTitle ctTitle = xssfChart.getCTChart().getTitle();

            CTTextCharacterProperties textProp;

            if (ctTitle.isSetTx()) {
                textProp = ctTitle.getTx().getRich().getPList().get(0).getPPr()
                        .getDefRPr();
            } else {
                textProp = ctTitle.getTxPr().getPList().get(0).getPPr()
                        .getDefRPr();
            }

            result.textProperties = createFontProperties(textProp);

            result.isFloating = ctTitle.getOverlay().getVal();
        } catch (NullPointerException e) {
            // NOP
        }

        return result;
    }

    public TextProperties getLegendTextProperties() {
        try {
            CTTextCharacterProperties defRPr = xssfChart.getCTChart()
                    .getLegend().getTxPr().getPArray(0).getPPr().getDefRPr();

            return createFontProperties(defRPr);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public LinkedHashMap<Long, AxisProperties> getYAxisProperties() {
        LinkedHashMap<Long, AxisProperties> result = new LinkedHashMap<Long, AxisProperties>();

        List<CTValAx> valAxList = xssfChart.getCTChart().getPlotArea()
                .getValAxList();

        for (CTValAx valAx : valAxList) {
            result.put(valAx.getAxId().getVal(),
                    getAxisProperties(valAx));
        }

        return result;
    }

    public AxisProperties getXAxisProperties() {
        List<CTCatAx> catAxList = xssfChart.getCTChart().getPlotArea()
                .getCatAxList();

        if (catAxList.size() > 0)
            return getAxisProperties(catAxList.get(0));
        else
            return null;
    }

    public BorderStyle getBorderStyle() {
        try {
            BorderStyle result = new BorderStyle();

            CTLineProperties borderLineProp = xssfChart.getCTChartSpace()
                    .getSpPr().getLn();

            if (borderLineProp.isSetNoFill())
                return result;

            if (xssfChart.getCTChartSpace().getRoundedCorners().getVal())
                result.radius = EXCEL_BORDER_RADIUS;

            if (borderLineProp.isSetW())
                result.width = borderLineProp.getW() / EMU_PER_PT;
            else
                result.width = DEFAULT_BORDER_WIDTH;

            result.color = ColorUtils.createColorPropertiesFromFill(
                    borderLineProp.getSolidFill(), getColorMap());

            return result;
        } catch (NullPointerException e) {
            // it seems that excel default is to have a border
            return new BorderStyle() {
                {
                    width = DEFAULT_BORDER_WIDTH;
                }
            };
        }
    }

    /**
     * Allows for overriding to wrap in additional property detection/conversion
     * NOTE: POI needs a meta-API for the generated OOXML CT* classes,
     * so shared properties like these can come from a common interface
     * @param yAx 
     * @return axis properties
     */
    protected AxisProperties getAxisProperties(CTValAx yAx) {
        AxisProperties axisProperties = new AxisProperties();

        readAxisTitle(yAx.getTitle(), axisProperties);

        CTScaling scaling = yAx.getScaling();
        if (scaling != null && scaling.isSetMin()) {
            axisProperties.minVal = Double.valueOf(scaling.getMin().getVal());
        }
        if (scaling != null && scaling.isSetMax()) {
            axisProperties.maxVal = Double.valueOf(scaling.getMax().getVal());
        }

        return axisProperties;
    }

    /**
     * Allows for overriding to wrap in additional property detection/conversion
     * NOTE: POI needs a meta-API for the generated OOXML CT* classes,
     * so shared properties like these can come from a common interface
     * @param xAx 
     * @return axis properties
     */
    protected AxisProperties getAxisProperties(CTCatAx xAx) {
        AxisProperties axisProperties = new AxisProperties();

        readAxisTitle(xAx.getTitle(), axisProperties);

        CTScaling scaling = xAx.getScaling();
        if (scaling != null && scaling.isSetMin()) {
            axisProperties.minVal = Double.valueOf(scaling.getMin().getVal());
        }
        if (scaling != null && scaling.isSetMax()) {
            axisProperties.maxVal = Double.valueOf(scaling.getMax().getVal());
        }

        return axisProperties;
    }

    private void readAxisTitle(CTTitle title, AxisProperties axisProperties) {
        try {
            CTTextParagraph p = title.getTx().getRich().getPArray(0);

            axisProperties.title = "";

            for (CTRegularTextRun r : p.getRList())
                axisProperties.title += r.getT();

            axisProperties.textProperties = createFontProperties(p.getPPr()
                    .getDefRPr());

            if (axisProperties.textProperties == null) {
                axisProperties.textProperties = new TextProperties();
                // default in Excel
                axisProperties.textProperties.bold = true;
            }
        } catch (NullPointerException e) {
            // NOP
        }
    }

    private CTBaseStyles getThemeElements() {
        if (themeElements == null) {
            ThemesTable theme = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getTheme();
            if (theme == null) {
                return null;
            }
            ThemeDocument themeDocument;
            try {
                themeDocument = ThemeDocument.Factory.parse(theme
                        .getPackagePart().getInputStream());
            } catch (XmlException e) {
                return null;
            } catch (IOException e) {
                return null;
            }

            themeElements = themeDocument.getTheme().getThemeElements();
        }

        return themeElements;
    }

    private String getFontFamilyConsideringTheme(CTTextCharacterProperties pPr) {
        try {
            String fontString = pPr.getLatin().getTypeface();

            if (fontString.startsWith("+mj"))
                return getMajorFont();
            else if (fontString.startsWith("+mn"))
                return getMinorFont();
            else
                return fontString;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String getMinorFont() {
        if (getThemeElements() == null) {
            return "";
        }
        try {
            return getThemeElements().getFontScheme().getMinorFont().getLatin()
                    .getTypeface();
        } catch (NullPointerException e) {
            return "";
        }
    }

    private String getMajorFont() {
        if (getThemeElements() == null) {
            return "";
        }
        try {
            return getThemeElements().getFontScheme().getMajorFont().getLatin()
                    .getTypeface();
        } catch (NullPointerException e) {
            return "";
        }
    }

    private TextProperties createFontProperties(CTTextCharacterProperties pPr) {
        try {
            TextProperties result = new TextProperties();

            result.fontFamily = getFontFamilyConsideringTheme(pPr);
            result.size = pPr.getSz() / FONT_SIZE_FACTOR;
            result.bold = pPr.getB();
            result.italics = pPr.getI();

            result.color = ColorUtils.createColorPropertiesFromFill(
                    pPr.getSolidFill(), getColorMap());

            return result;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private Map<String, byte[]> getColorMap() {
        if (colorMap == null) {
            if (getThemeElements() == null) {
                return new HashMap<>();
            }
            colorMap = ColorUtils
                    .createColorMap(getThemeElements().getClrScheme());
        }
        return colorMap;
    }
}
