/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.command;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTConditionalFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The conditional formattings that live entirely on one row, kept so an undo
 * can put them back.
 *
 * <p>
 * POI drops a formatting outright when the only row it covers is removed, and
 * shifting the rows back does not bring it back - it only moves the ones that
 * survived. Remembering the {@link ConditionalFormatting} object is not enough
 * either: POI disconnects the removed rule's XML, so the remembered object
 * throws as soon as it is read.
 *
 * <p>
 * What is remembered instead is the formatting's XML, as text. Text because an
 * undo command lives in the session and has to survive being serialized, and
 * XML because it carries every kind of rule exactly - conditions, colours, data
 * bars, icon sets - where copying rule by rule through POI's public API would
 * quietly lose whatever the copy did not cover.
 *
 * <p>
 * Reaching the XML needs the package-private {@code _cf} field of
 * {@code XSSFConditionalFormatting}; the same need for POI's hidden
 * conditional-formatting state is why {@code POIReflectionTools} exists in the
 * parent package. Only {@code .xlsx} keeps its formatting this way. For
 * {@code .xls} nothing is captured and the old behaviour stands, which is that
 * the formatting is lost.
 */
final class ConditionalFormattingSnapshot implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConditionalFormattingSnapshot.class);

    private final List<String> formattingXml = new ArrayList<>();

    /**
     * Captures the formattings that cover nothing but the given row.
     * <p>
     * Formattings that also cover other rows are left alone: POI keeps those
     * and adjusts their ranges.
     *
     * @param sheet
     *            the sheet the row belongs to
     * @param rowIndex
     *            the row about to be removed, 0-based
     */
    static ConditionalFormattingSnapshot ofRow(Sheet sheet, int rowIndex) {
        ConditionalFormattingSnapshot snapshot = new ConditionalFormattingSnapshot();
        if (!(sheet instanceof XSSFSheet)) {
            return snapshot;
        }
        SheetConditionalFormatting formattings = sheet
                .getSheetConditionalFormatting();
        for (int i = 0; i < formattings.getNumConditionalFormattings(); i++) {
            ConditionalFormatting formatting = formattings
                    .getConditionalFormattingAt(i);
            if (!coversOnlyRow(formatting, rowIndex)) {
                continue;
            }
            String xml = xmlOf(formatting);
            if (xml != null) {
                snapshot.formattingXml.add(xml);
            }
        }
        return snapshot;
    }

    /**
     * Puts the captured formattings back on the sheet.
     *
     * @param sheet
     *            the sheet to add them to
     */
    void restoreTo(Sheet sheet) {
        if (formattingXml.isEmpty() || !(sheet instanceof XSSFSheet xssf)) {
            return;
        }
        for (String xml : formattingXml) {
            try {
                CTConditionalFormatting parsed = CTConditionalFormatting.Factory
                        .parse(xml);
                xssf.getCTWorksheet().addNewConditionalFormatting().set(parsed);
            } catch (Exception e) {
                LOGGER.warn("Could not restore a conditional formatting that"
                        + " was removed with its row", e);
            }
        }
    }

    private static boolean coversOnlyRow(ConditionalFormatting formatting,
            int rowIndex) {
        CellRangeAddress[] ranges = formatting.getFormattingRanges();
        if (ranges.length == 0) {
            return false;
        }
        for (CellRangeAddress range : ranges) {
            if (range.getFirstRow() != rowIndex
                    || range.getLastRow() != rowIndex) {
                return false;
            }
        }
        return true;
    }

    /**
     * The formatting's XML, or {@code null} when this POI build does not lay it
     * out where we expect.
     */
    private static String xmlOf(ConditionalFormatting formatting) {
        try {
            Field field = formatting.getClass().getDeclaredField("_cf");
            field.setAccessible(true);
            Object ct = field.get(formatting);
            if (ct instanceof CTConditionalFormatting typed) {
                return typed.xmlText();
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Could not read a conditional formatting's XML, so it"
                    + " will not come back if its row is removed and the"
                    + " removal undone", e);
        }
        return null;
    }
}
