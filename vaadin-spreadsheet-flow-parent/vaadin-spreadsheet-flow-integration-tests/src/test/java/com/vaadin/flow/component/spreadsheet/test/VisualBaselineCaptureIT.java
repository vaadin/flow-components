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
import java.nio.file.Files;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.vaadin.flow.testutil.TestPath;

/**
 * Writes the reference screenshots that {@link VisualBaselineDiffIT} compares
 * with, one for each sample workbook, into the screenshot reference directory
 * that this module's failsafe configuration sets:
 * {@code src/reference-screenshots/<workbook>/1280x720-dpr1-chrome.png}.
 * <p>
 * The test writes into the source tree, so it runs only when the
 * {@code spreadsheet.visualBaseline.capture} system property is {@code true},
 * for example with {@code -Dspreadsheet.visualBaseline.capture=true}. Review
 * the screenshots it writes before committing them.
 */
@TestPath("vaadin-spreadsheet")
public class VisualBaselineCaptureIT extends AbstractVisualBaselineIT {

    static final String ENABLED_PROPERTY = "spreadsheet.visualBaseline.capture";

    @Before
    public void requireCaptureEnabled() {
        Assume.assumeTrue(
                "Skipped: the reference screenshots are captured "
                        + "only with -D" + ENABLED_PROPERTY + "=true.",
                Boolean.getBoolean(ENABLED_PROPERTY));
    }

    @Test
    public void sampleWorkbooks_writeReferenceScreenshots() throws IOException {
        for (String workbook : WORKBOOKS) {
            openWorkbook(workbook);

            File reference = getReferenceScreenshot(workbook);
            Files.createDirectories(reference.getParentFile().toPath());
            byte[] png = ((TakesScreenshot) getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            Files.write(reference.toPath(), png);

            System.out.println("[visual baseline] wrote " + reference + " ("
                    + png.length + " bytes) for " + workbook);
        }
    }
}
