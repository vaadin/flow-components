package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field")
public class CustomFieldView extends DemoView {

    @Override
    protected void initView() {
        basicDemo(); // Basic Usage
        validation(); // Validation
    }

    private void basicDemo() {
        addCard("Basic usage", new PhoneNumberField());
        addCard("Value change event", new ValueChangeEvent());
    }

    private void validation() {
        addCard("Validation", "Required", new MainForRequired());
    }

    // begin-source-example
    // source-example-heading: Basic usage
    public static class PhoneNumberField extends CustomField<String> {
        private final Select countryCode = new Select();
        private final TextField areaCode = new TextField();
        private final TextField subscriberCode = new TextField();

        PhoneNumberField() {
            setLabel("Phone number");
            countryCode.setItems("+358", "+46", "+34");
            countryCode.getStyle().set("width", "6em");
            countryCode.setPlaceholder("Code");
            areaCode.setPattern("[0-9]*");
            areaCode.setPreventInvalidInput(true);
            areaCode.setMaxLength(4);
            areaCode.setPlaceholder("Area");
            areaCode.getStyle().set("width", "5em");
            subscriberCode.setPattern("[0-9]*");
            subscriberCode.setPreventInvalidInput(true);
            subscriberCode.setMaxLength(8);
            subscriberCode.setPlaceholder("Subscriber");
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(countryCode, areaCode, subscriberCode);
            add(horizontalLayout);
        }

        @Override
        protected String generateModelValue() {
            return countryCode.getValue() + " " + areaCode.getValue() + " "
                    + subscriberCode.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            if (newPresentationValue == null) {
                areaCode.setValue(null);
                subscriberCode.setValue(null);
            }
        }
    }
    // end-source-example

    // begin-source-example
    // source-example-heading: Value change event
    public static class ValueChangeEvent extends HorizontalLayout {
        private final Div display = new Div();

        ValueChangeEvent() {
            // The code is reused from the previous example.
            PhoneNumberField phoneNumberField = new PhoneNumberField();
            phoneNumberField.addValueChangeListener(
                    e -> display.setText("Value: " + e.getValue()));
            add(phoneNumberField, display);
        }
    }
    // end-source-example

    // begin-source-example
    // source-example-heading: Required
    public static class MainForRequired extends HorizontalLayout {
        private final PriceField priceField = new PriceField();

        MainForRequired() {
            Binder<Product> binder = new Binder<>();
            Product product = new Product();
            binder.forField(priceField)
                    .asRequired("Please fill the price amount")
                    .bind(Product::getPrice, Product::setPrice);
            priceField.setLabel("Price");

            Button button = new Button("Submit", event -> {
                if (binder.writeBeanIfValid(product)) {
                    Notification.show("Submit successful", 2000,
                            Notification.Position.MIDDLE);
                }
            });

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setPadding(false);
            verticalLayout.add(priceField, button);
            add(verticalLayout);
        }
    }

    public static class PriceField extends CustomField<String> {
        private final NumberField price = new NumberField();
        private final Select<String> currency = new Select<>();

        PriceField() {
            currency.setItems("Euros", "Dollars", "Pounds");
            currency.getStyle().set("width", "6em");

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(price, currency);
            add(horizontalLayout);
        }

        @Override
        public String generateModelValue() {
            if (price.getValue() == null || currency.getValue() == null)
                return null;
            return price.getValue() + " " + currency.getValue();
        }

        @Override
        public void setPresentationValue(String newPresentationValue) {
            String[] part = newPresentationValue.split(" ");
            price.setValue(Double.valueOf(part[0]));
            currency.setValue(part.length > 1 ? part[1] : null);
        }
    }

    private static class Product {
        private String name;
        private String price;

        public Product() {
        }

        public Product(String name, String price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
    // end-source-example

}
