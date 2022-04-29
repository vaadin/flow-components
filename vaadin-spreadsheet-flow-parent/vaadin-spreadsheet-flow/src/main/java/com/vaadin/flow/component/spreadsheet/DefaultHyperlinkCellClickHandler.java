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

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;

/**
 * Default implementation of the {@link HyperlinkCellClickHandler} interface.
 * Handles links to cells in either the same or some other sheet, as well as
 * external URLs.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DefaultHyperlinkCellClickHandler
        implements HyperlinkCellClickHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(DefaultHyperlinkCellClickHandler.class);

    private final Spreadsheet spreadsheet;
    private HyperlinkOpenStyle openStyle;

    /**
     * @param spreadsheet
     */
    public DefaultHyperlinkCellClickHandler(Spreadsheet spreadsheet) {
        this(spreadsheet, HyperlinkOpenStyle.NewTab);
    }

    /**
     * @param spreadsheet
     * @param openStyle
     *            defaults to {@link HyperlinkOpenStyle#NewTab} if null
     */
    public DefaultHyperlinkCellClickHandler(Spreadsheet spreadsheet,
            HyperlinkOpenStyle openStyle) {
        this.spreadsheet = spreadsheet;
        this.openStyle = openStyle;
    }

    /**
     * expose for subclasses
     *
     * @return Spreadsheet for this handler
     */
    protected Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    /**
     * expose for subclasses
     *
     * @return the openStyle
     */
    protected HyperlinkOpenStyle getOpenStyle() {
        return openStyle;
    }

    /**
     * @param openStyle
     *            the openStyle to set (uses NewTab if null)
     */
    public void setOpenStyle(HyperlinkOpenStyle openStyle) {
        this.openStyle = openStyle;
    }

    /**
     * Called when a hyperlink cell has been clicked.
     *
     * @param cell
     * @param hyperlink
     *            may be null, only for Excel link relations, not formula
     */
    @Override
    public void onHyperLinkCellClick(Cell cell, Hyperlink hyperlink) {
        if (hyperlink != null && hyperlink.getAddress() != null) {
            if (hyperlink.getType() == HyperlinkType.DOCUMENT) { // internal
                navigateTo(cell, hyperlink.getAddress());
            } else {
                openExternalLink(hyperlink.getAddress());
            }
        } else if (isHyperlinkFormulaCell(cell)) {
            String address = getHyperlinkFunctionTarget(cell);

            // does nothing if no navigator present.
            // "#!" is an invalid start to an inter-sheet address
            // (null sheet name)
            if (address.startsWith("#!")) {
                UI ui = UI.getCurrent();
                // non-push fragment navigation - requires navigator
                ui.getPage().open(address.substring(2));
                // final Navigator navigator = ui == null ? null :
                // ui.getNavigator();
                // if (navigator != null)
                // navigator.navigateTo(address.substring(2));
            } else if (address.startsWith("#")) { // inter-sheet address
                navigateTo(cell, address.substring(1));
            } else if (address.startsWith("[") && address.contains("]")) {
                // FIXME: for now we assume that the hyperlink points to the
                // current file. Should check file name against
                // address.substring(1, address.indexOf("]"));
                navigateTo(cell, address.substring(address.indexOf("]") + 1));
            } else {
                openExternalLink(address);
            }
        }
    }

    /**
     * Navigate to a spreadsheet location
     */
    private void navigateTo(Cell cell, String address) {
        if (address.contains("!")) { // has sheet name -> change
            String currentSheetName = cell.getSheet().getSheetName();
            String sheetName = address.substring(0, address.indexOf("!"));
            String addressInSheet = address.substring(address.indexOf("!") + 1);
            if (!currentSheetName.equals(sheetName)) {
                int sheetPOIIndex = getSheetIndex(cell, sheetName);
                spreadsheet.setActiveSheetWithPOIIndex(sheetPOIIndex);
            }
            spreadsheet.initialSheetSelection = address;
            spreadsheet.getCellSelectionManager()
                    .onSheetAddressChanged(addressInSheet, true);
        } else {
            // change selection to cell within the same sheet
            spreadsheet.getCellSelectionManager().onSheetAddressChanged(address,
                    false);
        }
    }

    private int getSheetIndex(Cell cell, String rawSheetName) {
        // if name contains only numbers or contains spaces it's enclosed in
        // single quotes
        String sheetName = rawSheetName;
        if (sheetName.charAt(0) == '\''
                && sheetName.charAt(sheetName.length() - 1) == '\'') {
            sheetName = sheetName.substring(1, sheetName.length() - 1);
        }
        return cell.getSheet().getWorkbook().getSheetIndex(sheetName);

    }

    /**
     * Should only be called for cells {@link #isHyperlinkFormulaCell(Cell)}
     * returns true. Returns the target for tooltip use by default.
     * <p>
     * The address is inside the first argument:
     * <code>HYPERLINK("address","friendly name")</code> or
     * <code>HYPERLINK("#!viewName[/arguments]","friendly name")</code> or
     * <code>HYPERLINK(D5,"friendly name")</code> or
     * <code>HYPERLINK([arbitrary formula],"friendly name")</code>
     *
     * @param cell
     *            Target cell containing a hyperlink function
     * @return the address that the hyperlink function points to
     */
    @Override
    public String getHyperlinkFunctionTarget(Cell cell) {
        return getFirstArgumentFromFormula(cell);
    }

    /**
     * we parse the formula with a formula/POI trick so we don't have to use
     * tricky regular expressions that hit terminal runaway evaluation cases
     *
     * see: https://www.regular-expressions.info/catastrophic.html
     *
     * Instead, translate
     *
     * <pre>
     * HYPERLINK(arg1[, arg2])
     * to
     * IF(true, arg1[, arg2])
     * </pre>
     */
    protected String getFirstArgumentFromFormula(Cell cell) {
        String formula = cell.getCellFormula()
                .replaceFirst("(?i)hyperlink\\s*\\(", "IF(true, ");

        try {
            ValueEval value = ((WorkbookEvaluatorProvider) spreadsheet
                    .getFormulaEvaluator())._getWorkbookEvaluator().evaluate(
                            formula,
                            new CellReference(cell.getSheet().getSheetName(),
                                    cell.getRowIndex(), cell.getColumnIndex(),
                                    false, false));
            if (value instanceof StringEval) {
                return ((StringEval) value).getStringValue();
            }
        } catch (Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return "";
        }
        return "";

    }

    /**
     * Returns true if the cell contains a hyperlink function.
     *
     * @param cell
     *            Cell to investigate
     * @return True if hyperlink is found
     */
    public final static boolean isHyperlinkFormulaCell(Cell cell) {
        return cell != null && cell.getCellType() == CellType.FORMULA
                && cell.getCellFormula().startsWith("HYPERLINK(");
    }

    /**
     * Uses the {@link HyperlinkOpenStyle} to open link addresses.
     *
     * Subclass and override to use something else with the address.
     *
     * @param address
     *            to navigate to
     */
    protected void openExternalLink(String address) {
        if (openStyle != null) {
            openStyle.openExternalLink(address);
        } else {
            HyperlinkOpenStyle.NewTab.openExternalLink(address);
        }
    }

    /**
     * Choose how external links should open
     */
    public static enum HyperlinkOpenStyle {
        /**
         * Note: for backward compatibility this opens in a new window/tab, but
         * to do so it uses
         * {@link com.vaadin.flow.component.page.Page#open(String, String)}
         * which is deprecated, and in most browsers is blocked by popup
         * blocking privacy settings.
         */
        NewTab {
            @Override
            public void openExternalLink(String address) {
                UI.getCurrent().getPage().open(address, "_new");
            }
        },

        /**
         * replaces the contents of the current window/tab using
         * {@link com.vaadin.flow.component.page.Page#setLocation(String)}
         */
        Replace {
            @Override
            public void openExternalLink(String address) {
                UI.getCurrent().getPage().setLocation(address);
            }
        },;

        /**
         * open external link
         *
         * @param address
         */
        public abstract void openExternalLink(String address);
    }
}
