package com.vaadin.addon.spreadsheet;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

public class XSSFColorConverter implements ColorConverter {

    private String defaultBackgroundColor;
    private String defaultColor;
    private XSSFWorkbook workbook;

    public XSSFColorConverter(XSSFWorkbook workbook) {
        this.workbook = workbook;
        workbook.getTheme();
    }

    @Override
    public void colorStyles(CellStyle cellStyle, StringBuilder sb) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillBackgroundXSSFColor = cs.getFillBackgroundXSSFColor();
        XSSFColor fillForegroundXSSFColor = cs.getFillForegroundXSSFColor();
        String backgroundColor = null;
        if (fillForegroundXSSFColor != null
                && !fillForegroundXSSFColor.isAuto()) {
            backgroundColor = styleColor(fillForegroundXSSFColor);
        } else if (fillBackgroundXSSFColor != null
                && !fillBackgroundXSSFColor.isAuto()) {
            backgroundColor = styleColor(fillBackgroundXSSFColor);
        } else {
            // bypass POI API and try to get the fill ourself, because of bug:
            // https://issues.apache.org/bugzilla/show_bug.cgi?id=53262
            try {
                XSSFColor themeColor = getFillColor(cs);
                if (themeColor != null && !themeColor.isAuto()) {
                    backgroundColor = styleColor(themeColor);
                }
            } catch (Exception e) {
            }
        }

        if (backgroundColor != null
                && !backgroundColor.equals(defaultBackgroundColor)) {
            sb.append("background-color:");
            sb.append(backgroundColor);
        }

