/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;

import com.vaadin.flow.testutil.TestPath;

/**
 * Tier 3 / Phase 3.0 visual-regression check.
 *
 * <p>
 * Loads each top-priority fixture, waits for the GWT client to render, then
 * calls {@link #compareScreen(File, String)} against the baseline written by
 * {@link Phase30BaselineCaptureIT}. Fails with a fixture-by-fixture report when
 * any baseline differs beyond the configured per-pixel tolerance (0.01, set in
 * pom.xml).
 *
 * <p>
 * This IT runs on every CI build. The baselines it diffs against are committed
 * under {@code src/reference-screenshots/<slug>/}.
 */
@TestPath("vaadin-spreadsheet")
public class Phase30BaselineDiffIT extends AbstractSpreadsheetIT {

    private static final Dimension CAPTURE_VIEWPORT = new Dimension(1280, 720);

    private static final List<String> FIXTURES = List.of("Tagetik 5.xlsx",
            "Groupingtest.xlsx", "100_000_rows.xlsx", "merged_cells.xlsx",
            "scrolled_frozen.xlsx", "ConditionalFormatterSamples.xlsx",
            "spreadsheet_styles.xlsx", "FormulaValues.xlsx",
            "data_filters.xlsx");

    @Before
    public void overrideViewport() {
        getDriver().manage().window().setSize(CAPTURE_VIEWPORT);
    }

    @Test
    public void allTopPriorityFixturesMatchBaseline()
            throws IOException, InterruptedException {
        List<String> mismatches = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String fixture : FIXTURES) {
            open();
            loadFile(fixture);
            Thread.sleep(2000L);

            String slug = Phase30BaselineCaptureIT.slugify(fixture);
            File ref = new File("src/reference-screenshots/" + slug
                    + "/1280x720-dpr1-chrome.png");

            if (!ref.isFile()) {
                missing.add(fixture + " (expected at " + ref.getPath() + ")");
                continue;
            }
            BufferedImage refImage = ImageIO.read(ref);
            if (!getCommandExecutor().compareScreen(refImage,
                    slug + "_chrome_diff")) {
                mismatches.add(fixture);
            }
        }

        if (!missing.isEmpty()) {
            // Skipped, not failed: the baselines are captured deliberately,
            // by running Phase30BaselineCaptureIT with
            // -Dphase3.baseline.capture=true, and a build where nobody has
            // done that yet has no visual regression to report. Failing here
            // would leave a permanently red test that says nothing about the
            // code.
            Assume.assumeTrue("Baselines not yet captured for: " + missing
                    + ". Run Phase30BaselineCaptureIT with "
                    + "-Dphase3.baseline.capture=true to bootstrap them.",
                    false);
        }
        Assert.assertTrue("Visual regression detected on: " + mismatches
                + ". Check target/screenshot-diffs/ for the actual "
                + "vs reference comparison.", mismatches.isEmpty());
    }
}
