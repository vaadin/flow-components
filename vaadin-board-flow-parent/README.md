### Overview
Vaadin addon allows to create flexible responsive layouts and build nice looking dashboard.
Vaadin Board key feature is how it effectively reorders UI components on different screen sizes, maximizing the use of space and looking stunning.

**Board requires Vaadin Framework 8.1 or newer.**

### License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).
For license terms, see LICENSE.txt.

### Installing
Add vaadin-board and vaadin-board-precompiled to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-board</artifactId>
    <version>1.0.0-alpha5</version>
  </dependency>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-board-precompiled</artifactId>
    <version>1.0.0-alpha5</version>
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
setContent(board);
```

[Demo](https://demo.vaadin.com/vaadin-board)

[Documentation](https://vaadin.com/docs/-/part/board/board-overview.html)
