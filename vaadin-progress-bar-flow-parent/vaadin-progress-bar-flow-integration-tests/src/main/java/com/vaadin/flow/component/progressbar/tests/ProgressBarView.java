/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.progressbar.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ProgressBar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-progress-bar")
public class ProgressBarView extends Div {

    public ProgressBarView() {
        createBasicProgressBar();
        createProgressBarWithCustomBounds();
        createIndeterminateProgressBar();
        createProgressBarWithThemeVariant();
    }

    private void createProgressBarWithThemeVariant() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setId("progress-bar-theme-variant");
        progressBar.setValue(0.345);
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);

        NativeButton removeVariantButton = new NativeButton(
                "Remove theme variant", e -> progressBar
                        .removeThemeVariants(ProgressBarVariant.LUMO_ERROR));
        removeVariantButton.setId("remove-theme-variant-button");

        addCard("Progress Bar Theme Variant", progressBar, removeVariantButton);
    }

    private void createBasicProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(0.345);

        progressBar.setId("default-progress-bar");
        addCard("Progress bar", progressBar);
    }

    private void createProgressBarWithCustomBounds() {
        ProgressBar progressBar = new ProgressBar(10, 100, 20);
        NativeButton progressButton = new NativeButton("Make progress", e -> {
            double value = progressBar.getValue() + 10;
            if (value > progressBar.getMax()) {
                value = progressBar.getMin();
            }
            progressBar.setValue(value);
        });

        progressBar.setId("custom-progress-bar");
        progressButton.setId("progress-button");
        addCard("Progress bar with custom bounds", progressBar, progressButton);
    }

    private void createIndeterminateProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        progressBar.setId("indeterminate-progress-bar");
        addCard("Indeterminate progress bar", progressBar);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
