package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class AppLayoutTest {

    private AppLayout systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new AppLayout();
    }

    @Test
    public void setBranding_Element() {
        Element branding = new H2("Vaadin").getElement();
        systemUnderTest.setBranding(branding);

        // Verify that branding goes to the branding slot.
        long brandingCount = systemUnderTest.getElement().getChildren()
            .filter(e -> e.getAttribute("slot").equals("branding")).count();
        Assert.assertEquals(1, brandingCount);
    }

    @Test
    public void setBranding_Component() {
        Component branding = new Div();
        Assert.assertNull(branding.getElement().getAttribute("slot"));
        systemUnderTest.setBranding(branding);
        Assert.assertEquals("branding",
            branding.getElement().getAttribute("slot"));
        Assert.assertTrue(
            systemUnderTest.getChildren().anyMatch(branding::equals));
    }

    @Test
    public void removeBranding() {
        Element branding = new H2("Vaadin").getElement();
        systemUnderTest.setBranding(branding);

        systemUnderTest.removeBranding();

        Assert.assertTrue(systemUnderTest.getElement().getChildren()
            .noneMatch(e -> e.getAttribute("slot").equals("branding")));
    }

    @Test
    public void setContent() {
        Element content = new Div().getElement();
        systemUnderTest.setContent(content);

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        Assert.assertTrue(children.contains(content));
    }

    @Test
    public void removeContent() {
        systemUnderTest.removeContent(); // No NPE.

        Element content = new Div().getElement();
        systemUnderTest.setContent(content);

        systemUnderTest.removeContent();

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        Assert.assertFalse(children.contains(content));
        Assert.assertNull(systemUnderTest.getContent());
    }

}
