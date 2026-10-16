/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Row heights and column widths go to the client as runs of equal sizes: one
 * size per row came to 5.6 MB for the largest sheet.
 */
class SizeRunsTest {

    private static String sent(float[] heights) {
        return Serializer.serialize(SizeRuns.of(heights));
    }

    private static String sent(int[] widths) {
        return Serializer.serialize(SizeRuns.of(widths));
    }

    @Test
    void rowsAllOfOneHeight_areOneRun() {
        float[] heights = new float[1_048_576];
        Arrays.fill(heights, 15f);

        assertEquals("{\"runs\":[15.0,1048576]}", sent(heights));
    }

    @Test
    void rowsOfDifferentHeights_areARunEach_inOrder() {
        assertEquals("{\"runs\":[15.0,2,30.0,1,15.0,1]}",
                sent(new float[] { 15f, 15f, 30f, 15f }));
    }

    @Test
    void hiddenRows_areARunOfNoHeight() {
        assertEquals("{\"runs\":[15.0,1,0.0,2,15.0,1]}",
                sent(new float[] { 15f, 0f, 0f, 15f }));
    }

    @Test
    void noRows_areNoRuns() {
        assertEquals("{\"runs\":[]}", sent(new float[0]));
    }

    @Test
    void columns_areRunsOfWholePixels() {
        assertEquals("{\"runs\":[50,2,100,1,50,1]}",
                sent(new int[] { 50, 50, 100, 50 }));
    }

    @Test
    void noColumns_areNoRuns() {
        assertEquals("{\"runs\":[]}", sent(new int[0]));
    }
}
