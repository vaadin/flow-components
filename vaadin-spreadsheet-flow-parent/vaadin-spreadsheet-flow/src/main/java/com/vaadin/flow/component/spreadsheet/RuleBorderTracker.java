/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

import com.vaadin.flow.component.spreadsheet.SpreadsheetStyleFactory.BorderStyle;

/**
 * Builds the CSS for a conditional-formatting rule's border, and tracks which
 * rules emit top- and left-border CSS that must spill onto adjacent cells.
 *
 * <p>
 * Owns two maps, both keyed by {@link ConditionalFormatting} so a later cell
 * match can look up whether the matching rule needs adjacent-cell border
 * styling:
 *
 * <ul>
 * <li>{@code topBorders} — conditional-formatting → css-index whose CSS
 * describes a {@code border-bottom} rule that should apply to the cell
 * <em>above</em> a matched cell (Excel renders a "top" border on the cell
 * above's bottom edge).</li>
 * <li>{@code leftBorders} — analogous, for left borders rendered on the cell to
 * the left's right edge.</li>
 * </ul>
 *
 * <p>
 * The colour converter is passed in on each call rather than kept, because
 * {@link ConditionalFormatter#colorConverter} is a {@code protected} field that
 * a subclass may assign: border colours then come from the same converter as
 * font and fill colours.
 */
final class RuleBorderTracker implements Serializable {

    private static final String BORDER_STYLE_DEFAULT = "1pt solid #d6d6d6;";

    private final Map<ConditionalFormatting, Integer> topBorders = new HashMap<ConditionalFormatting, Integer>();
    private final Map<ConditionalFormatting, Integer> leftBorders = new HashMap<ConditionalFormatting, Integer>();

    void clear() {
        topBorders.clear();
        leftBorders.clear();
    }

    boolean hasTopBorder(ConditionalFormatting cf) {
        return topBorders.containsKey(cf);
    }

    int getTopBorder(ConditionalFormatting cf) {
        return topBorders.get(cf);
    }

    boolean hasLeftBorder(ConditionalFormatting cf) {
        return leftBorders.containsKey(cf);
    }

    int getLeftBorder(ConditionalFormatting cf) {
        return leftBorders.get(cf);
    }

    /**
     * Builds the rule's own border CSS into {@code css}, and as a side effect
     * registers any spillover top/left borders into the {@code topBorders} /
     * {@code leftBorders} maps. Returns the next {@code cssIndex} to use (may
     * be advanced by 0, 1, or 2 depending on how many spillover borders the
     * rule has). The border colours come from {@code colorConverter}.
     */
    int addBorderFormatting(ConditionalFormatting cf,
            ConditionalFormattingRule rule, StringBuilder css, int cssIndex,
            HashMap<Integer, String> conditionalFormattingStyles,
            ColorConverter colorConverter) {

        if (!(rule instanceof XSSFConditionalFormattingRule)) {
            // HSSF not supported
            return cssIndex;
        }

        XSSFBorderFormatting borderFormatting = (XSSFBorderFormatting) rule
                .getBorderFormatting();
        if (borderFormatting != null) {

            BorderStyle borderLeft = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderLeft());
            BorderStyle borderRight = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderRight());
            BorderStyle borderTop = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderTop());
            BorderStyle borderBottom = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderBottom());

            // In Excel, we can set a border to 'none', which overrides previous
            // rules. Default is 'not set', in which case we add no CSS.
            boolean isLeftSet = POIReflectionTools.isBorderSet(borderFormatting,
                    BorderSide.LEFT);
            boolean isTopSet = POIReflectionTools.isBorderSet(borderFormatting,
                    BorderSide.TOP);
            boolean isRightSet = POIReflectionTools
                    .isBorderSet(borderFormatting, BorderSide.RIGHT);
            boolean isBottomSet = POIReflectionTools
                    .isBorderSet(borderFormatting, BorderSide.BOTTOM);

            if (isRightSet) {
                css.append("border-right:");
                if (borderRight != BorderStyle.NONE) {
                    css.append(borderRight.getBorderAttributeValue());
                    css.append(
                            colorConverter.getBorderColorCSS(BorderSide.RIGHT,
                                    "border-right-color", borderFormatting));
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }
            if (isBottomSet) {
                css.append("border-bottom:");
                if (borderBottom != BorderStyle.NONE) {
                    css.append(borderBottom.getBorderAttributeValue());
                    css.append(
                            colorConverter.getBorderColorCSS(BorderSide.BOTTOM,
                                    "border-bottom-color", borderFormatting));
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }

            if (isTopSet) {
                // bottom border for cell above
                final StringBuilder sb2 = new StringBuilder("border-bottom:");
                if (borderTop != BorderStyle.NONE) {
                    sb2.append(borderTop.getBorderAttributeValue());
                    sb2.append(colorConverter.getBorderColorCSS(BorderSide.TOP,
                            "border-bottom-color", borderFormatting));

                    conditionalFormattingStyles.put(cssIndex, sb2.toString());
                    topBorders.put(cf, cssIndex++);
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }

            if (isLeftSet) {
                // right border for cell to the left
                final StringBuilder sb2 = new StringBuilder("border-right:");
                if (borderLeft != BorderStyle.NONE) {
                    sb2.append(borderLeft.getBorderAttributeValue());
                    sb2.append(colorConverter.getBorderColorCSS(BorderSide.LEFT,
                            "border-right-color", borderFormatting));

                    conditionalFormattingStyles.put(cssIndex, sb2.toString());
                    leftBorders.put(cf, cssIndex++);
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }
        }

        return cssIndex;
    }
}
