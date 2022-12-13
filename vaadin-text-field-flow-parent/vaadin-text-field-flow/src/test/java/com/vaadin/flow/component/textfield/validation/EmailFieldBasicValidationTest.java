package com.vaadin.flow.component.textfield.validation;

import com.vaadin.flow.component.textfield.EmailField;

public class EmailFieldBasicValidationTest extends AbstractBasicValidationTest<EmailField> {
    protected EmailField createTestField() {
        return new EmailField();
    }
}
