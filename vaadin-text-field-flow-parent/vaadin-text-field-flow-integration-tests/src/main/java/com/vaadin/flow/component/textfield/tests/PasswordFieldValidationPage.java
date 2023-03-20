
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

/**
 * View for testing validation with {@link PasswordField}.
 */
@Route("vaadin-text-field/passwork-field-validation")
public class PasswordFieldValidationPage extends ValidationTestView {

    @Override
    protected HasValidation getValidationComponent() {
        return new PasswordField();
    }
}
