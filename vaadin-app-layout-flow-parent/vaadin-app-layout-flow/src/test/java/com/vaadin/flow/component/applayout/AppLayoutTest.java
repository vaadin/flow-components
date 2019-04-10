package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AppLayoutTest {

    private AppLayout systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new AppLayout();
    }

    @Test
    public void setContent() {
        Div content = new Div();
        systemUnderTest.setContent(content);

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        assertTrue(children.contains(content.getElement()));
    }

    @Test
    public void setContentNull() {
        systemUnderTest.setContent(null); // No NPE.

        Div content = new Div();
        systemUnderTest.setContent(content);

        systemUnderTest.setContent(null);

        List<Element> children = systemUnderTest.getElement().getChildren()
            .collect(Collectors.toList());
        assertFalse(children.contains(content.getElement()));
        assertNull(systemUnderTest.getContent());
    }

    @Test
    public void addToDrawer() {
        final Component component = new Div();
        systemUnderTest.addToDrawer(component);
        assertEquals("drawer", component.getElement().getAttribute("slot"));
        assertEquals(systemUnderTest, getParent(component));
    }

    @Test
    public void addToNavbar() {
        final Component component = new Div();
        systemUnderTest.addToNavbar(component);
        assertEquals("navbar", component.getElement().getAttribute("slot"));
        assertEquals(systemUnderTest, getParent(component));
    }

    @Test
    public void removeContent() {
        testRemoval(systemUnderTest::setContent);
        assertNull(systemUnderTest.getContent());
    }

    @Test
    public void removeDrawer() {
        testRemoval(systemUnderTest::addToDrawer);
    }

    @Test
    public void removeNavbar() {
        testRemoval(systemUnderTest::addToNavbar);
    }

    private void testRemoval(Consumer<Component> adder) {
        final Component component = new Div();
        assertNull(getParent(component));

        adder.accept(component);
        assertEquals(systemUnderTest, getParent(component));

        systemUnderTest.remove(component);
        assertNull(getParent(component));
    }

    @Test
    public void testShowRouterLayoutContentWithNullValue() {
        testShowRouterLayoutContent(null);
    }

    @Test
    public void testShowRouterLayoutContentWithValidValue() {
        testShowRouterLayoutContent(new Div());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShowRouterLayoutContentThrowsExceptionForNonComponent() {
        final Element element = new Element("div");
        systemUnderTest.showRouterLayoutContent(() -> element);
    }

    private void testShowRouterLayoutContent(HasElement content) {
        // Works initially
        systemUnderTest.showRouterLayoutContent(content);
        assertEquals(content, systemUnderTest.getContent());

        final Component component = new Div();
        systemUnderTest.showRouterLayoutContent(component);
        assertEquals(component, systemUnderTest.getContent());

        // Works after setting other value
        systemUnderTest.showRouterLayoutContent(content);
        assertEquals(content, systemUnderTest.getContent());
    }

    private static Component getParent(Component component) {
        return component.getParent().orElse(null);
    }
}
