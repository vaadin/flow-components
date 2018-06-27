# Confirm Dialog Component for Vaadin Flow

### Overview
Vaadin Confirm Dialog is an easy to use web component to ask the user to confirm a choice.

### License & Author

This add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add Confirm Dialog to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-confirm-dialog-flow</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
ConfirmDialog dialog = new ConfirmDialog("Unsaved changes",
    "Do you want to save or discard your changes before navigating away?",
    "Save", event -> { /* handle confirm */ },
    "Discard", event -> { /* handle discard */ },
    "Cancel", event -> { /* handle cancel */ } );
dialog.open();
```
