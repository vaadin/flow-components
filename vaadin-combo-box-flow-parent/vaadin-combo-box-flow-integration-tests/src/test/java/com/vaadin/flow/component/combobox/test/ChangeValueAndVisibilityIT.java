
package com.vaadin.flow.component.combobox.test;

import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.ALTERNATIVE_VALUE;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.INITIAL_VALUE;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.NEW_VALUE;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.VALUE_CHANGES_ID;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.VALUE_ID;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.VISIBILITY_ID;
import static com.vaadin.flow.component.combobox.test.ChangeValueAndVisibilityPage.VISIBILITY_VALUE_ID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/change-value-and-visibility")
public class ChangeValueAndVisibilityIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void invisible_makeVisible_hasInitialValue() {
        clickButton(VISIBILITY_ID);
        assertValueChanges(new String[0]);
        assertValue(INITIAL_VALUE);
    }

    @Test
    public void invisible_makeVisibleSetNewValue_hasNewValue() {
        clickButton(VISIBILITY_VALUE_ID);
        assertValueChanges(NEW_VALUE);
        assertValue(NEW_VALUE);
    }

    @Test
    public void setAlternativeValue_makeVisibleSetNewValue_hasNewValue() {
        clickButton(VALUE_ID);
        assertValueChanges(ALTERNATIVE_VALUE);

        clickButton(VISIBILITY_VALUE_ID);
        assertValueChanges(ALTERNATIVE_VALUE, NEW_VALUE);
        assertValue(NEW_VALUE);
    }

    private void assertValue(String value) {
        Assert.assertEquals(value, combo.getSelectedText());
    }

    private void assertValueChanges(String... expected) {
        String[] valueChanges = $("div").id(VALUE_CHANGES_ID).$("p").all()
                .stream().map(TestBenchElement::getText).toArray(String[]::new);
        Assert.assertArrayEquals(expected, valueChanges);
    }
}
