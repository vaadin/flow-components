/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.testbench.Parameters;

/**
 * What {@link VisualBaselineCaptureIT} and {@link VisualBaselineDiffIT} share:
 * the sample workbooks, the window size, where the reference screenshots are,
 * and the wait for the spreadsheet to finish rendering before a screenshot.
 */
public abstract class AbstractVisualBaselineIT extends AbstractSpreadsheetIT {

    /** The workbooks that have a reference screenshot. */
    static final List<String> WORKBOOKS = List.of("Groupingtest.xlsx",
            "100_000_rows.xlsx", "merged_cells.xlsx", "scrolled_frozen.xlsx",
            "ConditionalFormatterSamples.xlsx", "spreadsheet_styles.xlsx",
            "FormulaValues.xlsx", "data_filters.xlsx");

    private static final Dimension WINDOW_SIZE = new Dimension(1280, 720);

    @Before
    public void setWindowSize() {
        getDriver().manage().window().setSize(WINDOW_SIZE);
    }

    /**
     * Opens the test page and loads the given workbook, then waits until the
     * spreadsheet has finished rendering it.
     *
     * @param workbook
     *            the file name of the workbook
     */
    protected void openWorkbook(String workbook) {
        open();
        loadFile(workbook);
        waitUntilRendered(workbook);
    }

    /**
     * Gets the reference screenshot of the given workbook, in the screenshot
     * reference directory that TestBench is configured with.
     *
     * @param workbook
     *            the file name of the workbook
     * @return the reference screenshot file, which may not exist
     */
    protected static File getReferenceScreenshot(String workbook) {
        File directory = new File(Parameters.getScreenshotReferenceDirectory(),
                toDirectoryName(workbook));
        return new File(directory, "1280x720-dpr1-chrome.png");
    }

    /**
     * Gets the name of the directory that holds the reference screenshot of the
     * given workbook: the file name without its extension, in lower case, with
     * each run of characters other than letters and digits replaced by a dash.
     *
     * @param workbook
     *            the file name of the workbook
     * @return the directory name
     */
    static String toDirectoryName(String workbook) {
        int dot = workbook.lastIndexOf('.');
        String stem = dot >= 0 ? workbook.substring(0, dot) : workbook;
        return stem.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Waits until the spreadsheet has rendered the loaded workbook and settled:
     * no request to the server is pending, the cells are drawn, images and
     * fonts have loaded, and nothing in the spreadsheet has changed for half a
     * second. The cells in view are sent again once the client has measured the
     * workbook's styles, a round trip after they first arrive, so the first
     * drawing of the cells is not the last.
     */
    private void waitUntilRendered(String workbook) {
        Object rendered = ((JavascriptExecutor) getDriver())
                .executeAsyncScript("""
                        const host = arguments[0];
                        const done = arguments[arguments.length - 1];
                        const quietMillis = 500;
                        const timeoutMillis = 20000;
                        const start = performance.now();
                        let lastChange = start;
                        const roots = [host, host.shadowRoot];
                        const collectShadowRoots = (node) => {
                            for (const element of node.querySelectorAll('*')) {
                                if (element.shadowRoot) {
                                    roots.push(element.shadowRoot);
                                    collectShadowRoots(element.shadowRoot);
                                }
                            }
                        };
                        collectShadowRoots(host.shadowRoot);
                        collectShadowRoots(host);
                        const observer = new MutationObserver(() => {
                            lastChange = performance.now();
                        });
                        roots.forEach((root) => observer.observe(root, {
                            subtree: true, childList: true,
                            attributes: true, characterData: true
                        }));
                        const flowIdle = () => Object.values(
                                (window.Vaadin && window.Vaadin.Flow
                                    && window.Vaadin.Flow.clients) || {})
                            .every((client) => !client.isActive());
                        const cellsDrawn = () => {
                            const grid = host.shadowRoot.querySelector(
                                'vaadin-spreadsheet-internal-cell-grid');
                            return !!(grid && grid.shadowRoot
                                && grid.shadowRoot.querySelector('div.cell'));
                        };
                        const imagesLoaded = () => roots.every((root) =>
                            Array.from(root.querySelectorAll('img'))
                                .every((image) => image.complete));
                        const check = () => {
                            const now = performance.now();
                            if (cellsDrawn() && flowIdle() && imagesLoaded()
                                    && document.fonts.status === 'loaded'
                                    && now - lastChange >= quietMillis) {
                                observer.disconnect();
                                // Painted, too.
                                requestAnimationFrame(() =>
                                    requestAnimationFrame(() => done(true)));
                            } else if (now - start > timeoutMillis) {
                                observer.disconnect();
                                done(false);
                            } else {
                                setTimeout(check, 50);
                            }
                        };
                        check();
                        """, getSpreadsheet());
        Assert.assertTrue(
                "The spreadsheet did not finish rendering " + workbook,
                Boolean.TRUE.equals(rendered));
    }
}
