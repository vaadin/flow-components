/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Benchmarks {@link FormAIController}: does the model fill the right fields
 * with the right values, and leave the others alone?
 */
@EnabledIfEnvironmentVariable(named = AIBenchmark.MODEL_VARIABLE, matches = ".+")
class FormAIControllerBenchmark {

    @RegisterExtension
    AIBenchmark bench = new AIBenchmark();

    /** Bean behind the form; property names double as field descriptions. */
    public static class Contact {
        private String name;
        private String email;
        private String phone;
        private String company;
        private Integer customerNumber;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public Integer getCustomerNumber() {
            return customerNumber;
        }

        public void setCustomerNumber(Integer customerNumber) {
            this.customerNumber = customerNumber;
        }
    }

    /** A fresh form per attempt, bound to {@link Contact}. */
    private static final class ContactForm {
        final TextField name = new TextField("Name");
        final EmailField email = new EmailField("Email");
        final TextField phone = new TextField("Phone");
        final TextField company = new TextField("Company");
        final IntegerField customerNumber = new IntegerField("Customer number");
        final Div root = new Div(name, email, phone, company, customerNumber);
        final FormAIController controller;

        ContactForm() {
            var binder = new Binder<>(Contact.class);
            binder.bindInstanceFields(this);
            controller = new FormAIController(root, binder);
        }
    }

    @Test
    void fillsContactFromEmailSignature() {
        bench.score(() -> {
            var form = new ContactForm();
            try (var conversation = bench.conversation(form.root,
                    form.controller)) {
                conversation.say("""
                        Fill in the form from this email signature:

                        Maria Koskinen
                        Sales Director, Northwind Oy
                        maria.koskinen@example.com | +358 40 123 4567
                        """);
            }
            Assertions.assertEquals("Maria Koskinen", form.name.getValue());
            Assertions.assertEquals("maria.koskinen@example.com",
                    form.email.getValue());
            Assertions.assertEquals("358401234567",
                    digits(form.phone.getValue()),
                    "phone number digits differ");
            Assertions.assertEquals("Northwind Oy", form.company.getValue());
            Assertions.assertNull(form.customerNumber.getValue(),
                    "customer number has no source in the input and must stay empty");
        });
    }

    @Test
    void correctsOnlyTheMentionedField() {
        bench.score(() -> {
            var form = new ContactForm();
            form.name.setValue("Maria Koskinen");
            form.email.setValue("maria.koskinen@example.com");
            form.phone.setValue("+358 40 123 4567");
            form.company.setValue("Northwind Oy");
            form.customerNumber.setValue(4711);
            try (var conversation = bench.conversation(form.root,
                    form.controller)) {
                conversation.say(
                        "The email address is wrong, it should be maria.k@northwind.example");
            }
            Assertions.assertEquals("maria.k@northwind.example",
                    form.email.getValue());
            Assertions.assertEquals("Maria Koskinen", form.name.getValue());
            Assertions.assertEquals("+358 40 123 4567", form.phone.getValue());
            Assertions.assertEquals("Northwind Oy", form.company.getValue());
            Assertions.assertEquals(4711, form.customerNumber.getValue());
        });
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
