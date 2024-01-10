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
 */
package com.vaadin.flow.component.shared;

/**
 * Represents selection preservation mode on data change.
 * <p>
 * These enums should be used in
 * {@link SelectionPreservationHandler#setSelectionPreservationMode(SelectionPreservationMode)}
 * to switch between the implemented selection preservation modes.
 *
 * @see SelectionPreservationHandler
 * @author Vaadin Ltd.
 */
public enum SelectionPreservationMode {

    /**
     * Selection preservation mode that preserves all selected items on data
     * change.
     */
    PRESERVE_ALL,

    /**
     * Selection preservation mode that only preserves the selected items that
     * still exist after data change.
     */
    PRESERVE_EXISTING,

    /**
     * Selection preservation mode that clears selection on data change.
     */
    DISCARD
}
