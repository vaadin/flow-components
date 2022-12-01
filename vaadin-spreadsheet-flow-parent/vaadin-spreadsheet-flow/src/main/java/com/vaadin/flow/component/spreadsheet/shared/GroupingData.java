package com.vaadin.flow.component.spreadsheet.shared;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import java.io.Serializable;

/**
 * Shared state for the grouping feature
 */
public class GroupingData implements Serializable {
    public int startIndex;
    public int endIndex;
    public int level;
    /**
     * index unique for this group, for collapse/expand
     */
    public int uniqueIndex;
    public boolean collapsed;

    public GroupingData() {
    }

    public GroupingData(long start, long end, short level, long unique,
            boolean coll) {
        this((int) start, (int) end, (int) level, (int) unique, coll);
    }

    public GroupingData(int start, int end, int level, int unique,
            boolean coll) {
        startIndex = start;
        endIndex = end;
        this.level = level;
        uniqueIndex = unique;
        collapsed = coll;
    }

}
