/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import java.io.Serializable;

/**
 * The internationalization properties for {@link ComboBoxBase}.
 */
public interface ComboBoxBaseI18n extends Serializable {
    /**
     * Gets the error message displayed when the field is required but empty.
     *
     * @return the error message or {@code null} if not set
     */
    String getRequiredErrorMessage();
}
