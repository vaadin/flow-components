package com.vaadin.flow.component.login.vaadincom;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-login")
public class LoginView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
        overlay();
        internationalization();
        addCard(" ");
        customTitle();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Login
        Login component = new Login();
        component.addLoginListener(e -> {
            boolean isAuthenticated = authenticate(e);
            if (isAuthenticated) {
                navigateToMainPage();
            } else {
                component.setError(true);
            }
        });
        // end-source-example

        addCard("Basic Login", createLayout(component));
    }

    @SuppressWarnings("unused")
    private boolean authenticate(AbstractLogin.LoginEvent e) {
        return false;
    }

    private void navigateToMainPage() {

    }

    private void internationalization() {
        // begin-source-example
        // source-example-heading: Login with internationalization
        Login component = new Login();
        Button updateI18nButton = new Button("Switch to Brazilian Portuguese",
            event -> component.setI18n(createPortugueseI18n()));
        // end-source-example

        addCard("Login with internationalization", createLayout(component), updateI18nButton);
    }

    private void overlay() {
        // begin-source-example
        // source-example-heading: Login in an overlay
        LoginOverlay component = new LoginOverlay();
        component.addLoginListener(e -> component.close());
        Button open = new Button("Open login overlay",
            e -> component.setOpened(true));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation("To close the login form submit non-empty username and password");
        component.setI18n(i18n);
        // end-source-example

        addCard("Login in an overlay", component, open);
    }

    private void customTitle() {
        // begin-source-example
        // source-example-heading: Title with custom HTML
        LoginOverlay component = new LoginOverlay();
        H1 title = new H1();
        title.getStyle().set("color", "var(--lumo-base-color)");
        Icon icon = VaadinIcon.VAADIN_H.create();
        icon.setSize("30px");
        icon.getStyle().set("top", "-4px");
        title.add(icon);
        title.add(new Text(" My App"));
        component.setTitle(title);
        component.addLoginListener(e -> component.close());

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation("To close the login form submit non-empty username and password");
        component.setI18n(i18n);

        Button open = new Button("Open login overlay",
                e -> component.setOpened(true));
        // end-source-example

        addCard("Title with custom HTML", component, open);
    }

    private Component createLayout(Login login) {
        VerticalLayout layout = new VerticalLayout();
        layout.add(login);

        layout.setSizeFull();
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("background", "var(--lumo-shade-5pct)");

        return layout;
    }

    // NOTE: heading is an unicode space
    // begin-source-example
    // source-example-heading:  
    private LoginI18n createPortugueseI18n() {
        final LoginI18n i18n = LoginI18n.createDefault();

        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Nome do aplicativo");
        i18n.getHeader().setDescription("Descrição do aplicativo");
        i18n.getForm().setUsername("Usuário");
        i18n.getForm().setTitle("Acesse a sua conta");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setPassword("Senha");
        i18n.getForm().setForgotPassword("Esqueci minha senha");
        i18n.getErrorMessage().setTitle("Usuário/senha inválidos");
        i18n.getErrorMessage()
            .setMessage("Confira seu usuário e senha e tente novamente.");
        i18n.setAdditionalInformation(
            "Caso necessite apresentar alguma informação extra para o usuário"
                + " (como credenciais padrão), este é o lugar.");
        return i18n;
    }
    // end-source-example
}
