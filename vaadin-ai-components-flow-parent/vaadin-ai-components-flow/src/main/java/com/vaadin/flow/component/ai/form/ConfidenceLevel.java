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
package com.vaadin.flow.component.ai.form;

/**
 * How sure the LLM was about a value it wrote to a field. A level is a rough
 * judgement reported by the model about itself, not a probability: it is useful
 * for ranking the fields of one fill against each other, and not for much else.
 * The meaning of each level is defined in the {@code fill_form} tool
 * description the LLM sees.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd
 */
enum ConfidenceLevel {

    /** The value is written in the source and copied as it is. */
    HIGH,

    /**
     * The value follows from the source but needed some interpretation, such as
     * combining fields, converting units, or choosing one candidate over
     * another.
     */
    MEDIUM,

    /** The source is unclear, or the value is a guess. */
    LOW
}
