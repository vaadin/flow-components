package com.vaadin.flow.component.details.examples;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        Details details = new Details();
        details.setSummary(new Span("Some summary"));
        details.setContent(new Text("Some content"));

        Details detailsDisabled = new Details();
        detailsDisabled.setOpened(true);
        detailsDisabled.setEnabled(false);
        detailsDisabled.setSummaryText("Disabled heading");
        detailsDisabled.addContent(new H3("Disabled content"));
        detailsDisabled.addContent(new Span("Always visible content"));

        Details detailsThemed = new Details("Small Reversed Filled Summary", new Span("Themed Content"));
        detailsThemed.addThemeVariants(DetailsVariant.values());

        Details detailsListener = new Details("Details with opened change listener", new Span("Content"));
        detailsListener.addOpenedChangeListener(e ->
                Notification.show("opened-change"));

        add(details, detailsDisabled, detailsThemed, detailsListener);
    }
}
