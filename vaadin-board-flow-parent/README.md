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
    <version>1.0.0.alpha1</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
Board board = new Board();
board.setSizeFull();

Label lbl1 =  new Label("LABEL1");
Label lbl2 =  new Label("LABEL2");
Label lbl3 =  new Label("LABEL3");
Label lbl4 =  new Label("LABEL4");

board.addRow(lbl1, lbl2, lbl3, lbl4);
add(board);
```

[Demo](https://demo.vaadin.com/vaadin-board)

[Documentation](https://vaadin.com/docs/v10/board/board-overview.html)
