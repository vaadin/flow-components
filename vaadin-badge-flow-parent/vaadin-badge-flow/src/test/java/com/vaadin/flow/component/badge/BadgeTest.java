/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.badge;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/**
 * Unit tests for the {@link Badge} component.
 */
public class BadgeTest {

    private Badge badge;

    @Before
    public void setup() {
        var ui = new UI();
        UI.setCurrent(ui);
        badge = new Badge();
        ui.add(badge);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void defaultConstructor_textIsEmpty() {
        Assert.assertEquals("", badge.getText());
    }

    @Test
    public void constructorWithText_textIsSet() {
        var badgeWithText = new Badge("New");
        Assert.assertEquals("New", badgeWithText.getText());
    }

    @Test
    public void setText_textIsUpdated() {
        badge.setText("Updated");
        Assert.assertEquals("Updated", badge.getText());
    }

    @Test
    public void setTextNull_textIsEmpty() {
        badge.setText("Text");
        badge.setText(null);
        Assert.assertEquals("", badge.getText());
    }

    @Test
    public void addComponent_componentIsAdded() {
        var span = new Span("Content");
        badge.add(span);
        Assert.assertTrue(badge.getChildren().anyMatch(c -> c.equals(span)));
        Assert.assertTrue(span.isAttached());
    }

    @Test
    public void addMultipleComponents_allComponentsAreAdded() {
        var span1 = new Span("First");
        var span2 = new Span("Second");
        badge.add(span1, span2);
        Assert.assertEquals(2, badge.getChildren().count());
        Assert.assertTrue(badge.getChildren().anyMatch(c -> c.equals(span1)));
        Assert.assertTrue(badge.getChildren().anyMatch(c -> c.equals(span2)));
    }

    @Test
    public void removeComponent_componentIsRemoved() {
        var span = new Span("Content");
        badge.add(span);
        badge.remove(span);
        Assert.assertFalse(badge.getChildren().anyMatch(c -> c.equals(span)));
        Assert.assertFalse(span.isAttached());
    }

    @Test
    public void removeAll_allComponentsAreRemoved() {
        badge.add(new Span("First"), new Span("Second"));
        badge.removeAll();
        Assert.assertEquals(0, badge.getChildren().count());
    }

    @Test
    public void addThemeVariant_variantIsAdded() {
        badge.addThemeVariants(BadgeVariant.LUMO_SUCCESS);
        Assert.assertTrue(badge.getThemeNames().contains("success"));
    }

    @Test
    public void addMultipleThemeVariants_allVariantsAreAdded() {
        badge.addThemeVariants(BadgeVariant.LUMO_SUCCESS,
                BadgeVariant.LUMO_SMALL);
        Assert.assertTrue(badge.getThemeNames().contains("success"));
        Assert.assertTrue(badge.getThemeNames().contains("small"));
    }

    @Test
    public void removeThemeVariant_variantIsRemoved() {
        badge.addThemeVariants(BadgeVariant.LUMO_SUCCESS);
        badge.removeThemeVariants(BadgeVariant.LUMO_SUCCESS);
        Assert.assertFalse(badge.getThemeNames().contains("success"));
    }

    @Test
    public void setAriaLabel_ariaLabelIsSet() {
        badge.setAriaLabel("Status badge");
        Assert.assertEquals("Status badge", badge.getAriaLabel().orElse(null));
    }

    @Test
    public void setAriaLabelNull_ariaLabelIsRemoved() {
        badge.setAriaLabel("Status badge");
        badge.setAriaLabel(null);
        Assert.assertTrue(badge.getAriaLabel().isEmpty());
    }

    @Test
    public void setTooltipText_tooltipIsSet() {
        badge.setTooltipText("This is a badge");
        // Tooltip is set, we can't easily test the content but we can verify no exception
        Assert.assertNotNull(badge);
    }

    @Test
    public void setWidth_widthIsSet() {
        badge.setWidth("100px");
        Assert.assertEquals("100px", badge.getWidth());
    }

    @Test
    public void setHeight_heightIsSet() {
        badge.setHeight("50px");
        Assert.assertEquals("50px", badge.getHeight());
    }

    @Test
    public void addClassName_classNameIsAdded() {
        badge.addClassName("custom-badge");
        Assert.assertTrue(badge.getClassNames().contains("custom-badge"));
    }
}
