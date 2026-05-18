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

import com.vaadin.flow.component.HasValue;

/**
 * Carrier passed from {@link FormAIController} to {@link FormAITools} for one
 * visible (non-ignored) form field. The id is the opaque UUID assigned at
 * discovery time; the {@link FormFieldHints} reference is live so live label,
 * helper text, current value, and any post-construction hint updates are read
 * fresh on each tool call.
 */
record FormFieldEntry(String id, HasValue<?, ?> field, FormFieldType type,
        FormFieldHints hints) {
}
