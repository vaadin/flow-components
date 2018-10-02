package com.vaadin.flow.component.richtexteditor;

/*
 * #%L
 * Vaadin Rich Text Editor for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H1;

/**
 * Server-side component for the {@code <vaadin-rich-text-editor>} component.
 *
 * @author Vaadin Ltd
 *
 */
@Tag("vaadin-rich-text-editor")
@HtmlImport("frontend://bower_components/vaadin-rich-text-editor/src/vaadin-rich-text-editor.html")
public class RichTextEditor extends Component {

    /**
     * Initializes a new Rich Text Editor.
     */
    public RichTextEditor() {
        this("Foo bar");
    }

    /**
     * Initializes a new Rich Text Editor with the string content.
     * @param content the string content.
     */
    public RichTextEditor(String content) {
        getElement().appendChild(new H1(content).getElement());
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
    }
}
