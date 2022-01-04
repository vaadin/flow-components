/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-button/detach-reattach-disable-on-click-button")
public class DetachReattachDisableOnClickButtonPage extends Div {

    public DetachReattachDisableOnClickButtonPage() {
        Button disableOnClickButton = new Button("Disable on click");
        disableOnClickButton.setId("disable-on-click");
        disableOnClickButton.setDisableOnClick(true);
        disableOnClickButton.addClickListener(event -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                event.getSource().setEnabled(true);
            }
        });

        Button removeFromViewButton = new Button("Remove from view",
                event -> remove(disableOnClickButton));
        removeFromViewButton.setId("remove-from-view");
        Button addToViewButton = new Button("Add to view",
                event -> add(disableOnClickButton));
        addToViewButton.setId("add-to-view");

        add(removeFromViewButton, addToViewButton, disableOnClickButton);
    }
}
