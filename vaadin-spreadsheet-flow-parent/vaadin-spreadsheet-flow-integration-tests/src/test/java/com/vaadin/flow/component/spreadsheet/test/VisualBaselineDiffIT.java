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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.Parameters;

/**
 * Compares how the sample workbooks render with the reference screenshots that
 * {@link VisualBaselineCaptureIT} writes.
 * <p>
 * The references are captured in Chrome on macOS with this module's own
 * application shell, so they hold for this module's test setup only. The test
 * runs only when the module's failsafe configuration sets the
 * {@code spreadsheet.visualBaseline.diff} system property, next to the
 * screenshot comparison settings. Anywhere else, such as in the merged
 * integration test module that CI runs, it is skipped.
 * <p>
 * A screenshot matches its reference when no 16x16 block of pixels differs from
 * the reference by more than the configured tolerance. A missing reference
 * fails the test, reported together with any mismatches.
 */
@TestPath("vaadin-spreadsheet")
public class VisualBaselineDiffIT extends AbstractVisualBaselineIT {

    static final String ENABLED_PROPERTY = "spreadsheet.visualBaseline.diff";

    @Before
    public void requireModuleSetup() {
        Assume.assumeTrue("Skipped: the reference screenshots hold only for "
                + "the setup of the spreadsheet integration test module, "
                + "whose failsafe configuration sets " + ENABLED_PROPERTY
                + "=true.", Boolean.getBoolean(ENABLED_PROPERTY));
    }

    @Test
    public void sampleWorkbooks_renderAsReferenceScreenshots()
            throws IOException {
        List<String> mismatches = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String workbook : WORKBOOKS) {
            File reference = getReferenceScreenshot(workbook);
            if (!reference.isFile()) {
                missing.add(workbook + " (" + reference.getPath() + ")");
                continue;
            }
            openWorkbook(workbook);
            if (!getCommandExecutor().compareScreen(ImageIO.read(reference),
                    toDirectoryName(workbook) + "_chrome_diff")) {
                mismatches.add(workbook);
            }
        }

        List<String> problems = new ArrayList<>();
        if (!mismatches.isEmpty()) {
            problems.add("Rendering differs from the reference screenshot for "
                    + mismatches + ". The screenshots and their differences "
                    + "are in " + Parameters.getScreenshotErrorDirectory()
                    + ".");
        }
        if (!missing.isEmpty()) {
            problems.add("No reference screenshot for " + missing
                    + ". Run VisualBaselineCaptureIT with -D"
                    + VisualBaselineCaptureIT.ENABLED_PROPERTY
                    + "=true to capture them.");
        }
        Assert.assertTrue(String.join(" ", problems), problems.isEmpty());
    }
}
