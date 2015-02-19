package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FormulaBarWidget extends Composite {

    private final TextBox formulaField;
    private final TextBox addressField;

    private String cachedAddressFieldValue;
    private String cachedFunctionFieldValue;

    private final FormulaBarHandler handler;

    public FormulaBarWidget(FormulaBarHandler selectionManager) {
        handler = selectionManager;
        formulaField = new TextBox();
        formulaField.setTabIndex(2);
        addressField = new TextBox();
        addressField.setTabIndex(1);
        formulaField.setStyleName("functionfield");
        addressField.setStyleName("addressfield");

        FlowPanel panel = new FlowPanel();
        FlowPanel left = new FlowPanel();
        FlowPanel right = new FlowPanel();
        left.setStyleName("fixed-left-panel");
        right.setStyleName("adjusting-right-panel");
        left.add(addressField);
        right.add(formulaField);
        panel.add(left);
        panel.add(right);

        initWidget(panel);

        setStyleName("functionbar");

        initListeners();
    }

    private void initListeners() {
        Event.sinkEvents(addressField.getElement(), Event.ONKEYUP
                | Event.FOCUSEVENTS);
        Event.setEventListener(addressField.getElement(), new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                final int type = event.getTypeInt();
                if (type == Event.ONKEYUP) {
                    final int keyCode = event.getKeyCode();
                    if (keyCode == KeyCodes.KEY_ENTER) {
                        // submit address value
                        handler.onAddressEntered(addressField.getValue()
                                .replaceAll(" ", ""));
                        addressField.setFocus(false);
                    } else if (keyCode == KeyCodes.KEY_ESCAPE) {
                        revertCellAddressValue();
                        handler.onAddressFieldEsc();
                    }
                } else if (type == Event.ONFOCUS) {
                    addressField.getElement().getStyle()
                            .setTextAlign(TextAlign.LEFT);
                } else {
                    addressField.getElement().getStyle().clearTextAlign();
                }
            }
        });
        Event.sinkEvents(formulaField.getElement(), Event.KEYEVENTS
                | Event.FOCUSEVENTS);
        Event.setEventListener(formulaField.getElement(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                switch (event.getTypeInt()) {
                case Event.ONFOCUS:
                    cachedFunctionFieldValue = formulaField.getValue();
                    handler.onFormulaFieldFocus(cachedFunctionFieldValue);
                    break;
                case Event.ONBLUR:
                    handler.onFormulaFieldBlur(formulaField.getValue());
                    break;
                case Event.ONKEYDOWN:
                    handleFunctionFieldKeyDown(event);
                    break;
                case Event.ONPASTE:
                case Event.ONKEYPRESS:
                    scheduleForumulaValueUpdate();
                    break;
                default:
                    break;
                }
            }

        });
    }

    private void scheduleForumulaValueUpdate() {

        if (handler.isTouchMode()) {

            /*
             * Can't be done deferred because that is apparently too fast in
             * iPads, resulting in textfield not having new value when this is
             * run. A timer makes sure hat the textfield actually has the
             * entered character.
             */
            new Timer() {

                @Override
                public void run() {
                    handler.onFormulaValueChange(formulaField.getValue());
                }
            }.schedule(100);

        } else {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    handler.onFormulaValueChange(formulaField.getValue());
                }
            });
        }
    }

    private void handleFunctionFieldKeyDown(Event event) {
        switch (event.getKeyCode()) {
        case KeyCodes.KEY_BACKSPACE:
            scheduleForumulaValueUpdate();
            break;
        case KeyCodes.KEY_ENTER:
            handler.onFormulaEnter(formulaField.getValue());
            event.stopPropagation();
            event.preventDefault();
            break;
        case KeyCodes.KEY_TAB:
            handler.onFormulaTab(formulaField.getValue());
            event.stopPropagation();
            break;
        case KeyCodes.KEY_ESCAPE:
            formulaField.setValue(cachedFunctionFieldValue);
            handler.onFormulaEsc();
            event.stopPropagation();
            event.preventDefault();
        default:
            break;
        }
    }

    public void revertCellAddressValue() {
        addressField.setValue(cachedAddressFieldValue);
        addressField.setFocus(false);
    }

    public void revertCellValue() {
        formulaField.setValue(cachedFunctionFieldValue);
    }

    public void setSelectedCellAddress(String selection) {
        cachedAddressFieldValue = selection;
        addressField.setValue(selection);
    }

    public void setCellPlainValue(String plainValue) {
        formulaField.setValue(plainValue);
    }

    public void setCellFormulaValue(String formula) {
        if (!formula.isEmpty()) {
            formulaField.setValue("=" + formula);
        } else {
            formulaField.setValue(formula);
        }
    }

    public void setFormulaFieldValue(String value) {
        formulaField.setValue(value);
    }

    public void clear() {
        setCellPlainValue("");
        setSelectedCellAddress("");
    }

    public String getFormulaFieldValue() {
        return formulaField.getValue();
    }

    public void setFormulaFieldEnabled(boolean enabled) {
        formulaField.setEnabled(enabled);
    }

    public void cacheFormulaFieldValue() {
        cachedFunctionFieldValue = formulaField.getValue();
    }

}
