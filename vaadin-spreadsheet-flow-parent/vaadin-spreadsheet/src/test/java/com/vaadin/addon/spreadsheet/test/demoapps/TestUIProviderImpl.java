package com.vaadin.addon.spreadsheet.test.demoapps;

import com.vaadin.addon.spreadsheet.test.FreezePaneLocaleUI;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
public class TestUIProviderImpl extends UIProvider {

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        String name = (event.getRequest()).getPathInfo();
        if (name.contains(TestexcelsheetUI.class.getSimpleName())) {
            return TestexcelsheetUI.class;
        } else if (name.contains(SpreadsheetDemoUI.class.getSimpleName())) {
            return SpreadsheetDemoUI.class;
        } else if (name.contains(PushTestCase.class.getSimpleName())) {
            return PushTestCase.class;
        } else if (name.contains(TouchUI.class.getSimpleName())) {
            return TouchUI.class;
        } else if (name.contains(MultipleSpreadsheetUI.class.getSimpleName())) {
            return MultipleSpreadsheetUI.class;
        } else if (name.contains(FreezePaneLocaleUI.class.getSimpleName())) {
            return FreezePaneLocaleUI.class;
        } else if (name.contains(TabsheetTestUI.class.getSimpleName())) {
            return TabsheetTestUI.class;
        } else if (name.contains(EmptySpreadsheetUI.class.getSimpleName())) {
            return EmptySpreadsheetUI.class;
        }
        return null;
    }
}
