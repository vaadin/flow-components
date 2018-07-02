package com.vaadin.flow.component.cookieconsent.vaadincom;

import java.util.Random;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("vaadin-cookie-consent")
public class CookieConsentView extends DemoView {
    private static final Random random = new Random();

    @Override
    protected void initView() {
        confirmDeleteDialog();
        meetingStartingAlert();
    }

    private void meetingStartingAlert() {
        // @formatter:off
	// begin-source-example
	// source-example-heading: Default Values Example
	CookieConsent dialog = new CookieConsent();
	// end-source-example
	// @formatter:on

        createCard("Default Values Example", "Open dialog", dialog);
    }

    private void confirmDeleteDialog() {
        // @formatter:off
	// begin-source-example
	// source-example-heading: Customization Example
	CookieConsent dialog = new CookieConsent("We are using cookies to make your visit here awesome!", "Cool!",
			"Why?", "https://vaadin.com/terms-of-service", Position.BOTTOM_LEFT);
	// end-source-example
	// @formatter:on

        dialog.setCookieName(createCookieName());
        createCard("Customization Example", "Open dialog", dialog);
    }

    private void createCard(String heading, String buttonText,
            CookieConsent element) {
        final Div messageDiv = createMessageDiv();
        final Button button = new Button(buttonText);
        addCard(heading, button, messageDiv);
    }

    private Div createMessageDiv() {
        final Div message = new Div();
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private static String createCookieName() {
        final String values = "ABDEFGHIJKLMNQRSTUVXYZ0123456789";
        return "consentdemo-" + random.ints(0, values.length()).limit(50)
                .mapToObj(values::charAt).collect(StringBuilder::new,
                        StringBuilder::append, (a, b) -> a.append(b))
                .toString();
    }
}
