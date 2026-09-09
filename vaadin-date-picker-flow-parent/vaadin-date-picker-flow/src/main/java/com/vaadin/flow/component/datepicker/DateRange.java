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
 * A range of dates, inclusive on both ends. A range of a single date has the
 * same {@code start} and {@code end}.
 *
 * @param start
 *            the first date of the range, not {@code null}
 * @param end
 *            the last date of the range, not {@code null}
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public record DateRange(LocalDate start,
        LocalDate end) implements Serializable {

    /**
     * Creates a new date range with the given start and end date.
     *
     * @param start
     *            the first date of the range, not {@code null}
     * @param end
     *            the last date of the range, not {@code null}
     * @throws IllegalArgumentException
     *             if start is after end
     */
    public DateRange {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date");
        }
    }
}
