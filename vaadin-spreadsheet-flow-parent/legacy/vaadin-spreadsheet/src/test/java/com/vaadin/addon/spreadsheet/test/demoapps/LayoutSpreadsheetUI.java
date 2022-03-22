package com.vaadin.addon.spreadsheet.test.demoapps;

import java.io.File;
import java.io.IOException;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("demo")
@Widgetset("com.vaadin.addon.spreadsheet.Widgetset")
public class LayoutSpreadsheetUI extends UI {


    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.setHeightUndefined();
        setContent(layout);

        HorizontalLayout buttons = new HorizontalLayout();

        layout.addComponent(buttons);


        HorizontalLayout showHideLayout = new HorizontalLayout();
        showHideLayout.setWidth(100, Unit.PERCENTAGE);
        showHideLayout.setHeight(100, Unit.PIXELS);
        layout.addComponent(showHideLayout);

        Button showButton = createShowHideButton(true, showHideLayout);
        Button hiddenButton = createShowHideButton(false, showHideLayout);
        buttons.addComponent(showButton);
        buttons.addComponent(hiddenButton);

        Component spreadsheet=createSpreadsheet();
        layout.addComponent(buttons);
        layout.addComponent(showHideLayout);
        layout.addComponent(spreadsheet);
    }

    private Component createSpreadsheet() {
        Component spreadsheet;
        ClassLoader classLoader = SpreadsheetDemoUI.class.getClassLoader();
        File file = new File(classLoader.getResource("test_sheets/500x200test.xlsx").getFile());
        try {
            spreadsheet = new Spreadsheet(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        spreadsheet.setSizeFull();
        return spreadsheet;
    }

    private Button createShowHideButton(final boolean isShown, final Component component) {
        Button button = new Button();

        if(isShown){
            button.setCaption("Show");
        }
        else {
            button.setCaption("Hide");
        }
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                component.setVisible(isShown);
            }
        });
        return button;
    }

}
