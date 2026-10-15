/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover;

/**
 * Popover position in relation to the target element.
 */
public enum PopoverPosition {
    TOP_START("top-start"),
    TOP("top"),
    TOP_END("top-end"),
    BOTTOM_START("bottom-start"),
    BOTTOM("bottom"),
    BOTTOM_END("bottom-end"),
    START_TOP("start-top"),
    START("start"),
    START_BOTTOM("start-bottom"),
    END_TOP("end-top"),
    END("end"),
    END_BOTTOM("end-bottom");

    private final String position;

    PopoverPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}
