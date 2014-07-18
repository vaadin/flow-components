package com.vaadin.addon.spreadsheet;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

public class HSSFColorConverter implements ColorConverter {
    private final HSSFWorkbook wb;
    private final HSSFPalette colors;
    private String defaultBackgroundColor;
    private String defaultColor;

    private static final HSSFColor HSSF_AUTO = new HSSFColor.AUTOMATIC();

    public HSSFColorConverter(HSSFWorkbook wb) {
        this.wb = wb;
        // If there is no custom palette, then this creates a new one that is
        // a copy of the default
        colors = wb.getCustomPalette();
    }

    @Override
    public void colorBorder(BorderSide borderSide, String attr,
            CellStyle cellStyle, StringBuilder sb) {
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
    }

    @Override
    public void colorStyles(final CellStyle cellStyle, final StringBuilder sb) {
        HSSFCellStyle cs = (HSSFCellStyle) cellStyle;
        // TODO fill pattern ????
        // out.format("  /* fill pattern = %d */%n", cs.getFillPattern());
        short fillForegroundColor = cs.getFillForegroundColor();
        short fillBackgroundColor = cs.getFillBackgroundColor();

        String backgroundColor = null;
        HSSFColor fillForegroundColorColor = cs.getFillForegroundColorColor();
        if (fillForegroundColorColor != null
                && fillForegroundColor != HSSFColor.AUTOMATIC.index) {
            backgroundColor = styleColor(fillForegroundColor);
        } else {
            HSSFColor fillBackgroundColorColor = cs
                    .getFillBackgroundColorColor();
            if (fillBackgroundColorColor != null
                    && fillBackgroundColor != HSSFColor.AUTOMATIC.index) {
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
                && fillForegroundColor != HSSFColor.AUTOMATIC.index) {
            return true;
        } else {
            HSSFColor fillBackgroundColorColor = cs
                    .getFillBackgroundColorColor();
            if (fillBackgroundColorColor != null
                    && fillBackgroundColor != HSSFColor.AUTOMATIC.index) {
                return true;
            }
        }
        return false;
    }
}