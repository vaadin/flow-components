package com.vaadin.flow.component.select.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.select.SelectView.Country;

public class CountryData {
    private static final List<Country> COUNTRY_LIST = createCountryList();

    private static List<Country> createCountryList() {
        List<Country> countryList = new ArrayList<>();

        countryList.add(new Country("Finland", "Europe", "Helsinki"));
        countryList.add(new Country("Russia", "Europe", "Moscow"));
        countryList.add(new Country("Iran", "Asia", "Tehran"));
        countryList.add(new Country("Romania", "Europe", "Bucharest"));
        countryList.add(new Country("Portugal", "Europe", "Lisbon"));
        countryList.add(new Country("Brazil", "America", "Brasilia"));
        countryList.add(new Country("Dominica", "America", "Roseau"));
        countryList.add(new Country("Egypt", "Africa", "Cairo"));

        return Collections.unmodifiableList(countryList);
    }

    public List<Country> getCountries() {
        return COUNTRY_LIST;
    }

}
