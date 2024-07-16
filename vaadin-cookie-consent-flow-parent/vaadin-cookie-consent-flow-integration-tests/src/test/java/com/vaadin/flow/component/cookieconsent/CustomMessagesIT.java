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

import com.vaadin.flow.component.confirmdialog.examples.CustomMessages;

public class CustomMessagesIT extends AbstractParallelTest {

    @Test
    public void test() throws Exception {
        open(CustomMessages.class, WINDOW_SIZE_SMALL);
        verifyElement(CustomMessages.MESSAGE, CustomMessages.DISMISS_LABEL,
                CustomMessages.LEARN_MORE_LABEL, CustomMessages.LEARN_MORE_LINK,
                CustomMessages.POSITION);
    }

}
