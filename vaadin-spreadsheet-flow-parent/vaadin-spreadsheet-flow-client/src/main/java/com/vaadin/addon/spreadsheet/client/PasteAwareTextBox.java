package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/**
 * {@link TextBox} that notices incoming paste events, and notifies SheetWidget.
 */
public class PasteAwareTextBox extends TextBox {

    private SheetWidget widget;

    public PasteAwareTextBox(SheetWidget widget) {
        super();
        this.widget = widget;

        sinkEvents(Event.ONPASTE);
    }

    @Override
    public void onBrowserEvent(Event event) {

        if (event.getTypeInt() == Event.ONPASTE) {

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    widget.handleInputElementValueChange(true);
                }
            });
        }

        super.onBrowserEvent(event);
    }
}
