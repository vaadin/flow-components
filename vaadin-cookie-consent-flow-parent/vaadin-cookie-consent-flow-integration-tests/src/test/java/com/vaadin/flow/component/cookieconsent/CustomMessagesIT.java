package com.vaadin.flow.component.cookieconsent;

import org.junit.Test;

import com.vaadin.flow.component.cookieconsent.examples.CustomMessages;

public class CustomMessagesIT extends AbstractParallelTest {

    @Test
    public void test() throws Exception {
        open(CustomMessages.class, WINDOW_SIZE_SMALL);
        verifyElement(CustomMessages.MESSAGE, CustomMessages.DISMISS_LABEL,
                CustomMessages.LEARN_MORE_LABEL, CustomMessages.LEARN_MORE_LINK,
                CustomMessages.POSITION);
    }

}
