package com.vaadin.flow.component.select.tests.validation;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-select/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<Select<String>> {
    public static final String REQUIRED_BUTTON = "required-button";

    public static final String ATTACH_FIELD_BUTTON = "attach-field-button";
    public static final String DETACH_FIELD_BUTTON = "detach-field-button";

    public BasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));

        addAttachDetachControls();
    }

    private void addAttachDetachControls() {
        NativeButton attachButton = createButton(ATTACH_FIELD_BUTTON,
                "Attach field", event -> add(testField));
        NativeButton detachButton = createButton(DETACH_FIELD_BUTTON,
                "Detach field", event -> remove(testField));

        add(new Div(attachButton, detachButton));
    }

    @Override
    protected Select<String> createTestField() {
        Select<String> select = new Select<>();
        select.setItems(List.of("foo", "bar", "baz"));
        select.setEmptySelectionAllowed(true);

        return select;
    }
}
