/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for ProgressBar component.
 */
@Route(value = "progress-bar", layout = MainLayout.class)
@PageTitle("Progress Bar | Vaadin Kitchen Sink")
public class ProgressBarDemoView extends VerticalLayout {

    public ProgressBarDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Progress Bar Component"));
        add(new Paragraph("ProgressBar displays the progress of a task."));

        // Basic progress bar
        ProgressBar basic = new ProgressBar();
        basic.setValue(0.5);
        basic.setWidthFull();
        addSection("Basic Progress Bar (50%)", basic);

        // Various progress levels
        ProgressBar empty = new ProgressBar();
        empty.setValue(0);
        empty.setWidthFull();
        addSection("Empty (0%)", empty);

        ProgressBar quarter = new ProgressBar();
        quarter.setValue(0.25);
        quarter.setWidthFull();
        addSection("25% Complete", quarter);

        ProgressBar half = new ProgressBar();
        half.setValue(0.5);
        half.setWidthFull();
        addSection("50% Complete", half);

        ProgressBar threeQuarter = new ProgressBar();
        threeQuarter.setValue(0.75);
        threeQuarter.setWidthFull();
        addSection("75% Complete", threeQuarter);

        ProgressBar complete = new ProgressBar();
        complete.setValue(1.0);
        complete.setWidthFull();
        addSection("Complete (100%)", complete);

        // Indeterminate progress bar
        ProgressBar indeterminate = new ProgressBar();
        indeterminate.setIndeterminate(true);
        indeterminate.setWidthFull();
        addSection("Indeterminate (Loading)", indeterminate);

        // With custom range
        ProgressBar customRange = new ProgressBar(0, 10, 7);
        customRange.setWidthFull();
        addSection("Custom Range (7 of 10)", customRange);

        // Theme variants - Success
        ProgressBar success = new ProgressBar();
        success.setValue(0.8);
        success.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        success.setWidthFull();
        addSection("Success Variant", success);

        // Theme variants - Error
        ProgressBar error = new ProgressBar();
        error.setValue(0.3);
        error.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        error.setWidthFull();
        addSection("Error Variant", error);

        // Theme variants - Contrast
        ProgressBar contrast = new ProgressBar();
        contrast.setValue(0.6);
        contrast.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
        contrast.setWidthFull();
        addSection("Contrast Variant", contrast);

        // Combination examples
        Div downloadExample = new Div();
        downloadExample.add(new Paragraph("Downloading file.zip..."));
        ProgressBar downloadProgress = new ProgressBar();
        downloadProgress.setValue(0.65);
        downloadProgress.setWidthFull();
        downloadExample.add(downloadProgress);
        downloadExample.add(new Paragraph("65% complete - 3.2 MB of 5 MB"));
        addSection("Download Progress Example", downloadExample);

        Div uploadExample = new Div();
        uploadExample.add(new Paragraph("Uploading images..."));
        ProgressBar uploadProgress = new ProgressBar();
        uploadProgress.setValue(0.4);
        uploadProgress.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        uploadProgress.setWidthFull();
        uploadExample.add(uploadProgress);
        uploadExample.add(new Paragraph("2 of 5 files uploaded"));
        addSection("Upload Progress Example", uploadExample);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
