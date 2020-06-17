package com.vaadin.flow.component.treegrid.demo.entity;

public class Account {
    private String code;
    private String title;
    private Account parent;

    public Account(String code, String title, Account parent) {
        this.code = code;
        this.title = title;
        this.parent = parent;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Account getParent() {
        return parent;
    }

    public void setParent(Account parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return title;
    }

}
