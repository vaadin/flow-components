[![Published on vaadin.com/directory](https://img.shields.io/vaadin-directory/status/vaadin-board.svg)](https://vaadin.com/directory/component/vaadin-board)

### Overview
Vaadin Board allows to create flexible responsive layouts and build nice looking dashboards.
Vaadin Board key feature is how it effectively reorders UI components on different screen sizes, maximizing the use of space and looking stunning.

This version is for Vaadin 10 and newer. For a Vaadin Framework 8 compatible version, see https://vaadin.com/directory/component/vaadin-board/1.0.1

### License & Author

This add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add vaadin-board to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-board-flow</artifactId>
    <version>2.0.0</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
Board board = new Board();
board.setSizeFull();

Div child1 = new Div(); child1.setText("This could be chart 1");
Div child2 = new Div(); child2.setText("This could be chart 2");
Div child3 = new Div(); child3.setText("This could be chart 3");
Div child4 = new Div(); child4.setText("This could be chart 4");

board.addRow(child1, child2, child3, child4);
add(board);
```

[Demo](https://vaadin.com/components/vaadin-board/java-examples)

[Documentation](https://vaadin.com/components/vaadin-board)
