/*
 * Copyright 2000-2019 Vaadin Ltd.
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
window.Vaadin.Flow.menubarConnector = {
  initLazy: function (menubar) {
    if (menubar.$connector) {
      return;
    }
    menubar.$connector = {

      updateButtons: function () {
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
              }
            });
          }
        });
      }
    }
  }
};
