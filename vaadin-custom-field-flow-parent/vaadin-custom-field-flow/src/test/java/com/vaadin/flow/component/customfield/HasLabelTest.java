/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasLabel;

public class HasLabelTest {

    @Test
    public void customField() {
        CustomField<String> c = new CustomField<String>() {
            @Override
            protected String generateModelValue() {
                return null;
            }

            @Override
            protected void setPresentationValue(String newPresentationValue) {

            }
        };
        Assert.assertTrue(c instanceof HasLabel);
    }

}
