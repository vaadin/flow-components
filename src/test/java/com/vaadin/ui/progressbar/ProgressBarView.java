/*
 * Copyright 2000-2017 Vaadin Ltd.
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
 *
 */

package com.vaadin.ui.progressbar;

import com.vaadin.flow.demo.DemoView;
import com.vaadin.router.Route;
import com.vaadin.ui.button.Button;
import com.vaadin.ui.common.HtmlImport;

/**
 * View for {@link ProgressBar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-progress-bar")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-progress-bar.html")
public class ProgressBarView extends DemoView {

    @Override
    public void initView() {
        createBasicProgressBar();
        createProgressBarWithCustomBounds();
        createIndeterminateProgressBar();
    }

    private void createBasicProgressBar() {
        // begin-source-example
        // source-example-heading: Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(0.345);
        // end-source-example

        progressBar.setId("default-progress-bar");
        addCard("Progress bar", progressBar);
    }

    private void createProgressBarWithCustomBounds() {
        // begin-source-example
        // source-example-heading: Progress bar with custom bounds
        ProgressBar progressBar = new ProgressBar(10, 100, 20);
        Button progressButton = new Button("Make progress", e -> {
            double value = progressBar.getValue() + 10;
            if (value > progressBar.getMax()) {
                value = progressBar.getMin();
            }
            progressBar.setValue(value);
        });
        // end-source-example

        progressBar.setId("custom-progress-bar");
        progressButton.setId("progress-button");
        addCard("Progress bar with custom bounds", progressBar,
                progressButton);
    }

    private void createIndeterminateProgressBar() {
        // begin-source-example
        // source-example-heading: Indeterminate progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        // end-source-example

        progressBar.setId("indeterminate-progress-bar");
        addCard("Indeterminate progress bar", progressBar);
    }
}
