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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for RichTextEditor component.
 */
@Route(value = "rich-text-editor", layout = MainLayout.class)
@PageTitle("Rich Text Editor | Vaadin Kitchen Sink")
public class RichTextEditorDemoView extends VerticalLayout {

    public RichTextEditorDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Rich Text Editor Component"));
        add(new Paragraph("RichTextEditor provides WYSIWYG text editing capabilities."));

        // Basic rich text editor
        RichTextEditor basic = new RichTextEditor();
        basic.setWidthFull();
        basic.setHeight("300px");
        addSection("Basic Rich Text Editor", basic);

        // With initial content
        RichTextEditor withContent = new RichTextEditor();
        withContent.setWidthFull();
        withContent.setHeight("300px");
        withContent.setValue(
            "<h2>Welcome to Rich Text Editor</h2>" +
            "<p>This editor supports <strong>bold</strong>, <em>italic</em>, and <u>underlined</u> text.</p>" +
            "<p>You can also create:</p>" +
            "<ul>" +
            "<li>Bullet lists</li>" +
            "<li>Like this one</li>" +
            "</ul>" +
            "<ol>" +
            "<li>Numbered lists</li>" +
            "<li>Work too</li>" +
            "</ol>" +
            "<blockquote>And even blockquotes for important text.</blockquote>"
        );
        addSection("With Initial Content", withContent);

        // With value change listener
        RichTextEditor withListener = new RichTextEditor();
        withListener.setWidthFull();
        withListener.setHeight("250px");
        Button getValueBtn = new Button("Get HTML Value", e -> {
            String value = withListener.getValue();
            Notification.show("HTML length: " + value.length() + " characters");
        });
        addSection("With Value Retrieval", withListener, getValueBtn);

        // Read-only mode
        RichTextEditor readonly = new RichTextEditor();
        readonly.setWidthFull();
        readonly.setHeight("200px");
        readonly.setValue(
            "<p>This rich text editor is in <strong>read-only mode</strong>.</p>" +
            "<p>Users can view but not edit the content.</p>"
        );
        readonly.setReadOnly(true);
        addSection("Read-only Mode", readonly);

        // Disabled mode
        RichTextEditor disabled = new RichTextEditor();
        disabled.setWidthFull();
        disabled.setHeight("200px");
        disabled.setValue("<p>This editor is disabled.</p>");
        disabled.setEnabled(false);
        addSection("Disabled Mode", disabled);

        // Compact size
        RichTextEditor compact = new RichTextEditor();
        compact.setWidthFull();
        compact.setHeight("150px");
        addSection("Compact Size", compact);
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
