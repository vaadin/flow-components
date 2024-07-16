/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.demo.data;

import java.util.ArrayList;
import java.util.List;

public class CountryData {
    public static final String UNITED_STATES = "United States";
    private static final List<String> COUNTRY_LIST = createCountryList();

    private static List<String> createCountryList() {
        List<String> countryList = new ArrayList<>();

        countryList.add("Afghanistan");
        countryList.add("Albania");
        countryList.add("Algeria");
        countryList.add("Andorra");
        countryList.add("Romania");
        countryList.add("Argentina");
        countryList.add("Armenia");
        countryList.add("Australia");
        countryList.add("Austria");
        countryList.add("Denmark");
        countryList.add("Estonia");
        countryList.add("Finland");
        countryList.add("France");
        countryList.add("Germany");
        countryList.add("Iceland");
        countryList.add("Iran");
        countryList.add("Japan");
        countryList.add("Poland");
        countryList.add("United Kingdom");
        countryList.add(UNITED_STATES);

        return countryList;
    }

    public List<String> getAllCountries() {
        return COUNTRY_LIST;
    }
}
