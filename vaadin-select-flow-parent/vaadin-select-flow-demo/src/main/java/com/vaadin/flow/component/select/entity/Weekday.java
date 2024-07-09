/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.select.entity;

public enum Weekday {

    MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY(
            "Thursday"), FRIDAY(
                    "Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");

    private String value;

    Weekday(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
