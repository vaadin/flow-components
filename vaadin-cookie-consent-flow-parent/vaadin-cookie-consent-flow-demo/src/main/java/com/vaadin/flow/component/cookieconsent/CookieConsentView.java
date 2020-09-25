package com.vaadin.flow.component.cookieconsent;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("vaadin-cookie-consent")
public class CookieConsentView extends DemoView {

    private CookieConsent cookieConsent;

    @Override
    protected void initView() {
        defaultValues();
        customValues();
    }

    private void defaultValues() {
        // @formatter:off
	// begin-source-example
	// source-example-heading: Basic usage
	CookieConsent dialog = new CookieConsent();
	add(dialog);
	// end-source-example
	// @formatter:on

        createCard("Basic usage", dialog);
    }

    private void customValues() {
        // @formatter:off
	// begin-source-example
	// source-example-heading: Customize texts
        CookieConsent dialog = new CookieConsent(
                "We are using cookies to make your visit here awesome!",
                "Cool!", "Why?", "https://vaadin.com/terms-of-service",
                Position.BOTTOM_LEFT);
	add(dialog);
	// end-source-example
	// @formatter:on

        createCard("Customize texts", dialog);
    }

    private void createCard(String heading, CookieConsent cookieConsent) {
        final Button button = new Button("Show consent popup");
        button.addClickListener(e -> {
            clearCookieConsent();
            this.cookieConsent = cookieConsent;
            add(cookieConsent);
            getUI().get().getPage().executeJavaScript("$0._show()",
                    cookieConsent.getElement());
        });
        addCard(heading, button);
    }

    private void clearCookieConsent() {
        if (cookieConsent != null) {
            remove(cookieConsent);
        }
    }
}
