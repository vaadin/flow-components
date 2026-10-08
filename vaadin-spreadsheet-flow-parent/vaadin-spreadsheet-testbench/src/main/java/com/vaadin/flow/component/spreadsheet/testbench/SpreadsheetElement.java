/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.Optional;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * This is the base element class for accessing a Vaadin Spreadsheet component
 * for TestBench testing.
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
        // Cells and the merges drawn over them are divs; the prefix keeps
        // anything else with a cell's classes from answering to them, as
        // the GWT client's inline editor did.
        String selector = String.format("div.col%d.row%d.cell", column, row);
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
                                const container = root.querySelector('.sheet-scroll');
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
     * Found by its own class: it is not the only element with an inline
     * overflow.
     */
    private TestBenchElement scrollContainer() {
        WebElement el = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript("""
                        const host = arguments[0];
                        return host.shadowRoot
                            ? host.shadowRoot.querySelector('.sheet-scroll')
                            : null;
                        """, this);
        if (el == null) {
            throw new NoSuchElementException("scroll container not found");
        }
        return wrapElement(el, getCommandExecutor());
    }

    /**
     * Scrolls the sheet selector all the way back to the first tab.
     * <p>
     * Has no effect if there are not enough sheets to require scrolling.
     */
    public void scrollSheetsToStart() {
        clickTabScrollButton(".scroll-tabs-beginning");
    }

    /**
     * Scrolls the sheet selector as far as it goes, so that it shows the last
     * tab.
     * <p>
     * Has no effect if there are not enough sheets to require scrolling.
     */
    public void scrollSheetsToEnd() {
        clickTabScrollButton(".scroll-tabs-end");
    }

    /**
     * Scrolls the sheet selector by the given number of tabs, one tab at a
     * time. Scrolling stops at either end of the selector.
     * <p>
     * Has no effect if there are not enough sheets to require scrolling.
     *
     * @param amount
     *            how many tabs to scroll; positive numbers scroll to the right
     *            and negative numbers back towards the first tab
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
     * Selects the sheet at the given index by clicking its tab. Indexes are
     * counted only for visible sheets: a hidden sheet has no tab. When the tab
     * is scrolled out of the sheet selector, the selector is scrolled to show
     * it first.
     *
     * @param sheetIndex
     *            Index of sheet to select, 0-based
     * @throws NoSuchElementException
     *             if there is no visible sheet at the given index
     */
    public void selectSheetAt(int sheetIndex) {
        if (!clickSheetTab(sheetIndex + 1, null)) {
            throw new NoSuchElementException(
                    "Sheet index out of range: " + sheetIndex);
        }
    }

    /**
     * Selects the sheet with the given name by clicking its tab. Only visible
     * sheets can be selected: a hidden sheet has no tab. When the tab is
     * scrolled out of the sheet selector, the selector is scrolled to show it
     * first.
     * <p>
     * Selects nothing when no visible sheet has the given name.
     *
     * @param sheetName
     *            Name of sheet to select
     */
    public void selectSheet(String sheetName) {
        clickSheetTab(0, sheetName);
    }

    /**
     * Scrolls the sheet selector until it shows the tab of the given sheet, and
     * clicks the tab.
     *
     * @param index
     *            the sheet's index among the visible sheets, 1-based, or
     *            {@code 0} to find the sheet by its name
     * @param name
     *            the sheet's name, or {@code null} to find the sheet by its
     *            index
     * @return {@code false} if there is no such sheet
     */
    private boolean clickSheetTab(int index, String name) {
        // The selector scrolls by whole tabs with its own buttons, and has
        // no tab in the DOM for the sheets before the first one it shows,
        // while the tabs past its right end are there but cut off. A tab
        // that is missing is therefore found by scrolling back to the
        // start, and a tab that is cut off by scrolling forward until it
        // shows whole - or as whole as it gets when it is wider than the
        // selector. Done in one script, waiting for the tabs to render
        // after each step, rather than a round trip per tab.
        WebElement tab = (WebElement) getCommandExecutor().getDriver()
                .executeAsyncScript(
                        """
                                const [host, index, name] = arguments;
                                const done = arguments[arguments.length - 1];
                                const strip = host.shadowRoot && host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-sheet-tabs');
                                const root = strip && strip.shadowRoot;
                                if (!root) {
                                    done(null);
                                    return;
                                }
                                const renderedTabs = () => Array.from(root.querySelectorAll('.sheet-tab'));
                                const isWanted = (tab) => name === null
                                    ? Number(tab.dataset.index) === index
                                    : tab.title === name;
                                // A button that cannot scroll any further carries
                                // the hidden class.
                                const enabledButton = (selector) => {
                                    const button = root.querySelector(selector);
                                    return button && !button.classList.contains('hidden')
                                        ? button : null;
                                };
                                // Tabs show between the scroll buttons and the
                                // status label, when there is one.
                                const isShownWhole = (tab) => {
                                    const shown = root.querySelector('.sheet-tabsheet-container')
                                        .getBoundingClientRect();
                                    const label = root.querySelector('.sheet-tabsheet-infolabel');
                                    const right = label
                                        ? Math.min(shown.right, label.getBoundingClientRect().left)
                                        : shown.right;
                                    const box = tab.getBoundingClientRect();
                                    return box.left >= shown.left - 0.5 && box.right <= right + 0.5;
                                };
                                // Every tab from the first one shown is in the DOM,
                                // so the last one carries the number of sheets.
                                const initialTabs = renderedTabs();
                                const sheetCount = initialTabs.length
                                    ? Number(initialTabs[initialTabs.length - 1].dataset.index) : 0;
                                if (name === null && (index < 1 || index > sheetCount)) {
                                    done(null);
                                    return;
                                }
                                (async () => {
                                    // One step back to the start, and at most one
                                    // step forward per sheet.
                                    for (let step = 0; step <= sheetCount + 1; step++) {
                                        const tabs = renderedTabs();
                                        const tab = tabs.find(isWanted);
                                        let button;
                                        if (!tab) {
                                            button = enabledButton('.scroll-tabs-beginning');
                                            if (!button) {
                                                done(null);
                                                return;
                                            }
                                        } else if (tab === tabs[0] || isShownWhole(tab)) {
                                            done(tab);
                                            return;
                                        } else {
                                            button = enabledButton('.scroll-tabs-right');
                                            if (!button) {
                                                done(tab);
                                                return;
                                            }
                                        }
                                        button.click();
                                        while (!(await strip.updateComplete)) {
                                            // Rendered again after measuring the tabs.
                                        }
                                    }
                                    done(renderedTabs().find(isWanted) || null);
                                })();
                                """,
                        this, index, name);
        if (tab == null) {
            return false;
        }
        wrapElement(tab, getCommandExecutor()).click();
        return true;
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
     * Fetches the context menu for the spreadsheet. A menu that is fading out
     * as it closes is not open.
     *
     * @return {@link SpreadsheetElement.ContextMenuElement}
     * @throws NoSuchElementException
     *             if the menu isn't open
     */
    public ContextMenuElement getContextMenu() {
        // The menu is inside the spreadsheet's shadow root, not in the
        // document body.
        WebElement cm = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const menu = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-context-menu');
                                return menu && menu.shadowRoot
                                    ? menu.shadowRoot.querySelector('.menu:not(.animate-out)')
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
         * Gets the menu item whose caption contains the given text. Several
         * captions name what they act on, such as "Delete row 1", so the text
         * can be the part of the caption that does not change.
         *
         * @param text
         *            the text to look for in the captions
         * @return the first menu item whose caption contains the text, or
         *         {@code null} if there is none
         */
        public WebElement getItem(String text) {
            // Matched on textContent: getText() reports the empty string for
            // the items, which are in the menu's shadow root.
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
        // A cell in the selected range other than the active cell carries the
        // cell-range part; a solid outline marks such a cell too.
        String partAttr = Optional.ofNullable(element.getDomAttribute("part"))
                .orElse("");
        return partAttr.contains("cell-range")
                || "solid".equals(element.getCssValue("outline-style"));
    }

    /**
     * Gets the input of the inline cell editor, which holds the value of the
     * cell being edited.
     *
     * @return the input, or {@code null} when no cell is being edited
     */
    public WebElement getCellValueInput() {
        // The editor is in the DOM only while a cell is being edited.
        return findInCellGrid("[part~=\"cell-input\"]");
    }

    /**
     * Determine if the pop-up of PopupButton is currently visible. A pop-up
     * that is fading out as it closes counts as closed.
     *
     * @return {@code true} if the pop-up is visible, {@code false} otherwise
     */
    public boolean isPopupButtonPopupVisible() {
        // The pop-up is an overlay inside the spreadsheet's shadow root, so
        // only this spreadsheet's pop-up counts.
        Object visible = ((JavascriptExecutor) getCommandExecutor().getDriver())
                .executeScript("""
                        const overlay = arguments[0].shadowRoot
                            .querySelector('.popup-overlay');
                        return !!overlay && !overlay.hidden
                            && !overlay.classList.contains('animate-out');
                        """, this);
        return Boolean.TRUE.equals(visible);
    }
}
