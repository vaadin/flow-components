package com.vaadin.flow.component.select.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.select.examples.TestView;
import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractSelectIT extends AbstractComponentIT {

    protected SelectElement selectElement;

    class Page {

        void clickSelectFirstItem() {
            getButtonWithText(TestView.SELECT_FIRST_ITEM).click();
        }

        void clickSelectThirdItem() {
            getButtonWithText(TestView.SELECT_THIRD_ITEM).click();
        }

        void clickSelectLastItem() {
            getButtonWithText(TestView.SELECT_LAST_ITEM).click();
        }

        private TestBenchElement getButtonWithText(String text) {
            return $("button").all().stream()
                    .filter(element -> element.getText().equalsIgnoreCase(text))
                    .findFirst().orElseThrow(() -> new NoSuchElementException(
                            "Cannot find button"));
        }

        private CheckboxElement getCheckboxWithText(String textContent) {
            return $(CheckboxElement.class).all().stream().filter(
                    element -> textContent.equalsIgnoreCase(element.getText()))
                    .findFirst().orElseThrow(() -> new NoSuchElementException(
                            "Cannot find checkbox with text " + textContent));
        }

        private void setInputValue(String id, String value) {
            selectElement.closePopup();
            TestBenchElement input = $(TestBenchElement.class).id(id);
            clear(input);
            input.sendKeys(value);
            input.callFunction("blur");
        }

        private void clear(TestBenchElement input) {
            String value = input.getAttribute("value");
            CharSequence[] clearSequence = new CharSequence[value.length()];
            for (int i = 0; i < clearSequence.length; i++) {
                clearSequence[i] = Keys.BACK_SPACE;
            }
            input.sendKeys(clearSequence);
        }

        public void clickResetNItems(int N) {
            getButtonWithText("Reset " + N + " items").click();
        }

        public void clickRefreshAll() {
            getButtonWithText("Refresh All").click();
        }

        public void clickRefreshItem(int N) {
            getButtonWithText("Refresh item " + N).click();
        }

        public void toggleEnabled(boolean enabled) {
            getCheckboxWithText("Enabled").setChecked(enabled);
        }

        public void toggleReadOnly(boolean readOnly) {
            getCheckboxWithText("ReadOnly").setChecked(readOnly);
        }

        public void toggleVisible(boolean visible) {
            getCheckboxWithText("Visible").setChecked(visible);
        }

        public void clickRendererButton() {
            getButtonWithText("Set renderer").click();
        }

        public void toggleItemLabelGenerator(boolean itemLabelGenrator) {
            getCheckboxWithText("ItemLabelGenerator")
                    .setChecked(itemLabelGenrator);
        }

        public void toggleItemEnabledProvider(boolean itemEnabledProvider) {
            getCheckboxWithText("ItemEnabledProvider")
                    .setChecked(itemEnabledProvider);
        }

        public void toggleEmptySelectionEnabled(boolean emptySeletionEnabled) {
            getCheckboxWithText("emptySelectionEnabled")
                    .setChecked(emptySeletionEnabled);
        }

        public void setEmptySelectionCaption(String emptySelectionCaption) {
            setInputValue("emptySelectionCaption", emptySelectionCaption);
        }

        public void setPlaceholder(String placeholder) {
            setInputValue("placeholder", placeholder);
        }
    }

    class Verify {

        void selectedItem(String expectedItemText) {
            selectedItem(expectedItemText, expectedItemText);
        }

        void selectedItem(String expectedItemText, String expectedItemLabel) {
            Assert.assertEquals("Invalid selected visual text",
                    expectedItemLabel, selectElement.getSelectedText());
            Assert.assertEquals("Invalid options selected from popup",
                    expectedItemText,
                    selectElement.getSelectedOptionItem().getText());
            selectElement.closePopup();
        }

        void noItemSelected() {
            try {
                SelectElement.ItemElement selectedItem = selectElement
                        .getSelectedItem();
                Assert.fail(
                        "Expected nothing to be selected, but selection was: "
                                + selectedItem.getText());
            } catch (NoSuchElementException nsee) {
                // expected
            }
        }

        void emptySelectionItemSelected() {
            SelectElement.ItemElement selectedItem = selectElement
                    .getSelectedItem();

            Assert.assertEquals(
                    "EmptySelectionItem not selected based on value attribute",
                    "", selectedItem.getAttribute("value"));
        }

        void valueChangeEvent(String value, String oldValue, boolean fromClient,
                int eventCounter) {
            Assert.assertEquals("Invalid value change event data",
                    TestView.createEventString(value, oldValue, fromClient,
                            eventCounter),
                    getDriver().findElement(By.id("VCE-" + eventCounter))
                            .getText());
            eventCounter++;
            Assert.assertEquals(
                    "An extra event (id:" + eventCounter + " has been fired", 0,
                    getDriver().findElements(By.id("VCE-" + eventCounter))
                            .size());
        }

        public void selectDisabled() {
            Assert.assertTrue("disabled attribute missing",
                    selectElement.hasAttribute("disabled"));
            Assert.assertTrue("disabled property wrong value",
                    selectElement.getPropertyBoolean("disabled"));

            selectElement.openPopup();

            Assert.assertFalse("Popup should not be opened",
                    selectElement.isOpened());
        }

        public void selectReadOnly() {
            Assert.assertTrue("readonly attribute missing",
                    selectElement.hasAttribute("readonly"));
            Assert.assertTrue("readonly property wrong value",
                    selectElement.getPropertyBoolean("readonly"));

            selectElement.openPopup();

            Assert.assertFalse("Popup should not be opened",
                    selectElement.isOpened());
        }

        public void userSelectionDoesntFireEvent(int itemIndexToTestWith) {
            IntSupplier numberOfValueChanges = () -> findElement(
                    By.id("value-change-container"))
                            .findElements(By.tagName("div")).size();
            int currentValues = numberOfValueChanges.getAsInt();

            selectElement.selectItemByIndex(itemIndexToTestWith);

            Assert.assertEquals(
                    "No new value change event should have been fired",
                    currentValues, numberOfValueChanges.getAsInt());
        }

        public void itemEnabled(
                SerializablePredicate<Integer> itemEnabledProvider) {
            List<SelectElement.ItemElement> items = selectElement.getItems();
            Assert.assertEquals("Invalid amout of items",
                    getInitialNumberOfItems(), items.size());

            for (int i = 0; i < items.size(); i++) {
                SelectElement.ItemElement itemElement = items.get(i);
                Boolean disabled = !itemEnabledProvider.test(i);
                Assert.assertEquals(
                        "Wrong disabled attribute state for item " + i,
                        disabled, itemElement.getPropertyBoolean("disabled"));
            }
        }

        public void emptySelectionItemInDropDown(
                String emptySelectionItemCaption) {
            SelectElement.ItemElement itemElement = selectElement.getItems()
                    .get(0);
            Assert.assertEquals("invalid key", "",
                    itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", emptySelectionItemCaption,
                    itemElement.getText());
        }
    }

    protected final Page page = new Page();
    protected final Verify verify = new Verify();

    protected void openWithExtraParameter(String parameter) {
        open(getDefaultParameter(), parameter);
    }

    @Override
    protected String getTestURL(String... parameters) {
        // replacing the query parameter as normal flow route parameter
        return super.getTestURL(parameters).replace("?", "");
    }

    protected String getDefaultParameter() {
        int initialNumberOfItems = getInitialNumberOfItems();
        if (initialNumberOfItems < 1) {
            return null;
        } else {
            return "items=" + initialNumberOfItems;
        }
    }

    @Override
    protected void open(String... parameters) {
        super.open(parameters);
        selectElement = $(SelectElement.class).waitForFirst();
    }

    @Before
    public void init() {
        open(getDefaultParameter());
    }

    protected abstract int getInitialNumberOfItems();

}
