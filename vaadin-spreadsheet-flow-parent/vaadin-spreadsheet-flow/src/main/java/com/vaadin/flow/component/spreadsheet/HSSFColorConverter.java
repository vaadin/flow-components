package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

/**
 * Color converter implementation for the older Excel file type (.xls or HSSF in
 * POI terms).
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class HSSFColorConverter implements ColorConverter {
    private final HSSFWorkbook wb;
    private final HSSFPalette colors;
    private String defaultBackgroundColor;
    private String defaultColor;

    private static final HSSFColorPredefined HSSF_AUTO = HSSFColorPredefined.AUTOMATIC;

    public HSSFColorConverter(HSSFWorkbook wb) {
        this.wb = wb;
        // If there is no custom palette, then this creates a new one that is
        // a copy of the default
        colors = wb.getCustomPalette();
    }

    @Override
    public String getBorderColorCSS(BorderSide borderSide, String attr,
            CellStyle cellStyle) {

        StringBuilder sb = new StringBuilder();

        final HSSFCellStyle cs = (HSSFCellStyle) cellStyle;
        switch (borderSide) {
        case BOTTOM:
            styleBorderColor(sb, attr, cs.getBottomBorderColor());
            break;
        case LEFT:
            styleBorderColor(sb, attr, cs.getLeftBorderColor());
            break;
        case RIGHT:
            styleBorderColor(sb, attr, cs.getRightBorderColor());
            break;
        case TOP:
            styleBorderColor(sb, attr, cs.getTopBorderColor());
            break;
        default:
            break;
        }

        return sb.toString();
    }

    @Override
    public void colorStyles(final CellStyle cellStyle, final StringBuilder sb) {
        HSSFCellStyle cs = (HSSFCellStyle) cellStyle;
        // TODO Fill pattern not supported
        // out.format(" /* fill pattern = %d */%n", cs.getFillPattern());
        short fillForegroundColor = cs.getFillForegroundColor();
        short fillBackgroundColor = cs.getFillBackgroundColor();

        String backgroundColor = null;
        HSSFColor fillForegroundColorColor = cs.getFillForegroundColorColor();
        if (fillForegroundColorColor != null
                && fillForegroundColor != HSSFColorPredefined.AUTOMATIC
                        .getIndex()) {
            backgroundColor = styleColor(fillForegroundColor);
        } else {
            HSSFColor fillBackgroundColorColor = cs
                    .getFillBackgroundColorColor();
            if (fillBackgroundColorColor != null
                    && fillBackgroundColor != HSSFColorPredefined.AUTOMATIC
                            .getIndex()) {
                backgroundColor = styleColor(fillBackgroundColor);
            }
        }
        if (backgroundColor != null
                && !backgroundColor.equals(defaultBackgroundColor)) {
            sb.append("background-color:");
            sb.append(backgroundColor);
        }

        String color = styleColor(cs.getFont(wb).getColor());
        if (color != null && !color.equals(defaultColor)) {
            sb.append("color:");
            sb.append(color);
        }

    }

    @Override
    public void defaultColorStyles(CellStyle cellStyle, StringBuilder sb) {
        HSSFCellStyle cs = (HSSFCellStyle) cellStyle;
        defaultBackgroundColor = styleColor(cs.getFillBackgroundColor());
        if (defaultBackgroundColor == null) {
            defaultBackgroundColor = "#ffffff;";
        }
        sb.append("background-color:");
        sb.append(defaultBackgroundColor);
        defaultColor = styleColor(cs.getFont(wb).getColor());
        if (defaultColor == null) {
            defaultColor = "#000000;";
        }
        sb.append("color:");
        sb.append(defaultColor);
    }

    @Override
    public boolean hasBackgroundColor(CellStyle cellStyle) {
        HSSFCellStyle cs = (HSSFCellStyle) cellStyle;
        short fillForegroundColor = cs.getFillForegroundColor();
        short fillBackgroundColor = cs.getFillBackgroundColor();

        HSSFColor fillForegroundColorColor = cs.getFillForegroundColorColor();
        if (fillForegroundColorColor != null
                && fillForegroundColor != HSSFColorPredefined.AUTOMATIC
                        .getIndex()) {
            return true;
        } else {
            HSSFColor fillBackgroundColorColor = cs
                    .getFillBackgroundColorColor();
            if (fillBackgroundColorColor != null
                    && fillBackgroundColor != HSSFColorPredefined.AUTOMATIC
                            .getIndex()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getBackgroundColorCSS(ConditionalFormattingRule rule) {
        short index = rule.getFontFormatting().getFontColorIndex();
        String styleColor = styleColor(index);
        return styleColor;
    }

    @Override
    public String getFontColorCSS(ConditionalFormattingRule rule) {
        short color = rule.getPatternFormatting().getFillForegroundColor();
        String styleColor = styleColor(color);
        return styleColor;
    }

    @Override
    public String getBorderColorCSS(BorderSide right, String attribute,
            BorderFormatting borderFormatting) {
        // conditional formatting is not supported for HSSF
        return "";
    }

    private String styleColor(short index) {
        HSSFColor color = colors.getColor(index);
        if (index != HSSF_AUTO.getIndex() && color != null) {
            short[] rgb = color.getTriplet();
            return (String.format("#%02x%02x%02x;", rgb[0], rgb[1], rgb[2]));
        }
        return null;
    }

    private void styleBorderColor(final StringBuilder sb, String attr,
            short index) {
        HSSFColor color = colors.getColor(index);
        sb.append(attr);
        sb.append(":");
        if (index != HSSF_AUTO.getIndex() && color != null) {
            short[] rgb = color.getTriplet();
            sb.append(String.format("#%02x%02x%02x;", rgb[0], rgb[1], rgb[2]));
        } else {
            sb.append("#000;");
        }
    }

    @SuppressWarnings("unused")
    private void styleColor(final StringBuilder sb, String attr, short index) {
        HSSFColor color = colors.getColor(index);
        if (index != HSSF_AUTO.getIndex() && color != null) {
            short[] rgb = color.getTriplet();
            sb.append(attr);
            sb.append(":");
            sb.append(String.format("#%02x%02x%02x;", rgb[0], rgb[1], rgb[2]));
        }
    }
}