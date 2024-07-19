
/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.vaadin.flow.component.popover;

/**
 * Popover position in relation to the target element.
 */
public enum PopoverPosition {
    //@formatter:off
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
    //@formatter:off

    private final String position;

    PopoverPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}
