
package com.vaadin.flow.component.radiobutton;

public class ItemHelper {
    private String name;
    private String code;

    public ItemHelper(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        ItemHelper other = (ItemHelper) obj;
        if (code == null && other.code != null) {
            return false;
        } else if (!code.equals(other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
