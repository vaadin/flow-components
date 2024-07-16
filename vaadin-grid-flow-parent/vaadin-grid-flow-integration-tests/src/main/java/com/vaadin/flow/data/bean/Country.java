/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.bean;

public enum Country {

    FINLAND("Finland"), SWEDEN("Sweden"), USA("USA"), RUSSIA(
            "Russia"), NETHERLANDS("Netherlands"), SOUTH_AFRICA("South Africa");

    private String name;

    private Country(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
