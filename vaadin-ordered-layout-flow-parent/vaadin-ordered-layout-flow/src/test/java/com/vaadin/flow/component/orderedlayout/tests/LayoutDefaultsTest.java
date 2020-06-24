package com.vaadin.flow.component.orderedlayout.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class LayoutDefaultsTest {

    @Test
    public void testHorizontalLayout_byDefault_spacingIsOn() {
        Assert.assertTrue("Spacing should be on by default",
                new HorizontalLayout().isSpacing());
        Assert.assertFalse("Padding shouldn't be on by default",
                new HorizontalLayout().isPadding());
        Assert.assertFalse("Margin shouldn't be on by default",
                new HorizontalLayout().isMargin());
    }

    @Test
    public void testVerticalLayout_byDefault_spacingAndPaddingIsOn() {
        Assert.assertTrue("Padding should be on by default",
                new VerticalLayout().isPadding());
        Assert.assertTrue("Spacing should be on by default",
                new VerticalLayout().isSpacing());
        Assert.assertFalse("Margin shouldn't be on by default",
                new VerticalLayout().isMargin());
    }

    @Test
    public void create_Layout() {
        // Just testing that creating layout actually compiles and doesn't
        // throw. Test is on purpose, so that the implementation not
        // accidentally removed.
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClickListener(event -> {
        });

        FlexLayout flexLayout = new FlexLayout();
        flexLayout.addClickListener(event -> {
        });

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClickListener(event -> {
        });
    }

    @Test
    public void defaultAlignmentValues() {
        VerticalLayout verticalLayout = new VerticalLayout();
        Assert.assertEquals(Alignment.STRETCH,
                verticalLayout.getDefaultHorizontalComponentAlignment());

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Assert.assertEquals(Alignment.STRETCH,
                horizontalLayout.getDefaultVerticalComponentAlignment());

    }

    @Test
    public void expandable_Layout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addAndExpand(new Label("Foo"), new Label("bar"));
        testExpandableComponent(horizontalLayout.getWidth(),
                horizontalLayout.getChildren());

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addAndExpand(new Label("Foo"), new Label("bar"));
        testExpandableComponent(verticalLayout.getHeight(),
                verticalLayout.getChildren());
    }

    private void testExpandableComponent(String size,
            Stream<Component> components) {
        Assert.assertEquals(size, "100%");

        components.forEach(component -> Assert.assertEquals(
                component.getElement().getStyle().get("flex-grow"), "1.0"));
    }
}