        XSSFColor xssfColor = cs.getFont().getXSSFColor();
        if (xssfColor != null) {
            String color = styleColor(xssfColor);
            if (color != null && !color.equals(defaultColor)) {
                sb.append("color:");
                sb.append(color);
            }
        }
    }

    private XSSFColor getFillColor(XSSFCellStyle cs) {
        final CTXf _cellXf = cs.getCoreXf();
        int fillIndex = (int) _cellXf.getFillId();
        XSSFCellFill fg = workbook.getStylesSource().getFillAt(fillIndex);

        ThemesTable _theme = workbook.getTheme();
        XSSFColor fillForegroundColor = fg.getFillForegroundColor();
        if (fillForegroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillForegroundColor);
        }
        XSSFColor fillBackgroundColor = fg.getFillBackgroundColor();
        if (fillForegroundColor == null) {
            if (fillBackgroundColor != null && _theme != null) {
                _theme.inheritFromThemeAsRequired(fillBackgroundColor);
            }
            return fillBackgroundColor;
        } else {
            return fillForegroundColor;
        }
    }

    @Override
    public void colorBorder(BorderSide borderSide, String attr,
            CellStyle cellStyle, StringBuilder sb) {
        XSSFColor color;
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // ApplyBorder is not working for Excel themes, so need to get the
            // color manually
            color = getBorderColor((XSSFCellStyle) cellStyle, borderSide);
        } else {
            color = ((XSSFCellStyle) cellStyle).getBorderColor(borderSide);
        }

        sb.append(attr);
        sb.append(":");
        if (color == null || color.isAuto()) {
            sb.append("#000;");
            return;
        }

        byte[] argb = color.getARgb();
        if (argb == null) {
            sb.append("#000;");
            return;
        }

        final double tint = color.getTint();
        if (tint != 0.0) {
            argb[1] = applyTint(argb[1] & 0xFF, tint);
            argb[2] = applyTint(argb[2] & 0xFF, tint);
            argb[3] = applyTint(argb[3] & 0xFF, tint);
        }

        try {
            String temp = toRGBA(argb);
            sb.append(temp);
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage() + " " + nfe.getCause());
            sb.append(String
                    .format("#%02x%02x%02x;", argb[1], argb[2], argb[3]));
        }
    }

    private XSSFColor getBorderColor(XSSFCellStyle cs, BorderSide borderSide) {
        int idx = (int) cs.getCoreXf().getBorderId();
        XSSFCellBorder border = workbook.getStylesSource().getBorderAt(idx);

        return border.getBorderColor(borderSide);
    }

    private String styleColor(XSSFColor color) {
        if (color == null || color.isAuto()) {
            return null;
        }

        byte[] argb = color.getARgb();
        if (argb == null) {
            return null;
        }

        final double tint = color.getTint();
        if (tint != 0.0) {
            argb[1] = applyTint(argb[1] & 0xFF, tint);
            argb[2] = applyTint(argb[2] & 0xFF, tint);
            argb[3] = applyTint(argb[3] & 0xFF, tint);
        }

        try {
            String temp = toRGBA(argb);
            return temp;
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.getMessage() + " " + nfe.getCause());
            return String.format("#%02x%02x%02x;", argb[1], argb[2], argb[3]);
        }
    }

    private String toRGBA(byte[] argb) {
        StringBuilder sb = new StringBuilder("rgba(");
        int rgba[] = new int[3];
        for (int i = 1; i < argb.length; i++) {
            int x = argb[i];
            if (x < 0) {
                x += 256;
            }
            rgba[i - 1] = x;
        }
        sb.append(rgba[0]);
        sb.append(",");
        sb.append(rgba[1]);
        sb.append(",");
        sb.append(rgba[2]);
        sb.append(",");
        float x = argb[0];
        if (x == -1.0f) {
            x = 1.0f;
        } else if (x == 0.0) {
            // This is done because of a bug (???) in POI. Colors from libre
            // office in POI have the alpha-channel as 0.0, so that makes the
            // colors all wrong. The correct value should be -1.0 (no Alpha)
            x = 1.0f;
        }
        sb.append(x);
        sb.append(");");
        return sb.toString();
    }

    private static byte applyTint(int lum, double tint) {
        if (tint > 0) {
            return (byte) (lum * (1.0 - tint) + (255 - 255 * (1.0 - tint)));
        } else if (tint < 0) {
            return (byte) (lum * (1 + tint));
        } else {
            return (byte) lum;
        }
    }

    @Override
    public void defaultColorStyles(CellStyle cellStyle, StringBuilder sb) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillForegroundColorColor = cs.getFillForegroundColorColor();
        XSSFColor fillBackgroundColorColor = cs.getFillBackgroundColorColor();
        defaultBackgroundColor = styleColor(fillForegroundColorColor);
        defaultBackgroundColor = styleColor(fillBackgroundColorColor);

        if (defaultBackgroundColor == null) {
            defaultBackgroundColor = "rgba(255,255,255,1.0);";
        }
        sb.append("background-color:");
        sb.append(defaultBackgroundColor);

        XSSFColor xssfColor = cs.getFont().getXSSFColor();
        defaultColor = styleColor(xssfColor);

        if (defaultColor == null) {
            defaultColor = "rgba(0,0,0,1.0);";
        }
        sb.append("color:");
        sb.append(defaultColor);

    }

    @Override
    public boolean hasBackgroundColor(CellStyle cellStyle) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillBackgroundXSSFColor = cs.getFillBackgroundXSSFColor();
        XSSFColor fillForegroundXSSFColor = cs.getFillForegroundXSSFColor();
        if (fillForegroundXSSFColor != null
                && !fillForegroundXSSFColor.isAuto()) {
            return true;
        } else if (fillBackgroundXSSFColor != null
                && !fillBackgroundXSSFColor.isAuto()) {
            return true;
        } else {
            // bypass POI API and try to get the fill ourself, because of bug:
            // https://issues.apache.org/bugzilla/show_bug.cgi?id=53262
            try {
                XSSFColor themeColor = getFillColor(cs);
                if (themeColor != null && !themeColor.isAuto()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public String getBackgroundColorCSS(ConditionalFormattingRule rule) {

        XSSFConditionalFormattingRule r = (XSSFConditionalFormattingRule) rule;

        CTDxf dxf = getXMLColorDataWithReflection(r);

        CTColor bgColor = dxf.getFill().getPatternFill().getBgColor();
        byte[] rgb = bgColor.getRgb();

        return rgb == null ? null : toRGBA(rgb);
    }

    /**
     * XSSF doesn't have an API to get this value, so brute force it is..
     * 
     * @param rule
     *            The rule that has color data defined
     * @return OpenXML data format that contains the real defined styles
     */
    private CTDxf getXMLColorDataWithReflection(
            XSSFConditionalFormattingRule rule) {
        CTCfRule realRule = null;

        Method declaredMethod = null;
        try {
            declaredMethod = rule.getClass().getDeclaredMethod("getCTCfRule");
            declaredMethod.setAccessible(true);
            realRule = (CTCfRule) declaredMethod.invoke(rule);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).fine(e.getMessage());
        } finally {
            declaredMethod.setAccessible(false);
        }

        CTDxf dxf = workbook.getStylesSource().getCTStylesheet().getDxfs()
                .getDxfArray((int) realRule.getDxfId());

        return dxf;
    }

    @Override
    public String getFontColorCSS(ConditionalFormattingRule rule) {

        XSSFConditionalFormattingRule r = (XSSFConditionalFormattingRule) rule;

        CTDxf dxf = getXMLColorDataWithReflection(r);
        CTFont font = dxf.getFont();

        if (font.getColorList() == null || font.getColorList().isEmpty()) {
            // default color
            return null;
        }

        CTColor ctColor = font.getColorList().get(0);
        byte[] rgb = ctColor.getRgb();

        if (rgb == null) {
            // default color
            return null;
        }

        return toRGBA(rgb);
    }
}