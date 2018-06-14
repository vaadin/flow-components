package com.vaadin.flow.component.confirmdialog;

/*
 * #%L
 * Vaadin Confirm Dialog for Vaadin 10
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

@Tag("vaadin-confirm-dialog")
@HtmlImport("frontend://bower_components/vaadin-confirm-dialog/vaadin-confirm-dialog.html")
public class ConfirmDialog extends Component
        implements HasSize, HasStyle, HasOrderedComponents<ConfirmDialog> {

    public ConfirmDialog() {
    }

}
