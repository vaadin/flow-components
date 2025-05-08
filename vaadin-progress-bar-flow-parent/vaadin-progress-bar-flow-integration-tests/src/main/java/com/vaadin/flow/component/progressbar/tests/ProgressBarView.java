/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.progressbar.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ProgressBar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-progress-bar")
public class ProgressBarView extends Div {

    public ProgressBarView() {
        ProgressBar progressBar = new ProgressBar(10, 100, 20);
        NativeButton progressButton = new NativeButton("Make progress", e -> {
            double value = progressBar.getValue() + 10;
            if (value > progressBar.getMax()) {
                value = progressBar.getMin();
            }
            progressBar.setValue(value);
        });

        progressBar.setId("progress-bar");
        progressButton.setId("progress-button");

        add(progressBar, progressButton);
    }
}
