/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/focus-selected-item")
public class ComboBoxFocusSelectedItemIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;
    private ComboBoxElement small;
    private ComboBoxElement comboDefault;
    private ComboBoxElement comboNoProvider;
    private ComboBoxElement comboThrows;
    private ComboBoxElement comboPerson;
    private ComboBoxElement comboBound;

    @Before
    public void init() {
        open();
        waitUntil(driver -> !findElements(By.id("combo-bound")).isEmpty());
        combo = $(ComboBoxElement.class).id("combo");
        small = $(ComboBoxElement.class).id("small-combo");
        comboDefault = $(ComboBoxElement.class).id("combo-default");
        comboNoProvider = $(ComboBoxElement.class).id("combo-no-provider");
        comboThrows = $(ComboBoxElement.class).id("combo-throws");
        comboPerson = $(ComboBoxElement.class).id("combo-person");
        comboBound = $(ComboBoxElement.class).id("combo-bound");
    }

    @Test
    public void defaultOff_presetValue_open_doesNotAutoScroll() {
        comboDefault.openPopup();
        assertLoadingStateResolved(comboDefault);
        Assert.assertEquals(-1L, getFocusedIndex(comboDefault));
    }

    @Test
    public void presetValue_open_focusesSelectedItem() {
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        Assert.assertEquals("Item 5000", getItemLabelAtFocusedIndex(combo));
    }

    @Test
    public void noValue_open_doesNotFocus() {
        clickButton("clear");
        combo.openPopup();
        assertLoadingStateResolved(combo);
        Assert.assertEquals(-1L, getFocusedIndex(combo));
    }

    @Test
    public void defaultOff_escape_closesInOnePress() {
        // Regression guard for flow#5142: with focusSelectedItem=false the web
        // component does not auto-scroll, so _focusedIndex stays -1 and Escape
        // closes the dropdown in a single press.
        comboDefault.openPopup();
        assertLoadingStateResolved(comboDefault);
        executeScript(
                "arguments[0].inputElement.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true, cancelable: true }));",
                comboDefault);
        waitUntil(driver -> !comboDefault.getPropertyBoolean("opened"));
    }

    @Test
    public void escape_clearsFocusThenCloses() {
        // Documented behavior of the web component: when _focusedIndex is set,
        // the first Escape clears focus and reverts the input; the second
        // Escape closes the overlay.
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        executeScript(
                "arguments[0].inputElement.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true, cancelable: true }));",
                combo);
        waitUntil(driver -> getFocusedIndex(combo) == -1L);
        Assert.assertTrue("First Escape should not close the overlay",
                combo.getPropertyBoolean("opened"));
        executeScript(
                "arguments[0].inputElement.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true, cancelable: true }));",
                combo);
        waitUntil(driver -> !combo.getPropertyBoolean("opened"));
    }

    @Test
    public void toggleOff_reopen_doesNotAutoScroll() {
        clickButton("toggle");
        combo.openPopup();
        assertLoadingStateResolved(combo);
        Assert.assertEquals(-1L, getFocusedIndex(combo));
    }

    @Test
    public void reopen_loadingResolves_focusesSelectedItem() {
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);

        combo.closePopup();
        waitUntil(driver -> !combo.getPropertyBoolean("opened"));
        // Let the server-side close event fully round-trip before reopening.
        // Without the pause, the client reopens before the server's
        // close-side processing lands and the no-op range match doesn't
        // occur, so the bug we're guarding against doesn't reproduce.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        combo.openPopup();
        assertLoadingStateResolved(combo);
        waitForFocusedIndex(combo, 5000);
        Assert.assertEquals("Item 5000", getItemLabelAtFocusedIndex(combo));
    }

    @Test
    public void setValueWhileClosed_reopenWithFilter_doesNotFocus() {
        clickButton("set-shallow");
        combo.openPopup();
        waitForFocusedIndex(combo, 123);
        combo.closePopup();
        waitUntil(driver -> !combo.getPropertyBoolean("opened"));

        setFilterViaInput(combo, "3");
        waitUntil(driver -> "3".equals(combo.getPropertyString("filter")));
        assertLoadingStateResolved(combo);
        Assert.assertEquals(
                "Reopening with a filter should not auto-scroll to the selected item",
                -1L, getFocusedIndex(combo));
    }

    @Test
    public void filterWhileOpen_doesNotReFocusSelectedItem() {
        clickButton("set-shallow");
        combo.openPopup();
        waitForFocusedIndex(combo, 123);

        clearInputAndType(combo, "3");
        waitUntil(driver -> "3".equals(combo.getPropertyString("filter")));
        assertLoadingStateResolved(combo);
        Assert.assertNotEquals(
                "Typing a filter while open must not leave focus on the selected item",
                "Item 123", getItemLabelAtFocusedIndex(combo));
    }

    @Test
    public void filterExcludingSelectedItem_doesNotFocus() {
        clickButton("set-shallow");
        combo.openPopup();
        waitForFocusedIndex(combo, 123);

        clearInputAndType(combo, "zzz-no-match");
        assertLoadingStateResolved(combo);
        Assert.assertEquals(-1L, getFocusedIndex(combo));
    }

    @Test
    public void rapidTyping_doesNotReFocusSelectedItem() {
        clickButton("set-shallow");
        combo.openPopup();
        waitForFocusedIndex(combo, 123);

        // Three rapid synthetic input events without awaiting in between.
        executeScript("const cb = arguments[0];"
                + "const fire = (v) => { cb.inputElement.value = v;"
                + "  cb.inputElement.dispatchEvent(new InputEvent('input', {bubbles:true, data:v, inputType:'insertText'})); };"
                + "fire('1'); fire('2'); fire('3');", combo);
        waitUntil(driver -> "3".equals(combo.getPropertyString("filter")));
        assertLoadingStateResolved(combo);
        Assert.assertNotEquals(
                "Rapid filter typing must not leave focus on the selected item",
                "Item 123", getItemLabelAtFocusedIndex(combo));
    }

    @Test
    public void clientSideFilter_open_doesNotFocus() {
        // Small in-memory data sets activate the web component's
        // _clientSideFilter mode, where the server never sees the typed
        // filter. The Flow connector's resolveSelectedItemIndex returns
        // null in that mode (an authoritative index would not match the
        // client-filtered list), so the dropdown opens at the top with no
        // scroll — even when no filter has been typed yet.
        small.openPopup();
        assertLoadingStateResolved(small);
        Assert.assertEquals(-1L, getFocusedIndex(small));

        clearInputAndType(small, "app");
        waitUntil(driver -> "app".equals(small.getPropertyString("filter")));
        Assert.assertEquals(-1L, getFocusedIndex(small));
    }

    @Test
    public void typeFilterThenClear_doesNotReFocusSelectedItem() {
        // Open with empty filter focuses Item 5000. Typing a filter and
        // clearing it back to empty must NOT re-focus the selected item:
        // once the user has interacted with the filter, the dropdown stays
        // wherever the web component left it.
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);

        clearInputAndType(combo, "5000");
        waitUntil(driver -> "5000".equals(combo.getPropertyString("filter")));
        assertLoadingStateResolved(combo);

        // Clear the filter back to empty. The dropdown stays open.
        dispatchInput(combo, "", "deleteContentBackward");
        waitUntil(driver -> combo.getPropertyString("filter") == null
                || combo.getPropertyString("filter").isEmpty());
        assertLoadingStateResolved(combo);
        Assert.assertNotEquals(
                "Clearing the filter back to empty must not re-focus the selected item",
                5000L, getFocusedIndex(combo));
    }

    @Test
    public void binderUpdate_open_focusesNewValue() {
        clickButton("update-bean");
        comboBound.openPopup();
        waitForLabelAtFocusedIndex(comboBound, "Item 2500");
    }

    @Test
    public void customType_withIdentifierProvider_focusesSelectedItem() {
        comboPerson.openPopup();
        waitForFocusedIndex(comboPerson, 42);
        Assert.assertEquals("Person 42",
                getItemLabelAtFocusedIndex(comboPerson));
    }

    @Test
    public void serverChangedValue_reopen_focusesNewValue() {
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        clickButton("push-update");
        // A pure value change does not re-run focusSelectedItem on an
        // already-open dropdown. Close and reopen to pick up the new value.
        combo.closePopup();
        waitUntil(driver -> !combo.getPropertyBoolean("opened"));
        combo.openPopup();
        waitForFocusedIndex(combo, 7500);
    }

    @Test
    public void detachReattach_open_focusesSelectedItem() {
        clickButton("detach-reattach");
        waitUntil(driver -> findElements(By.id("combo")).size() == 1);
        combo = $(ComboBoxElement.class).id("combo");
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
    }

    @Test
    public void twoCombos_independentFocusResolution() {
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        combo.closePopup();
        waitUntil(driver -> !combo.getPropertyBoolean("opened"));

        comboPerson.openPopup();
        waitForFocusedIndex(comboPerson, 42);
    }

    @Test
    public void replaceItemsDroppingValue_open_doesNotCrash() {
        clickButton("replace-items");
        combo.openPopup();
        assertLoadingStateResolved(combo);
        long focused = getFocusedIndex(combo);
        Assert.assertTrue(
                "Replacing items should not leave a stray focus on an unrelated row. Got "
                        + focused,
                focused == -1L
                        || "Item 7500".equals(getItemLabelAtFocusedIndex(combo))
                        || "Item 5000"
                                .equals(getItemLabelAtFocusedIndex(combo)));
    }

    @Test
    public void throwingItemIndexProvider_open_doesNotCrashDropdown() {
        clickButton("toggle-throw");
        comboThrows.openPopup();
        assertLoadingStateResolved(comboThrows);
        // Either the web-component found Item 300 in the cache or
        // _focusedIndex stays -1; either way the dropdown must remain usable.
        long focused = getFocusedIndex(comboThrows);
        Assert.assertTrue(
                "A throwing ItemIndexProvider must not leave the combo broken; focused="
                        + focused,
                focused == -1L || focused == 300L);
    }

    @Test
    public void noItemIndexProvider_valueInLoadedPage_doesNotFocus() {
        // Without an ItemIndexProvider on the lazy data view, the server
        // returns null from resolveSelectedItemIndex and the connector has
        // no in-cache fallback. The dropdown opens at the top, even when
        // the selected item happens to be in the loaded first page.
        clickButton("set-no-provider-near");
        comboNoProvider.openPopup();
        assertLoadingStateResolved(comboNoProvider);
        Assert.assertEquals(-1L, getFocusedIndex(comboNoProvider));
    }

    @Test
    public void noItemIndexProvider_valueNotInLoadedPage_doesNotFocus() {
        clickButton("set-no-provider-far");
        comboNoProvider.openPopup();
        assertLoadingStateResolved(comboNoProvider);
        Assert.assertEquals(-1L, getFocusedIndex(comboNoProvider));
    }

    @Test
    public void arrowDown_afterAutoFocus_movesFocusDown() {
        // Verify the auto-focused item is a real navigation target — pressing
        // ArrowDown moves the focused index by one. The Enter-to-commit
        // assertion was dropped because WC keyboard commit semantics for
        // programmatically-set _focusedIndex are out of scope for this fix.
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        WebElement input = (WebElement) executeScript(
                "return arguments[0].inputElement", combo);
        input.sendKeys(Keys.ARROW_DOWN);
        waitUntil(driver -> getFocusedIndex(combo) == 5001L);
    }

    @Test
    public void ariaActiveDescendant_pointsToFocusedItem() {
        combo.openPopup();
        waitForFocusedIndex(combo, 5000);
        String activeDescendant = (String) executeScript(
                "return arguments[0].inputElement.getAttribute('aria-activedescendant')",
                combo);
        Assert.assertNotNull("aria-activedescendant should be set",
                activeDescendant);
        String focusedItemId = (String) executeScript(
                "const items = arguments[0]._scroller.querySelectorAll('vaadin-combo-box-item');"
                        + "return Array.from(items).find(el => el.index === arguments[0]._focusedIndex)?.id || null;",
                combo);
        Assert.assertEquals("aria-activedescendant must match focused item id",
                focusedItemId, activeDescendant);
    }

    private long getFocusedIndex(ComboBoxElement cb) {
        Object value = executeScript("return arguments[0]._focusedIndex", cb);
        return value == null ? -1L : ((Number) value).longValue();
    }

    private String getItemLabelAtFocusedIndex(ComboBoxElement cb) {
        Object value = executeScript(
                "const it = arguments[0]._dropdownItems?.[arguments[0]._focusedIndex];"
                        + "return it ? (it.label ?? String(it)) : null;",
                cb);
        return value == null ? null : String.valueOf(value);
    }

    private void waitForFocusedIndex(ComboBoxElement cb, long expected) {
        waitUntil(driver -> getFocusedIndex(cb) == expected);
    }

    private void waitForLabelAtFocusedIndex(ComboBoxElement cb, String label) {
        waitUntil(driver -> label.equals(getItemLabelAtFocusedIndex(cb)));
    }

    private void setFilterViaInput(ComboBoxElement cb, String text) {
        dispatchInput(cb, text, "insertText");
    }

    private void clearInputAndType(ComboBoxElement cb, String text) {
        dispatchInput(cb, "", "deleteContentBackward");
        dispatchInput(cb, text, "insertText");
    }

    private void dispatchInput(ComboBoxElement cb, String value,
            String inputType) {
        executeScript("const cb = arguments[0]; cb.inputElement.focus();"
                + "cb.inputElement.value = arguments[1];"
                + "cb.inputElement.dispatchEvent(new InputEvent('input', "
                + "{bubbles:true, data:arguments[1], inputType:arguments[2]}));",
                cb, value, inputType);
    }
}
