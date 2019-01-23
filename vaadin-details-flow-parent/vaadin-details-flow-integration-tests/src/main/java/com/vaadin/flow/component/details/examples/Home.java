package com.vaadin.flow.component.details.examples;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        Details details = new Details();
        details.setSummary(new Span("Some summary"));
        details.setContent(new Span("Some content"));

        Details detailsText = new Details();
        detailsText.setOpened(true);
        detailsText.setSummaryText("Summary Text");
        detailsText.setContentText("Content Text");

        Details detailsDisabled = new Details();
        detailsDisabled.setOpened(true);
        detailsDisabled.setEnabled(false);
        detailsDisabled.setSummaryText("Disabled heading");
        detailsDisabled.setContentText("Always visible content");

        add(details, detailsText, detailsDisabled);
    }
}
