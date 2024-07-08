/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.progressbar.demo;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.progressbar.GeneratedVaadinProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ProgressBar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-progress-bar")
public class ProgressBarView extends DemoView {

    @Override
    public void initView() {
        createBasicProgressBar();
        createProgressBarWithCustomBounds();
        createIndeterminateProgressBar();
        createProgressBarWithThemeVariant();
    }

    private void createProgressBarWithThemeVariant() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(0.345);
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);

        add(progressBar);
        // end-source-example

        addVariantsDemo(() -> {
            return progressBar;
        }, GeneratedVaadinProgressBar::addThemeVariants,
                GeneratedVaadinProgressBar::removeThemeVariants,
                ProgressBarVariant::getVariantName,
                ProgressBarVariant.LUMO_ERROR);
    }

    private void createBasicProgressBar() {
        // begin-source-example
        // source-example-heading: Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(0.345);

        add(progressBar);
        // end-source-example

        progressBar.setId("default-progress-bar");
        addCard("Progress bar", progressBar);
    }

    private void createProgressBarWithCustomBounds() {
        // begin-source-example
        // source-example-heading: Progress bar with custom bounds
        ProgressBar progressBar = new ProgressBar(10, 100, 20);
        NativeButton progressButton = new NativeButton("Make progress", e -> {
            double value = progressBar.getValue() + 10;
            if (value > progressBar.getMax()) {
                value = progressBar.getMin();
            }
            progressBar.setValue(value);
        });

        add(progressBar, progressButton);
        // end-source-example

        progressBar.setId("custom-progress-bar");
        progressButton.setId("progress-button");
        addCard("Progress bar with custom bounds", progressBar, progressButton);
    }

    private void createIndeterminateProgressBar() {
        // begin-source-example
        // source-example-heading: Indeterminate progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        add(progressBar);
        // end-source-example

        progressBar.setId("indeterminate-progress-bar");
        addCard("Indeterminate progress bar", progressBar);
    }
}
