/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.vaadin.flow.testutil.TestPath;

/**
 * Bootstraps Tier 3 / Phase 3.0 golden-screenshot baselines by loading each
 * top-priority fixture in the demo page and writing a PNG to
 * {@code src/reference-screenshots/<fixture-slug>/<viewport>.png}.
 *
 * <p>
 * This IT is gated behind {@code -Dphase3.baseline.capture=true} because it
 * writes into the source tree. The companion diff IT (deferred to a follow-up
 * slice) will read these baselines via TestBench {@code compareScreen} and run
 * on every CI build.
 *
 * <p>
 * Capture viewport: 1280x720, DPR 1, Chromium. Matches the decision in
 * {@code refactor-artifacts/tier3-phase-3.0/INFRA_PROPOSAL.md} § 1.1 (Phase 3.0
 * ships Chromium+Firefox only).
 */
@TestPath("vaadin-spreadsheet")
public class Phase30BaselineCaptureIT extends AbstractSpreadsheetIT {

    private static final Dimension CAPTURE_VIEWPORT = new Dimension(1280, 720);

    /**
     * Top-priority fixtures, adapted from
     * {@code refactor-artifacts/tier3-phase-3.0/FIXTURE_INVENTORY.md} §
     * "Recommended Phase 3.0 capture order". The original list named 10
     * fixtures; 3 of those ({@code Simple Invoice.xlsx}, {@code Grouping.xlsx},
     * {@code Formulas.xlsx}) live under {@code src/main/resources/testsheets/}
     * which the demo's {@code #testSheetSelect} combo does NOT expose
     * ({@code SpreadsheetPage} loads only from {@code test_sheets/}). Two of
     * those have substitutes in {@code test_sheets/}: {@code Formulas.xlsx} →
     * {@code FormulaValues.xlsx}, {@code Grouping.xlsx} →
     * {@code Groupingtest.xlsx}. Simple Invoice has no clean substitute, so
     * it's deferred to a later slice that will either add a load path for
     * {@code testsheets/} or pick a substitute.
     */
    private static final List<String> TOP_PRIORITY_FIXTURES = List.of(
            "Tagetik 5.xlsx", "Groupingtest.xlsx", "100_000_rows.xlsx",
            "merged_cells.xlsx", "scrolled_frozen.xlsx",
            "ConditionalFormatterSamples.xlsx", "spreadsheet_styles.xlsx",
            "FormulaValues.xlsx", "data_filters.xlsx");

    @Before
    public void captureGate() {
        Assume.assumeTrue(
                "Skipped: baseline capture only runs with -Dphase3.baseline.capture=true",
                Boolean.getBoolean("phase3.baseline.capture"));
        // Override the AbstractSpreadsheetIT 1920x1080 default with the
        // capture viewport from INFRA_PROPOSAL § 1.1.
        getDriver().manage().window().setSize(CAPTURE_VIEWPORT);
    }

    @Test
    public void captureTopPriorityFixtures() throws Exception {
        for (String fixture : TOP_PRIORITY_FIXTURES) {
            captureOne(fixture);
        }
    }

    private void captureOne(String fixture)
            throws IOException, InterruptedException {
        open();
        loadFile(fixture);

        // Give the client time to settle. compareScreen will retry, but
        // the bootstrap path doesn't go through compareScreen, so we
        // wait explicitly. 2s matches the per-fixture median observed
        // during manual Playwright capture (slice 122 PoC).
        Thread.sleep(2000L);

        Path target = Paths.get("src/reference-screenshots", slugify(fixture),
                "1280x720-dpr1-chrome.png");
        Files.createDirectories(target.getParent());

        byte[] png = ((TakesScreenshot) getDriver())
                .getScreenshotAs(OutputType.BYTES);
        Files.write(target, png);

        System.out.println("[baseline] wrote " + target + " (" + png.length
                + " bytes) for fixture: " + fixture);
    }

    /**
     * Convert a fixture filename (with spaces, extension) into a
     * filesystem-safe directory slug. Drops the extension, replaces
     * non-alphanumerics with {@code -}, lowercases.
     */
    static String slugify(String fixtureFilename) {
        int dot = fixtureFilename.lastIndexOf('.');
        String stem = dot >= 0 ? fixtureFilename.substring(0, dot)
                : fixtureFilename;
        return stem.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
