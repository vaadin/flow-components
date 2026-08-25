import { expect } from 'chai';
import './env-setup.js';
// The path the combo box connector was available at before it was moved into
// the vaadin-combo-box folder. An add-on built against an earlier version
// imports it this way, so it has to keep working until the next major. This
// file deliberately imports nothing from the new path, so that a passing test
// can only be explained by the deprecated entrypoint itself.
import * as deprecatedComboBoxConnector from '../frontend/generated/jar-resources/comboBoxConnector.js';

describe('combo box connector - deprecated entrypoint', () => {
  it('should register the combo box connector', () => {
    expect(window.Vaadin.Flow.comboBoxConnector).to.exist;
  });

  it('should re-export the combo box connector class', () => {
    expect(deprecatedComboBoxConnector.ComboBoxConnector).to.exist;
  });
});
