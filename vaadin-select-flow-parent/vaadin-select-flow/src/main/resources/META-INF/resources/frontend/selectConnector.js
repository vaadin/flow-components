window.Vaadin.Flow.selectConnector = {
initLazy: function (select) {
    const _findListBoxElement = function() {
        for (let i = 0; i < select.childElementCount; i++) {
            const child = select.children[i];
            if ("VAADIN-LIST-BOX" === child.tagName.toUpperCase()) {
                return child;
            }
        }
    };

      // do not init this connector twice for the given select
      if (select.$connector) {
          return;
      }

      select.$connector = {};

      select.renderer = function(root) {
          const listBox = _findListBoxElement();
          if (listBox) {
              if (root.firstChild) {
                  root.firstChild.remove();
              }
              root.appendChild(listBox);
          }
      };
  }
};
