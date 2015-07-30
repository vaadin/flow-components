package com.vaadin.addon.spreadsheet.test.fixtures;

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
    private Class<? extends T> clazz;

    public Instantiator(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    public T getIt() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
