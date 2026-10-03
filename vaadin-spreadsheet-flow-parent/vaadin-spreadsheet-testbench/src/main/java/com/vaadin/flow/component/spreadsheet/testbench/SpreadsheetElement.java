/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Base TestBench element for the Vaadin Spreadsheet component.
 *
 * <p>
 * Slice 177 (Tier 3 Phase 3.7): re-targeted from the GWT-era DOM to the
 * Lit-element DOM. The component now consists of nested shadow roots: the
 * {@code <vaadin-spreadsheet>} wrapper owns one, and each internal Lit
 * sub-element ({@code internal-formula-bar}, {@code internal-column-header},
 * {@code internal-row-header}, {@code internal-cell-grid},
 * {@code internal-sheet-tabs}) owns its own. This class traverses them via
 * JavaScript executor calls — the public Java method signatures here are
 * unchanged, so existing IT tests don't need updates.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-spreadsheet")
public class SpreadsheetElement extends TestBenchElement {

    /**
     * Find a single element in the cell-grid's shadow root via a CSS selector.
     * Returns {@code null} when not found (matches the legacy
     * {@code findElementInShadowRoot} return semantics).
     */
    private WebElement findInCellGrid(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const grid = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-cell-grid');
                                return grid && grid.shadowRoot
                                    ? grid.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find a single element in the column-header's shadow root.
     */
    private WebElement findInColumnHeader(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const ch = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-column-header');
                                return ch && ch.shadowRoot
                                    ? ch.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find a single element in the row-header's shadow root.
     */
    private WebElement findInRowHeader(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const rh = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-row-header');
                                return rh && rh.shadowRoot
                                    ? rh.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find an element in the formula-bar's shadow root.
     */
    private WebElement findInFormulaBar(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const fb = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-formula-bar');
                                return fb && fb.shadowRoot
                                    ? fb.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find an element in the sheet-tabs' shadow root.
     */
    private WebElement findInSheetTabs(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const st = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-sheet-tabs');
                                return st && st.shadowRoot
                                    ? st.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find all elements in the sheet-tabs' shadow root matching a selector.
     * Returns an empty list when the element isn't there.
     */
    @SuppressWarnings("unchecked")
    private List<WebElement> findAllInSheetTabs(String selector) {
        return (List<WebElement>) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const st = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-sheet-tabs');
                                return st && st.shadowRoot
                                    ? Array.from(st.shadowRoot.querySelectorAll(arguments[1]))
                                    : [];
                                """,
                        this, selector);
    }

    /**
     * Gets the cell element at the given coordinates for the currently active
     * sheet. Throws NoSuchElementException if the cell is outside the visible
     * area.
     *
     * @param row
     *            Row index, 1-based
     * @param column
     *            Column index, 1-based
     * @return Cell element at the given index.
     * @throws NoSuchElementException
     *             if the cell at (row, column) is not found.
     */
    public SheetCellElement getCellAt(int row, int column) {
        String selector = String.format(".col%d.row%d.cell", column, row);
        // Polls the cell-grid's shadow root for the cell — the grid may
        // not have rendered yet (e.g. just after a scroll). Where a merge
        // covers the coordinate two elements answer to it: the cell and
        // the merge drawn over it. The merge is what the user sees, so it
        // wins.
        WebElement cell = (WebElement) getCommandExecutor().getDriver()
                .executeAsyncScript(
                        """
                                const host = arguments[0];
                                const selector = arguments[1];
                                const callback = arguments[arguments.length - 1];
                                const MAX_ATTEMPTS = 50;
                                const delay = () => new Promise(r => setTimeout(r, 100));
                                async function locateGrid() {
                                    for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                        const grid = host.shadowRoot
                                            && host.shadowRoot.querySelector(
                                                'vaadin-spreadsheet-internal-cell-grid');
                                        if (grid && grid.shadowRoot) return grid;
                                        await delay();
                                    }
                                    return null;
                                }
                                async function findCell() {
                                    const grid = await locateGrid();
                                    if (!grid) return null;
                                    for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                        const matches = grid.shadowRoot
                                            .querySelectorAll(selector);
                                        if (matches.length) {
                                            for (const match of matches) {
                                                if (match.classList
                                                        .contains('merged-cell')) {
                                                    return match;
                                                }
                                            }
                                            return matches[0];
                                        }
                                        await delay();
                                    }
                                    return null;
                                }
                                findCell().then(callback);
                                """,
                        this, selector);
        if (cell == null) {
            throw new NoSuchElementException(
                    "Cell not found: row=" + row + ", column=" + column);
        }
        TestBenchElement wrapped = wrapElement(cell, getCommandExecutor());
        SheetCellElement cellElement = wrapped.wrap(SheetCellElement.class);
        cellElement.setParent(this);
        return cellElement;
    }

    /**
     * Gets the cell element at the given cell address for the currently active
     * sheet. Throws NoSuchElementException if the cell is outside the visible
     * area.
     *
     * @param cellAddress
     *            Target address, e.g. A3
     * @return Cell element at the given index.
     * @throws NoSuchElementException
     *             if the cell at (cellAddress) is not found.
     */
    public SheetCellElement getCellAt(String cellAddress) {
        Point point = AddressUtil.addressToPoint(cellAddress);
        return getCellAt(point.getY(), point.getX());
    }

    /**
     * Gets the row header element at the given index.
     *
     * @param rowIndex
     *            Index of target row, 1-based
     * @return Header of the row at the given index
     */
    public SheetHeaderElement getRowHeader(int rowIndex) {
        WebElement cell = findInRowHeader(
                String.format(".row-header-cell.row%d", rowIndex));
        if (cell == null) {
            throw new NoSuchElementException(
                    "Row header not found: " + rowIndex);
        }
        return wrapElement(cell, getCommandExecutor())
                .wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the column header element at the given index.
     *
     * @param columnIndex
     *            Index of target column, 1-based
     * @return Header of the column at the given index
     */
    public SheetHeaderElement getColumnHeader(int columnIndex) {
        WebElement cell = findInColumnHeader(
                String.format(".col-header-cell.col%d", columnIndex));
        if (cell == null) {
            throw new NoSuchElementException(
                    "Column header not found: " + columnIndex);
        }
        return wrapElement(cell, getCommandExecutor())
                .wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the address field. The address field contains the address of the
     * cell that was last clicked, or A1 if no clicks have yet been made.
     *
     * @return Address field element
     */
    public TestBenchElement getAddressField() {
        WebElement el = findInFormulaBar("[part~=\"address-field\"]");
        return el == null ? null
                : (TestBenchElement) wrapElement(el, getCommandExecutor());
    }

    /**
     * Gets the formula field. This field is where the user can input data for
     * the cell whose address the address field currently contains.
     *
     * @return Formula field element
     */
    public TestBenchElement getFormulaField() {
        WebElement el = findInFormulaBar("[part~=\"formula-field\"]");
        return el == null ? null
                : (TestBenchElement) wrapElement(el, getCommandExecutor());
    }

    /**
     * Gets the info label: the status label at the right end of the sheet tabs,
     * which {@code Spreadsheet.setStatusLabelValue} sets.
     *
     * @return the label, or {@code null} while no status label is set
     */
    public TestBenchElement getInfoLabel() {
        WebElement el = findInSheetTabs(".sheet-tabsheet-infolabel");
        return el == null ? null
                : (TestBenchElement) wrapElement(el, getCommandExecutor());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scroll(int)
     */
    @Override
    public void scroll(int scrollTop) {
        scrollContainer().scroll(scrollTop);
        waitUntilDrawnAtScrollPosition();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scrollLeft(int)
     */
    @Override
    public void scrollLeft(int scrollLeft) {
        scrollContainer().scrollLeft(scrollLeft);
        waitUntilDrawnAtScrollPosition();
    }

    /**
     * Waits until the sheet has drawn itself at its new scroll position.
     * <p>
     * The cells are drawn a frame after the scroll, and the cell elements are
     * reused for whichever cells the new position shows. A cell found and
     * clicked in between can turn out to be another cell by the time the click
     * lands, so a scroll is not over until the sheet has caught up.
     */
    private void waitUntilDrawnAtScrollPosition() {
        ((JavascriptExecutor) getCommandExecutor().getDriver())
                .executeAsyncScript(
                        """
                                const host = arguments[0];
                                const done = arguments[arguments.length - 1];
                                const root = host.shadowRoot;
                                const container = root.querySelector('div[style*="overflow"]');
                                const grid = root.querySelector('vaadin-spreadsheet-internal-cell-grid');
                                let tries = 0;
                                const check = () => {
                                    const state = grid && grid.renderState;
                                    const caughtUp = !state || (state.scrollLeft === container.scrollLeft
                                        && state.scrollTop === container.scrollTop);
                                    if (caughtUp || ++tries > 100) {
                                        requestAnimationFrame(() => requestAnimationFrame(() => done(null)));
                                    } else {
                                        setTimeout(check, 20);
                                    }
                                };
                                check();
                                """,
                        this);
    }

    /**
     * The Lit Spreadsheet's scroll container — a {@code <div>} inside the
     * host's shadow root that wraps the cell grid and its sticky-positioned
     * header strips. Lit equivalent of the legacy {@code .bottom-right-pane}.
     */
    private TestBenchElement scrollContainer() {
        WebElement el = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript("""
                        const host = arguments[0];
                        return host.shadowRoot
                            ? host.shadowRoot.querySelector(
                                'div[style*="overflow"]')
                            : null;
                        """, this);
        if (el == null) {
            throw new NoSuchElementException("scroll container not found");
        }
        return wrapElement(el, getCommandExecutor());
    }

    /**
     * Scrolls the sheet-tab strip all the way back to the first tab.
     */
    public void scrollSheetsToStart() {
        clickTabScrollButton(".scroll-tabs-beginning");
    }

    /**
     * Scrolls the sheet-tab strip all the way to the last tab.
     */
    public void scrollSheetsToEnd() {
        clickTabScrollButton(".scroll-tabs-end");
    }

    /**
     * Scrolls the sheet-tab strip one tab at a time.
     *
     * @param amount
     *            how many tabs to scroll; negative scrolls back towards the
     *            first tab
     */
    public void scrollSheets(int amount) {
        String selector = amount < 0 ? ".scroll-tabs-left"
                : ".scroll-tabs-right";
        for (int i = 0; i < Math.abs(amount); i++) {
            clickTabScrollButton(selector);
        }
    }

    /**
     * Clicks one of the tab-strip scroll buttons, or does nothing when the
     * strip is short enough that there is nowhere to scroll - the element marks
     * a button it cannot use with the {@code hidden} class.
     */
    private void clickTabScrollButton(String selector) {
        WebElement button = findInSheetTabs(selector);
        if (button == null) {
            return;
        }
        String classes = Optional.ofNullable(button.getDomAttribute("class"))
                .orElse("");
        if (classes.contains("hidden")) {
            return;
        }
        button.click();
    }

    /**
     * Selects the sheet at the given index. Indexes are counted only for
     * visible sheets.
     *
     * @param sheetIndex
     *            Index of sheet to select, 0-based
     */
    public void selectSheetAt(int sheetIndex) {
        List<WebElement> tabs = findAllInSheetTabs(".sheet-tab");
        if (sheetIndex < 0 || sheetIndex >= tabs.size()) {
            throw new NoSuchElementException(
                    "Sheet index out of range: " + sheetIndex);
        }
        tabs.get(sheetIndex).click();
    }

    /**
     * Selects the sheet with the given name. Only visible sheets can be
     * selected.
     *
     * @param sheetName
     *            Name of sheet to select
     */
    public void selectSheet(String sheetName) {
        List<WebElement> tabs = findAllInSheetTabs(".sheet-tab");
        for (WebElement tab : tabs) {
            if (tab.getText().equals(sheetName)) {
                tab.click();
                return;
            }
        }
    }

    /**
     * Adds a new sheet by clicking the tab strip's add button.
     *
     * @throws NoSuchElementException
     *             if the add button is not present
     */
    public void addSheet() {
        WebElement addButton = findInSheetTabs(".add-new-tab");
        if (addButton == null) {
            throw new NoSuchElementException("Add-sheet button not found");
        }
        addButton.click();
    }

    /**
     * Fetches the context menu for the spreadsheet. The menu is an internal
     * element inside the spreadsheet's shadow root, so it is found by
     * traversing the shadow roots rather than by a document query.
     *
     * @return {@link SpreadsheetElement.ContextMenuElement}
     * @throws java.util.NoSuchElementException
     *             if the menu isn't open
     */
    public ContextMenuElement getContextMenu() {
        WebElement cm = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const menu = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-context-menu');
                                return menu && menu.shadowRoot
                                    ? menu.shadowRoot.querySelector('.menu')
                                    : null;
                                """,
                        this);
        if (cm == null) {
            throw new NoSuchElementException("Context menu not found");
        }
        return wrapElement(cm, getCommandExecutor())
                .wrap(ContextMenuElement.class);
    }

    public static class ContextMenuElement extends TestBenchElement {

        /**
         * The menu item whose caption contains the given text, or {@code null}.
         * <p>
         * A substring match, because several captions name what they act on —
         * "Delete row 1", not "Delete row" — and callers ask for the stable
         * part. The xpath {@code contains()} lookup this replaces did the same.
         * <p>
         * Matched on {@code textContent} rather than
         * {@link WebElement#getText()}: the items live in the context menu's
         * shadow root, where getText() reports the empty string, so every
         * lookup came back null.
         */
        public WebElement getItem(String text) {
            return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                    .getDriver()).executeScript("""
                            const menu = arguments[0];
                            for (const item of menu.querySelectorAll('.item')) {
                                if (item.textContent.includes(arguments[1])) {
                                    return item;
                                }
                            }
                            return null;
                            """, this, text);
        }

    }

    boolean isElementSelected(WebElement element) {
        // Cheapest first: a cell inside the selection but outside the active
        // range says so on itself, and needs no outline.
        if (isNonCoherentlySelected(element)) {
            return true;
        }
        // The outline is drawn around the active range only, and the client
        // leaves it out while that range is scrolled out of view. With no
        // outline on screen and no marking on the cell, it is not selected.
        if (!findSelectionOutline()) {
            return false;
        }
        // A range across a freeze is drawn a piece per pane, so the range is
        // every piece together. Measured in the browser, the cell too, so
        // both are in the same coordinates.
        return Boolean.TRUE
                .equals(((JavascriptExecutor) getCommandExecutor().getDriver())
                        .executeScript(
                                """
                                        const [host, cell] = arguments;
                                        const grid = host.shadowRoot
                                            .querySelector('vaadin-spreadsheet-internal-cell-grid');
                                        const pieces = Array.from(grid.shadowRoot
                                            .querySelectorAll('.sheet-selection'))
                                            .map((piece) => piece.getBoundingClientRect());
                                        if (pieces.length === 0) {
                                            return false;
                                        }
                                        const left = Math.min(...pieces.map((r) => r.left));
                                        const top = Math.min(...pieces.map((r) => r.top));
                                        const right = Math.max(...pieces.map((r) => r.right));
                                        const bottom = Math.max(...pieces.map((r) => r.bottom));
                                        const box = cell.getBoundingClientRect();
                                        const x = box.left + box.width / 2;
                                        const y = box.top + box.height / 2;
                                        return x >= left && x <= right && y >= top && y <= bottom;
                                        """,
                                this, element));
    }

    /**
     * Whether the client is drawing a selection outline.
     *
     * @return {@code false} when the client is not drawing an outline
     */
    private boolean findSelectionOutline() {
        // sometimes the spreadsheet takes so long to load that the selection
        // widget elements are not found
        try {
            waitUntil(e -> findInCellGrid(".s-top") != null);
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    private boolean isNonCoherentlySelected(WebElement element) {
        // an element is non-coherently selected if the part attribute
        // contains "cell-range" — slice 173 emits part="cell cell-range"
        // on cells inside the active range that aren't the active cell.
        // Falls back to the legacy "outline-style: solid" CSS check.
        String partAttr = Optional.ofNullable(element.getDomAttribute("part"))
                .orElse("");
        return partAttr.contains("cell-range")
                || "solid".equals(element.getCssValue("outline-style"));
    }

    /**
     * The cell value input — Lit equivalent is the {@code <input>} inside the
     * cell-grid carrying {@code part="cell-input"}.
     */
    public WebElement getCellValueInput() {
        return findInCellGrid("[part~=\"cell-input\"]");
    }

    /**
     * Determine if the pop-up of PopupButton is currently visible.
     * <p>
     * The Lit client shows it in its own overlay inside the spreadsheet's
     * shadow root, positioned in the scroll container so it tracks the sheet,
     * rather than in the GWT-era {@code .v-spreadsheet-popupbutton-overlay}
     * floating div.
     */
    public boolean isPopupButtonPopupVisible() {
        Object visible = ((JavascriptExecutor) getCommandExecutor().getDriver())
                .executeScript("""
                        const overlay = arguments[0].shadowRoot
                            .querySelector('.popup-overlay');
                        return !!overlay && !overlay.hidden;
                        """, this);
        return Boolean.TRUE.equals(visible);
    }
}
