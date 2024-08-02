/*
 * Copyright 2000-2024 Vaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.popover;

import elemental.json.JsonArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Text;

/**
 * @author Vaadin Ltd.
 */
public class PopoverTest {
    private Popover popover;

    @Before
    public void setup() {
        popover = new Popover();
    }

    @Test
    public void setFor_getFor() {
        popover.setFor("target-id");
        Assert.assertEquals(popover.getFor(), "target-id");
    }

    @Test
    public void setTarget_getTarget() {
        Div target = new Div();
        popover.setTarget(target);
        Assert.assertEquals(popover.getTarget(), target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTarget_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        popover.setTarget(textNode);
    }

    @Test
    public void setPosition_getPosition() {
        popover.setPosition(PopoverPosition.END);
        Assert.assertEquals("end",
                popover.getElement().getProperty("position"));
        Assert.assertEquals(PopoverPosition.END, popover.getPosition());
    }

    @Test
    public void defaultPosition_equalsNull() {
        Assert.assertEquals(null, popover.getPosition());
    }

    @Test
    public void setWidth_contentWidthPropertyUpdated() {
        popover.setWidth("200px");
        Assert.assertEquals("200px",
                popover.getElement().getProperty("contentWidth", ""));

        popover.setWidth(null);
        Assert.assertEquals("",
                popover.getElement().getProperty("contentWidth", ""));
    }

    @Test
    public void setHeight_contentHeightPropertyUpdated() {
        popover.setHeight("200px");
        Assert.assertEquals("200px",
                popover.getElement().getProperty("contentHeight", ""));

        popover.setHeight(null);
        Assert.assertEquals("",
                popover.getElement().getProperty("contentHeight", ""));
    }

    @Test
    public void setFocusDelay_getFocusDelay() {
        Assert.assertEquals(0, popover.getFocusDelay());

        popover.setFocusDelay(1000);
        Assert.assertEquals(1000, popover.getFocusDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("focusDelay", 0));
    }

    @Test
    public void setHoverDelay_getHoverDelay() {
        Assert.assertEquals(0, popover.getHoverDelay());

        popover.setHoverDelay(1000);
        Assert.assertEquals(1000, popover.getHoverDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("hoverDelay", 0));
    }

    @Test
    public void setHideDelay_getHideDelay() {
        Assert.assertEquals(0, popover.getHideDelay());

        popover.setHideDelay(1000);
        Assert.assertEquals(1000, popover.getHideDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("hideDelay", 0));
    }

    @Test
    public void isOpenOnClick_trueByDefault() {
        Assert.assertTrue(popover.isOpenOnClick());
    }

    @Test
    public void isOpenOnFocus_falseByDefault() {
        Assert.assertFalse(popover.isOpenOnFocus());
    }

    @Test
    public void isOpenOnHover_falseByDefault() {
        Assert.assertFalse(popover.isOpenOnHover());
    }

    @Test
    public void setOpenOnClick_isOpenOnClick() {
        popover.setOpenOnClick(false);
        Assert.assertFalse(popover.isOpenOnClick());

        popover.setOpenOnClick(true);
        Assert.assertTrue(popover.isOpenOnClick());
    }

    @Test
    public void setOpenOnFocus_isOpenOnFocus() {
        popover.setOpenOnFocus(true);
        Assert.assertTrue(popover.isOpenOnFocus());

        popover.setOpenOnFocus(false);
        Assert.assertFalse(popover.isOpenOnFocus());
    }

    @Test
    public void setOpenOnHover_isOpenOnHover() {
        popover.setOpenOnHover(true);
        Assert.assertTrue(popover.isOpenOnHover());

        popover.setOpenOnHover(false);
        Assert.assertFalse(popover.isOpenOnHover());
    }

    @Test
    public void getTriggerProperty_defaultValue_click() {
        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(1, jsonArray.length());
        Assert.assertEquals("click", jsonArray.get(0).asString());
    }

    @Test
    public void setOpenOnClick_triggerPropertyUpdated() {
        popover.setOpenOnClick(false);

        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void setOpenOnFocus_triggerPropertyUpdated() {
        popover.setOpenOnFocus(true);

        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(2, jsonArray.length());
        Assert.assertEquals("click", jsonArray.get(0).asString());
        Assert.assertEquals("focus", jsonArray.get(1).asString());
    }

    @Test
    public void setOpenOnHover_triggerPropertyUpdated() {
        popover.setOpenOnHover(true);

        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(2, jsonArray.length());
        Assert.assertEquals("click", jsonArray.get(0).asString());
        Assert.assertEquals("hover", jsonArray.get(1).asString());
    }

    @Test
    public void setAriaLabel_getAriaLabel() {
        popover.setAriaLabel("aria-label");
        Assert.assertTrue(popover.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", popover.getAriaLabel().get());

        popover.setAriaLabel(null);
        Assert.assertTrue(popover.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy_getAriaLabelledBy() {
        popover.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(popover.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby",
                popover.getAriaLabelledBy().get());

        popover.setAriaLabelledBy(null);
        Assert.assertTrue(popover.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void getOverlayRole_defaultDialog() {
        Assert.assertEquals("dialog", popover.getOverlayRole());
        Assert.assertEquals("dialog",
                popover.getElement().getProperty("overlayRole"));
    }

    @Test
    public void setOverlayRole_getOverlayRole() {
        popover.setOverlayRole("alertdialog");

        Assert.assertEquals("alertdialog", popover.getOverlayRole());
        Assert.assertEquals("alertdialog",
                popover.getElement().getProperty("overlayRole"));
    }

    @Test(expected = NullPointerException.class)
    public void setOverlayRole_null_throws() {
        popover.setOverlayRole(null);
    }

    @Test
    public void setModal_isModal() {
        Assert.assertFalse(popover.isModal());
        Assert.assertFalse(popover.getElement().getProperty("modal", false));

        popover.setModal(true);
        Assert.assertTrue(popover.isModal());
        Assert.assertTrue(popover.getElement().getProperty("modal", false));
    }

    @Test
    public void setBackdropVisible_isBackdropVisible() {
        Assert.assertFalse(popover.isBackdropVisible());
        Assert.assertFalse(
                popover.getElement().getProperty("withBackdrop", false));

        popover.setBackdropVisible(true);
        Assert.assertTrue(popover.isBackdropVisible());
        Assert.assertTrue(
                popover.getElement().getProperty("withBackdrop", false));
    }

    @Test
    public void setAutofocus_isAutofocus() {
        Assert.assertFalse(popover.isAutofocus());
        Assert.assertFalse(
                popover.getElement().getProperty("autofocus", false));

        popover.setAutofocus(true);
        Assert.assertTrue(popover.isAutofocus());
        Assert.assertTrue(popover.getElement().getProperty("autofocus", false));
    }
}
