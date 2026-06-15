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
import java.util.Collections;
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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
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
 * Reflection access is required because POI's public
 * {@code ConditionalFormatting} and {@code BorderFormatting} types don't
 * expose: rule priority, {@code stopIfTrue}, "border is set vs unset" (distinct
 * from "border is none"), or font strikethrough on conditional-formatting
 * rules. Future POI versions may add public APIs for these; this class is the
 * seam to swap to them.
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

        } catch (NoSuchFieldException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (SecurityException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (IllegalAccessException e) {
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
     * Returns the XSSF rules of {@code cf} sorted by their priority field (read
     * via reflection, since POI doesn't expose it). Higher priority comes
     * first.
     */
    static List<XSSFConditionalFormattingRule> getOrderedRuleList(
            ConditionalFormatting cf) {
        XSSFConditionalFormatting xcf = (XSSFConditionalFormatting) cf;
        List<XSSFConditionalFormattingRule> rules = new ArrayList<XSSFConditionalFormattingRule>();
        for (int i = 0; i < xcf.getNumberOfRules(); i++) {
            rules.add(xcf.getRule(i));
        }

        Collections.sort(rules,
                new Comparator<XSSFConditionalFormattingRule>() {

                    @Override
                    public int compare(XSSFConditionalFormattingRule o1,
                            XSSFConditionalFormattingRule o2) {

                        CTCfRule object = (CTCfRule) getFieldVal(o1, "_cfRule");
                        CTCfRule object2 = (CTCfRule) getFieldVal(o2,
                                "_cfRule");

                        if (object != null && object2 != null) {
                            // reverse order
                            return object2.getPriority() - object.getPriority();
                        }

                        return 0;
                    }
                });

        return rules;
    }

    /**
     * {@code true} if {@code rule} has its {@code stopIfTrue} flag set
     * (reflectively read since POI's API doesn't expose it).
     */
    static boolean stopHere(ConditionalFormattingRule rule) {
        if (rule instanceof XSSFConditionalFormattingRule) {
            CTCfRule ctRule = (CTCfRule) getFieldVal(rule, "_cfRule");
            if (ctRule != null) {
                return ctRule.getStopIfTrue();
            }
        }
        return false;
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

        switch (b) {
        case LEFT:
            return ctBorder.isSetLeft();
        case TOP:
            return ctBorder.isSetTop();
        case RIGHT:
            return ctBorder.isSetRight();
        case BOTTOM:
            return ctBorder.isSetBottom();
        case DIAGONAL:
            return ctBorder.isSetDiagonal();
        case HORIZONTAL:
            return ctBorder.isSetHorizontal();
        case VERTICAL:
            return ctBorder.isSetVertical();
        default:
            break;
        }

        return false;
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
