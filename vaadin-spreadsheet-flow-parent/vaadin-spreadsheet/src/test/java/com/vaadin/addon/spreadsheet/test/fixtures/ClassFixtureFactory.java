package com.vaadin.addon.spreadsheet.test.fixtures;

public class ClassFixtureFactory extends Instantiator<SpreadsheetFixture>
        implements SpreadsheetFixtureFactory {

    public ClassFixtureFactory(Class<? extends SpreadsheetFixture> clazz) {
        super(clazz);
    }

    @Override
    public SpreadsheetFixture create() {
        // TODO Auto-generated method stub
        return getIt();
    }

    // private Class<? extends SpreadsheetFixture> clazz;
    //
    // public ClassFixtureFactory(Class<? extends SpreadsheetFixture> clazz) {
    // this.clazz = clazz;
    // }
    //
    // @Override
    // public SpreadsheetFixture create() {
    // try {
    // return clazz.newInstance();
    // } catch (InstantiationException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // return null;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
