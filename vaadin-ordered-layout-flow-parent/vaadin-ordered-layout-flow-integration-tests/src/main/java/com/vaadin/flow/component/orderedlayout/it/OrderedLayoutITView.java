package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.demo.AbstractLayout;
import com.vaadin.flow.router.Route;

@Route("ordered-layout-tests")
public class OrderedLayoutITView extends Div {

    public OrderedLayoutITView() {
        VerticalLayout verticalLayoutWithSpacing = new VerticalLayout();
        verticalLayoutWithSpacing.setId("vl-spacing");
        verticalLayoutWithSpacing.add(AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeCheckbox("spacing",
                        verticalLayoutWithSpacing::setSpacing,true),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-xl"));

        HorizontalLayout horizontalLayoutWithSpacing = new HorizontalLayout();
        horizontalLayoutWithSpacing.setId("hl-spacing");
        horizontalLayoutWithSpacing.add(
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeCheckbox("spacing",
                        horizontalLayoutWithSpacing::setSpacing,true),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-xl"));

        add(verticalLayoutWithSpacing, horizontalLayoutWithSpacing);
    }
}
