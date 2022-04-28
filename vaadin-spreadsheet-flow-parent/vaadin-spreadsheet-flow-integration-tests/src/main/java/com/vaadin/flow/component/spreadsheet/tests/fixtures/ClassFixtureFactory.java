package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassFixtureFactory extends Instantiator<SpreadsheetFixture>
        implements SpreadsheetFixtureFactory {

    public ClassFixtureFactory(Class<? extends SpreadsheetFixture> clazz) {
        super(clazz);
    }

    @Override
    public SpreadsheetFixture create() {
        return getIt();
    }
}

class Instantiator<T> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Instantiator.class);

    private Class<? extends T> clazz;

    public Instantiator(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    public T getIt() {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            LOGGER.info("ERROR creating an instance of " + clazz.getName(), e);
        }

        return null;
    }
}
