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
package com.vaadin.flow.component.ai.common;

import java.io.Serializable;
import java.util.List;

/**
 * The source data the LLM reported alongside one value it produced: the
 * snippets it read and how sure it was. A value can have more than one extract
 * — a multi-select filled from several lines, or a total summed from several
 * line items.
 * <p>
 * A source describes the value it was reported with. Once the field no longer
 * holds that value, the source no longer applies.
 *
 * @param confidence
 *            how sure the model was, or {@code null} when the model did not
 *            report a level — meaning unknown, not low
 * @param extracts
 *            the snippets the model read, in the order reported; never
 *            {@code null}, possibly empty
 * @author Vaadin Ltd
 * @since 25.3
 */
public record ValueSource(ConfidenceLevel confidence,
        List<SourceExtract> extracts) implements Serializable {

    /**
     * Creates a new value source.
     *
     * @param confidence
     *            how sure the model was, may be {@code null}
     * @param extracts
     *            the snippets the model read; {@code null} is treated as an
     *            empty list
     */
    public ValueSource {
        extracts = extracts == null ? List.of() : List.copyOf(extracts);
    }
}
