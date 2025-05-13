/*
 * Copyright 2000-2025 Vaadin Ltd.
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
window.Vaadin.Flow.messageListConnector = {
  /**
   * Fully replaces the items in the list with the given items.
   */
  setItems(list, items, locale) {
    const formatter = new Intl.DateTimeFormat(locale, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    });
    list.items = items.map((item) =>
      item.time
        ? Object.assign(item, {
            time: formatter.format(new Date(item.time))
          })
        : item
    );
  },

  /**
   * Sets the text of the item at the given index to the given text.
   */
  setItemText(list, text, index) {
    list.items[index].text = text;
    list.items = [...list.items];
  },

  /**
   * Appends the given text to the text of the item at the given index.
   */
  appendItemText(list, appendedText, index) {
    const currentText = list.items[index].text || '';
    this.setItemText(list, currentText + appendedText, index);
  },

  /**
   * Adds the given items to the end of the list.
   */
  addItems(list, newItems, locale) {
    this.setItems(list, [...(list.items || []), ...newItems], locale);
  }
};
