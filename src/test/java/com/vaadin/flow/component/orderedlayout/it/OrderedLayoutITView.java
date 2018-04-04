package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.demo.AbstractLayout;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("ordered-layout-tests")
public class OrderedLayoutITView extends DemoView {

    @Override
    protected void initView() {
        createSpacingToggles();
    }

    private void createSpacingToggles() {
        VerticalLayout verticalLayoutWithSpacing = new VerticalLayout();
        verticalLayoutWithSpacing.setId("vl-spacing");
        verticalLayoutWithSpacing.add(
                AbstractLayout.createToggleThemeButton(
                        verticalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeButton(
                        verticalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeButton(
                        verticalLayoutWithSpacing, "spacing",
                        verticalLayoutWithSpacing::setSpacing),
                AbstractLayout.createToggleThemeButton(
                        verticalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeButton(
                        verticalLayoutWithSpacing, "spacing-xl"));

        HorizontalLayout horizontalLayoutWithSpacing = new HorizontalLayout();
        horizontalLayoutWithSpacing.setId("hl-spacing");
        horizontalLayoutWithSpacing.add(
                AbstractLayout.createToggleThemeButton(
                        horizontalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeButton(
                        horizontalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeButton(
                        horizontalLayoutWithSpacing, "spacing",
                        horizontalLayoutWithSpacing::setSpacing),
                AbstractLayout.createToggleThemeButton(
                        horizontalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeButton(
                        horizontalLayoutWithSpacing, "spacing-xl"));

        add(verticalLayoutWithSpacing, horizontalLayoutWithSpacing);
    }
}
