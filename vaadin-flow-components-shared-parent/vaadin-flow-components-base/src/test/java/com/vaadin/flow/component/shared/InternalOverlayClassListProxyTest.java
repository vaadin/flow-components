package com.vaadin.flow.component.shared;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;

public class InternalOverlayClassListProxyTest {
    private TestComponent component;
    private InternalOverlayClassListProxy proxy;

    @Before
    public void setup() {
        component = new TestComponent();
        proxy = new InternalOverlayClassListProxy(component);
    }

    @Test
    public void add() {
        proxy.add("foo");
        proxy.add("bar");
        proxy.add("baz");

        verifyClassNames("foo bar baz");

        // No duplicates
        proxy.add("foo");
        proxy.add("bar");
        proxy.add("baz");

        verifyClassNames("foo bar baz");
    }

    @Test
    public void addAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        verifyClassNames("foo bar baz");

        // No duplicates
        proxy.addAll(List.of("foo", "bar", "baz"));

        verifyClassNames("foo bar baz");
    }

    @Test
    public void remove() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.remove("bar");
        verifyClassNames("foo baz");

        proxy.remove("foo");
        verifyClassNames("baz");

        proxy.remove("baz");
        verifyClassNames(null);
    }

    @Test
    public void removeAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.removeAll(List.of("foo", "baz"));
        verifyClassNames("bar");

        proxy.removeAll(List.of("bar"));
        verifyClassNames(null);
    }

    @Test
    public void removeIf() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.removeIf("foo"::equals);
        verifyClassNames("bar baz");

        proxy.removeIf("baz"::equals);
        verifyClassNames("bar");

        proxy.removeIf("bar"::equals);
        verifyClassNames(null);
    }

    @Test
    public void retainAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.retainAll(List.of("foo", "baz"));
        verifyClassNames("foo baz");

        proxy.retainAll(new ArrayList<String>());
        verifyClassNames(null);
    }

    @Test
    public void clear() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.clear();
        verifyClassNames(null);
    }

    @Test
    public void contains() {
        proxy.add("foo");

        Assert.assertTrue(proxy.contains("foo"));

        proxy.remove("foo");

        Assert.assertFalse(proxy.contains("foo"));
    }

    @Test
    public void containsAll() {
        proxy.addAll(List.of("foo", "bar"));

        Assert.assertTrue(proxy.containsAll(List.of("foo", "bar")));

        proxy.remove("foo");

        Assert.assertFalse(proxy.containsAll(List.of("foo", "bar")));
    }

    @Test
    public void isEmpty() {
        Assert.assertTrue(proxy.isEmpty());

        proxy.add("foo");

        Assert.assertFalse(proxy.isEmpty());
    }

    private void verifyClassNames(String expected) {
        Assert.assertEquals(expected,
                component.getElement().getProperty("overlayClass"));
        Assert.assertEquals(expected, component.getClassName());
    }

    @Tag("test-component")
    private static class TestComponent extends Component implements HasStyle {
    }
}
