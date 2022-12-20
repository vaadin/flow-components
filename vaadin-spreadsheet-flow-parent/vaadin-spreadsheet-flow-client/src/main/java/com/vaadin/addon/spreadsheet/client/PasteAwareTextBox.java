/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

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
