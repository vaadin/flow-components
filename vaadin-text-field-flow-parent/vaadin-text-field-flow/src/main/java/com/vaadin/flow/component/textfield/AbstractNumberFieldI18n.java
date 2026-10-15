/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
