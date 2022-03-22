package com.vaadin.addon.spreadsheet.test.demoapps;

import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
@Push
public class PushTestCase extends SpreadsheetDemoUI {

    @Override
    protected void init(VaadinRequest request) {
        super.init(request);

        getOptionsLayout().addComponent(
                new Button("Hide/Show", new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        spreadsheet.setVisible(!spreadsheet.isVisible());
                        // new Thread(new LongRunningProcess()).start();
                    }
                }));
    }

    class LongRunningProcess implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ie) {

            }
            access(new Runnable() {

                @Override
                public void run() {
                    spreadsheet.setVisible(true);
                }
            });
        }

    }
}
