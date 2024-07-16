/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Menu Bar', 'vaadin-menu-bar-flow');
  };

  window.Vaadin.Flow.menubarConnector = {
    initLazy: function (menubar) {
      if (menubar.$connector) {
        return;
      }
      menubar.$connector = {

        updateButtons: tryCatchWrapper(function () {
          if (!menubar.shadowRoot) {
            // workaround for https://github.com/vaadin/flow/issues/5722
            setTimeout(() => menubar.$connector.updateButtons());
            return;
          }

          // Propagate disabled state from items to parent buttons
          menubar.items.forEach(item => item.disabled = item.component.disabled);

          // Remove hidden items entirely from the array. Just hiding them
          // could cause the overflow button to be rendered without items.
          // resetContent needs to be called to make buttons visible again.
          //
          // The items-prop needs to be set even when all items are visible
          // to update the disabled state and re-render buttons.
          menubar.items = menubar.items.filter(item => !item.component.hidden);

          // Propagate click events from the menu buttons to the item components
          menubar._buttons.forEach(button => {
            if (button.item && button.item.component) {
              button.addEventListener('click', e => {
                if (e.composedPath().indexOf(button.item.component) === -1) {
                  button.item.component.click();
				  e.stopPropagation();
                }
              });
            }
          });
        })
      };
    }
  };
})();
