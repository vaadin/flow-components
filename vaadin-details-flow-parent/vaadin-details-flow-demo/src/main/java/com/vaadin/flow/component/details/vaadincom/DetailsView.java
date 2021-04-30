package com.vaadin.flow.component.details.vaadincom;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-details")
public class DetailsView extends DemoView {

    @Override
    protected void initView() {
        details();
        disabledDetails();
        smallSizeDetails();
        reverseFilledDetails();
    }

    private void details() {
        // begin-source-example
        // source-example-heading: Details
        Details component = new Details("Expandable Details",
                new Text("Toggle using mouse, Enter and Space keys."));
        component.addOpenedChangeListener(e ->
                Notification.show(e.isOpened() ? "Opened" : "Closed"));
        add(component);
        // end-source-example

        addCard("Details", component);
    }

    private void disabledDetails() {
        // begin-source-example
        // source-example-heading: Disabled Details
        Details component = new Details(new Span("Disabled heading"),
                new Span("Always visible content."));
        component.setEnabled(false);
        component.setOpened(true);
        add(component);
        // end-source-example

        addCard("Disabled Details", component);
    }

    private void smallSizeDetails() {
        // begin-source-example
        // source-example-heading: Small Size Details
        Details component = new Details();
        component.setSummaryText("Small");
        component.addContent(new H3("Panel content heading"), new Text("Panel content text"));
        component.addThemeVariants(DetailsVariant.SMALL);
        add(component);
        // end-source-example

        addCard("Small Size Details", component);
    }

    private void reverseFilledDetails() {
        // begin-source-example
        // source-example-heading: Reverse Filled Details
        Details component = new Details("Reverse Filled", new Text("Panel content"));
        component.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        add(component);
        // end-source-example

        addCard("Reverse Filled Details", component);
    }
}
