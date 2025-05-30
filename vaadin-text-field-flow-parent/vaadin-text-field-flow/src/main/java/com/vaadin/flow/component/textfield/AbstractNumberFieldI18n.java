/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield;

import java.io.Serializable;

/**
 * The internationalization properties for {@link AbstractNumberField}.
 */
public interface AbstractNumberFieldI18n extends Serializable {
    /**
     * Gets the error message displayed when the field contains user input that
     * the server is unable to convert to type {@link Number}.
     *
     * @return the error message or {@code null} if not set
     */
    String getBadInputErrorMessage();

    /**
     * Gets the error message displayed when the field is required but empty.
     *
     * @return the error message or {@code null} if not set
     */
    String getRequiredErrorMessage();

    /**
     * Gets the error message displayed when the field value is smaller than the
     * minimum allowed value.
     *
     * @return the error message or {@code null} if not set
     */
    String getMinErrorMessage();

    /**
     * Gets the error message displayed when the field value is greater than the
     * maximum allowed value.
     *
     * @return the error message or {@code null} if not set
     */
    String getMaxErrorMessage();

    /**
     * Gets the error message displayed when the field value is not a multiple
     * of the step value.
     *
     * @return the error message or {@code null} if not set
     */
    String getStepErrorMessage();
}
