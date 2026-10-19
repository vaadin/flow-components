/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFFontFormatting;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection-based bridges into POI's hidden state, used by
 * {@link ConditionalFormatter} to access XML-level conditional-formatting data
 * that the POI public API doesn't expose.
 *
 * <p>
 * All methods are {@code static} — the class has no state, only utility
 * functions that read fields out of POI objects via reflection. Used by
 * {@link ConditionalFormatter} and intentionally not exposed beyond the
 * package; the public methods of {@link ConditionalFormatter} that call into
 * these utilities are the right API for downstream code.
 *
 * <p>
 * Reflection access is still required because POI's public
 * {@code BorderFormatting} type doesn't expose the "border is set vs unset"
 * distinction (separate from "border is none"), and the {@code FontFormatting}
 * type's {@code isStruckout} only checks the first strike entry while we need
 * to detect a {@code val=true} on ANY entry in the XML strike list. Rule
 * priority and {@code stopIfTrue} are public on
 * {@link XSSFConditionalFormattingRule} as of POI 5+ and read directly.
 */
final class POIReflectionTools {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(POIReflectionTools.class);

    private POIReflectionTools() {
    }

    /**
     * Reflectively reads the field named {@code fieldName} from {@code owner}.
     * Returns {@code null} (logging at error level) on any reflection failure —
     * including the case where the field doesn't exist on this POI version.
     */
    static Object getFieldVal(Object owner, String fieldName) {
        Field f = null;
        Object val = null;
        try {
            f = owner.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);

            val = f.get(owner);
            return val;

        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } finally {
            if (f != null) {
                f.setAccessible(false);
            }
        }

        return null;
    }

    /**
     * Returns the XSSF rules of {@code cf} sorted by priority descending.
     * Higher priority comes first.
     */
    static List<XSSFConditionalFormattingRule> getOrderedRuleList(
            ConditionalFormatting cf) {
        XSSFConditionalFormatting xcf = (XSSFConditionalFormatting) cf;
        List<XSSFConditionalFormattingRule> rules = new ArrayList<>();
        for (int i = 0; i < xcf.getNumberOfRules(); i++) {
            rules.add(xcf.getRule(i));
        }
        rules.sort(Comparator
                .comparingInt(XSSFConditionalFormattingRule::getPriority)
                .reversed());
        return rules;
    }

    /**
     * {@code true} if {@code rule} has its {@code stopIfTrue} flag set. Returns
     * false for non-XSSF rules.
     */
    static boolean stopHere(ConditionalFormattingRule rule) {
        return rule instanceof XSSFConditionalFormattingRule xssfRule
                && xssfRule.getStopIfTrue();
    }

    /**
     * {@code true} if the given border side has been explicitly set on
     * {@code borderFormatting} (distinct from "is none"; an explicitly-set
     * "none" border in a CF rule overrides the previous rule's border).
     */
    static boolean isBorderSet(XSSFBorderFormatting borderFormatting,
            BorderSide b) {

        CTBorder ctBorder = (CTBorder) getFieldVal(borderFormatting, "_border");

        if (ctBorder == null) {
            return false;
        }

        return switch (b) {
        case LEFT -> ctBorder.isSetLeft();
        case TOP -> ctBorder.isSetTop();
        case RIGHT -> ctBorder.isSetRight();
        case BOTTOM -> ctBorder.isSetBottom();
        case DIAGONAL -> ctBorder.isSetDiagonal();
        case HORIZONTAL -> ctBorder.isSetHorizontal();
        case VERTICAL -> ctBorder.isSetVertical();
        default -> false;
        };
    }

    /**
     * {@code true} if any strikethrough property on {@code fontFormatting} is
     * set to {@code val=true}. Falls through {@code false} for HSSF since this
     * is an XSSF-only XML detail.
     */
    static boolean hasStrikeThrough(FontFormatting fontFormatting) {
        if (fontFormatting instanceof XSSFFontFormatting) {
            CTFont font = (CTFont) getFieldVal(fontFormatting, "_font");

            if (font == null) {
                return false;
            }

            List<CTBooleanProperty> strikeList = font.getStrikeList();

            if (strikeList != null) {
                for (CTBooleanProperty p : strikeList) {
                    if (p.getVal()) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
