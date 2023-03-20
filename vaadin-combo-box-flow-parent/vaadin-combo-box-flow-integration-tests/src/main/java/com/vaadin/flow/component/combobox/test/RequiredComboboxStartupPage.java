
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/required-combobox-startup")
public class RequiredComboboxStartupPage extends Div {

    public RequiredComboboxStartupPage() {
        Binder<TestBean> testBinder = new Binder<>();
        testBinder.setBean(new TestBean());
        ComboBox<Boolean> comboBox = new ComboBox<>();
        comboBox.setItems(true, false);
        Binder.Binding<TestBean, Boolean> comboBoxBinding = testBinder
                .forField(comboBox).withValidator((value, context) -> {
                    if (value) {
                        return ValidationResult.error("Must be false");
                    } else {
                        return ValidationResult.ok();
                    }
                }).asRequired().bind(TestBean::getDob, TestBean::setDob);
        add(comboBox);

        // validator should fail immediately, text field should be invalid
        comboBoxBinding.validate();
    }

    private class TestBean {

        private Boolean dob = true;

        public Boolean getDob() {
            return dob;
        }

        public void setDob(Boolean dob) {
            this.dob = dob;
        }

    }
}
