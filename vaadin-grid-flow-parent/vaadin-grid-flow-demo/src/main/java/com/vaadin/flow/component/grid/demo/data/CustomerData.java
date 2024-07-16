/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.demo.data;

import com.vaadin.flow.component.grid.demo.entity.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerData {
    private static final List<Customer> CUSTOMER_LIST = createCustomerList();

    private static List<Customer> createCustomerList() {
        List<Customer> customerList = new ArrayList<>();

        customerList.add(new Customer(91, "Jack", "Giles", "United States",
                "Washington"));
        customerList.add(new Customer(92, "Nathan", "Patterson",
                "United States", "Florida"));
        customerList.add(
                new Customer(93, "Ahmad", "Matin", "Afghanistan", "Zabol"));
        customerList.add(new Customer(94, "Peter", "Buchanan", "United Kingdom",
                "London"));
        customerList.add(
                new Customer(95, "Andrew", "Bauer", "Australia", "Sydney"));
        customerList.add(new Customer(96, "Samuel", "Lee", "United States",
                "Washington"));
        customerList.add(
                new Customer(97, "Anton", "Ross", "United States", "Nevada"));
        customerList
                .add(new Customer(98, "Artur", "Signell", "Finland", "Turku"));
        customerList
                .add(new Customer(99, "Johannes", "Häyry", "Finland", "Turku"));
        customerList.add(new Customer(100, "Alexandru", "Chiuariu", "Romania",
                "Bucharest"));
        customerList
                .add(new Customer(101, "Mehdi", "Javan", "Iran", "Birjand"));
        customerList.add(new Customer(102, "Jack", "Woodward", "United States",
                "Georgia"));
        customerList.add(new Customer(103, "Avery", "Reeves", "United States",
                "Washington"));

        return customerList;
    }

    public List<Customer> getAllCustomers() {
        return CUSTOMER_LIST;
    }
}
