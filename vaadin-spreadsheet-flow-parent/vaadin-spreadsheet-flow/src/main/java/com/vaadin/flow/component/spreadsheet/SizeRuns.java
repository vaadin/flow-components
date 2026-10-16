/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Row heights and column widths as they go to the client: runs of equal sizes,
 * {@code {"runs": [size, count, size, count, ...]}}.
 * <p>
 * The client needs the size of every row and column, and nearly all of them are
 * the same: sent one by one, the heights of the 1,048,576 rows of the largest
 * sheet came to 5.6 MB, and went again whenever one of them changed.
 */
final class SizeRuns {

    private SizeRuns() {
    }

    /** The sizes as runs: each size, then how many in a row have it. */
    static Map<String, List<Number>> of(float[] sizes) {
        List<Number> runs = new ArrayList<>();
        int i = 0;
        while (i < sizes.length) {
            float size = sizes[i];
            int start = i;
            while (i < sizes.length && Float.compare(sizes[i], size) == 0) {
                i++;
            }
            runs.add(size);
            runs.add(i - start);
        }
        return Map.of("runs", runs);
    }

    /** The sizes as runs: each size, then how many in a row have it. */
    static Map<String, List<Number>> of(int[] sizes) {
        List<Number> runs = new ArrayList<>();
        int i = 0;
        while (i < sizes.length) {
            int size = sizes[i];
            int start = i;
            while (i < sizes.length && sizes[i] == size) {
                i++;
            }
            runs.add(size);
            runs.add(i - start);
        }
        return Map.of("runs", runs);
    }
}
