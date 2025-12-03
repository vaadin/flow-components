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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/remove-all")
public class DialogRemoveAllPage extends Div {
    public DialogRemoveAllPage() {
        Dialog dialog = new Dialog();

        dialog.add(new Span("Main content"));
        dialog.getHeader().add(new Span("Header content"));
        dialog.getFooter().add(new Span("Footer content"));

        NativeButton replaceContent = new NativeButton("Replace content", e -> {
            dialog.removeAll();
            dialog.add(new Span("Updated content"));
        });
        replaceContent.setId("replace-content");
        dialog.add(replaceContent);

        dialog.open();
    }
}
