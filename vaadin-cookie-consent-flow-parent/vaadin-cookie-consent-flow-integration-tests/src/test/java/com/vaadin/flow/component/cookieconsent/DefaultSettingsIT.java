/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent;

import org.junit.Test;

import com.vaadin.flow.component.confirmdialog.examples.DefaultSettings;
import com.vaadin.flow.component.cookieconsent.testbench.CookieConsentElement.DefaultValues;

public class DefaultSettingsIT extends AbstractParallelTest {

    @Test
    public void test() throws Exception {
        open(DefaultSettings.class, WINDOW_SIZE_SMALL);
        verifyElement(DefaultValues.MESSAGE, DefaultValues.DISMISS_LABEL,
                DefaultValues.LEARN_MORE_LABEL, DefaultValues.LEARN_MORE_LINK,
                DefaultValues.POSITION);

    }
}
