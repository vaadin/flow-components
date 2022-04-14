/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Menu Bar');
  };

  // const observer = new MutationObserver((records) => {
  //   records.forEach((mutation) => {
  //     if (mutation.type === 'attributes' && mutation.attributeName === 'disabled') {

  //     }
  //   });
  // });

  window.Vaadin.Flow.menubarConnector = {
    initLazy: function (menubar) {
      if (menubar.$connector) {
        return;
      }

      menubar.$connector = {
        /**
         * @param {string | undefined} appId
         * @param {number | undefined} nodeId
         */
        renderItems: tryCatchWrapper((appId, nodeId) => {
          if (!menubar.shadowRoot) {
            // workaround for https://github.com/vaadin/flow/issues/5722
            setTimeout(() => menubar.$connector.renderItems(appId, nodeId));
            return;
          }

          menubar._appId = appId || menubar._appId;
          menubar._nodeId = nodeId || menubar._nodeId;

          const items = window.Vaadin.Flow.contextMenuConnector.constructItemsTree(
            menubar._appId,
            menubar._nodeId
          );

          // Remove hidden items entirely from the array. Just hiding them
          // could cause the overflow button to be rendered without items.
          // resetContent needs to be called to make buttons visible again.
          //
          // The items-prop needs to be set even when all items are visible
          // to update the disabled state and re-render buttons.
          items = items.filter((item) => !item.component.hidden);

          // Propagate disabled state from items to parent buttons
          items.forEach((item) => {
            item.disabled = item.component.disabled;
          });

          // Assign the items to the menu-bar that will cause it to re-render.
          menubar.items = items;

          // Setup click listeners for the menu's buttons
          // to propagate click events from them to the context-menu-item components.
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

          menubar._buttons.forEach(() => {
            // Implement the Mutation Observer logic.
          });
        })
      }
    }
  };
})();
