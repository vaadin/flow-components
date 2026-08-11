/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Metadata for a single date, returned from a {@link DateMetadataProvider}.
 *
 * @param date
 *            the date the metadata applies to, not {@code null}
 * @param disabled
 *            whether the date cannot be selected
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public record DateMetadata(LocalDate date,
        boolean disabled) implements Serializable {

    /**
     * Creates new metadata for the given date.
     *
     * @param date
     *            the date the metadata applies to, not {@code null}
     * @param disabled
     *            whether the date cannot be selected
     */
    public DateMetadata {
        Objects.requireNonNull(date, "Date cannot be null");
    }
}
