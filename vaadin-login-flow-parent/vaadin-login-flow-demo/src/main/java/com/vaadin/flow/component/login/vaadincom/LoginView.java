package com.vaadin.flow.component.login.vaadincom;

import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-login")
public class LoginView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        Login component = new Login();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
