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

import java.util.Locale;

/**
 * Enum for describing the popover trigger mode, used to configure how the
 * overlay is opened or closed on user interaction with the target.
 */
public enum PopoverTrigger {
    /**
     * Popover opens and closes on target click, either by mouse or keyboard
     * (especially for elements like buttons that click on Enter or Space).
     */
    CLICK,

    /**
     * Popover opens when the cursor is moved onto the target, and closes when
     * the cursor leaves the target (unless it moves onto the popover overlay).
     */
    HOVER,

    /**
     * Popover opens when the target component receives keyboard focus, and
     * closes when the focus leaves the target (unless it moves to the popover
     * content).
     */
    FOCUS;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
