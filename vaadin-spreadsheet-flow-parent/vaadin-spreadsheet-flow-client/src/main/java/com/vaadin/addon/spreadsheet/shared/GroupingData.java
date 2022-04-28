package com.vaadin.addon.spreadsheet.shared;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Shared state for the grouping feature
 */
@SuppressWarnings("serial")
@JsType
public class GroupingData implements Serializable {
    public int startIndex;
    public int endIndex;
    public int level;
    /**
     * index unique for this group, for collapse/expand
     */
    public int uniqueIndex;
    public boolean collapsed;

    @JsIgnore
    public GroupingData() {
    }

    @JsIgnore
    public GroupingData(long start, long end, short level, long unique,
            boolean coll) {
        this((int) start, (int) end, (int) level, (int) unique, coll);
    }

    @JsIgnore
    public GroupingData(int start, int end, int level, int unique,
            boolean coll) {
        startIndex = start;
        endIndex = end;
        this.level = level;
        uniqueIndex = unique;
        collapsed = coll;
    }

}
