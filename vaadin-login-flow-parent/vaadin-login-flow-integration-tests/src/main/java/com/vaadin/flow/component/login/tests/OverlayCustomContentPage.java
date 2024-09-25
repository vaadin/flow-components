/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/custom-content")
public class OverlayCustomContentPage extends Div {
    public static String CUSTOM_FORM_CONTENT = "__CUSTOM_FORM_CONTENT__";
    public static String FOOTER_CONTENT = "__FOOTER_CONTENT__";

    public OverlayCustomContentPage() {
        LoginOverlay login = new LoginOverlay();

        NativeButton open = new NativeButton("Open");
        open.setId("open-overlay-btn");
        open.addClickListener(e -> login.setOpened(true));

        Span footerContent = new Span(FOOTER_CONTENT);

        NativeButton addFooter = new NativeButton("Add footer");
        addFooter.setId("add-footer-btn");
        addFooter.addClickListener(e -> login.getFooter().add(footerContent));

        NativeButton removeFooter = new NativeButton("Remove footer");
        removeFooter.setId("remove-footer-btn");
        removeFooter.addClickListener(e -> login.getFooter().removeAll());

        Span customFormContent = new Span(CUSTOM_FORM_CONTENT);

        NativeButton addCustomForm = new NativeButton(
                "Add custom form content");
        addCustomForm.setId("add-custom-form-btn");
        addCustomForm.addClickListener(
                e -> login.getCustomFormArea().add(customFormContent));

        NativeButton removeCustomForm = new NativeButton(
                "Remove custom form content");
        removeCustomForm.setId("remove-custom-form-btn");
        removeCustomForm
                .addClickListener(e -> login.getCustomFormArea().removeAll());

        add(open, addFooter, removeFooter, addCustomForm, removeCustomForm);
    }
}
