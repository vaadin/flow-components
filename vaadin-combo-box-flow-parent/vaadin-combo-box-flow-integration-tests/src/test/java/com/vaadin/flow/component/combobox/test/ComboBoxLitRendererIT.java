
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/lit-renderer")
public class ComboBoxLitRendererIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void shouldRenderFirstItem() {
        combo.openPopup();
        assertHasItem("Lit", "Item 0");
    }

    @Test
    public void shouldRenderLastItem() {
        int itemCount = getItems(combo).size();
        combo.openPopup();
        scrollToItem(combo, itemCount - 1);
        assertHasItem("Lit", "Item " + (itemCount - 1));
    }

    @Test
    public void shouldSwitchToComponentRenderer() {
        clickElementWithJs("componentRendererButton");
        combo.openPopup();
        assertHasItem("Component", "Item 0");
    }

    @Test
    public void shouldSwitchBackToLitRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("litRendererButton");
        combo.openPopup();
        assertHasItem("Lit", "Item 0");
    }

    private void assertHasItem(String type, String name) {
        Assert.assertTrue(getOverlayContents().stream().anyMatch(text -> {
            return text.contains(type) && text.contains(name);
        }));
    }
}
