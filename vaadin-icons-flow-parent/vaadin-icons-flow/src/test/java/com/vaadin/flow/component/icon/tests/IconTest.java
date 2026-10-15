/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasTooltip;

public class IconTest {

    @Test
    public void implementsHasTooltip() {
        Icon icon = new Icon();
        Assert.assertTrue(icon instanceof HasTooltip);
    }

    @Test
    public void emptyIconIsEmpty() {
        Assert.assertNull(new Icon().getIcon());
    }

    @Test
    public void usesVaadinCollectionByDefault() {
        Assert.assertEquals("vaadin:foo", new Icon("foo").getIcon());
    }

    @Test
    public void canDefineCollectionInConstructor() {
        Assert.assertEquals("bar:foo", new Icon("bar:foo").getIcon());
    }

    @Test
    public void canDefineCollectionInSetter() {
        Icon icon = new Icon();
        icon.setIcon("bar:foo");
        Assert.assertEquals("bar:foo", icon.getIcon());
    }

    @Test
    public void setterUsesCurrentCollection() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon("baz");
        Assert.assertEquals("bar:baz", icon.getIcon());
    }

    @Test
    public void canSetNewVaadinIcon() {
        Icon icon = new Icon("bar:foo");
        icon.setIcon(VaadinIcon.ABSOLUTE_POSITION);
        Assert.assertEquals("vaadin:absolute-position", icon.getIcon());
    }
}
