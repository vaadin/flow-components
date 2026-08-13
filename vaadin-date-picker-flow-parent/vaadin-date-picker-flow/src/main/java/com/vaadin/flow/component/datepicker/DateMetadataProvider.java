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
import java.util.Collection;

/**
 * A callback that provides metadata for a range of dates, for example to mark
 * dates as not selectable.
 * <p>
 * The callback runs on the server. The calendar calls it with the range of
 * dates it is about to show, which covers whole months, so a single query can
 * answer for all of them. Server-side validation calls it with the single date
 * being validated. Return an entry only for the dates that have metadata; dates
 * that are not mentioned have none.
 * <p>
 * Until the callback has answered, the affected dates render in a loading state
 * but <b>stay selectable</b>, so a slow callback does not make the calendar
 * unusable. A date selected before the answer arrives is reported invalid by
 * server-side validation.
 * <p>
 * Results are cached per month in the browser, but not on the server, so an
 * expensive implementation should cache its own results. Call
 * {@link DatePicker#refreshDateMetadata()} when the data behind the callback
 * has changed.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
@FunctionalInterface
public interface DateMetadataProvider extends Serializable {

    /**
     * Gets the metadata for the dates in the given range.
     *
     * @param range
     *            the range of dates to provide metadata for, covering whole
     *            months, never {@code null}
     * @return the metadata entries for the dates that have metadata, or an
     *         empty collection if none have
     */
    Collection<DateMetadata> getDateMetadata(DateRange range);
}
