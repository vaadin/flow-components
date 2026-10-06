import { expect } from 'chai';
import { fixtureSync } from '@vaadin/testing-helpers';
import { FlowComboBox, FlowMultiSelectComboBox, init } from './shared.ts';
import '@vaadin/combo-box';
import '@vaadin/multi-select-combo-box';

describe('multi-select-combo-box connector', () => {
  describe('toggle select all handler', () => {
    it('should not be set on a combo box', () => {
      const comboBox: FlowComboBox = fixtureSync('<vaadin-combo-box></vaadin-combo-box>');
      init(comboBox);
      expect((comboBox as FlowMultiSelectComboBox)._toggleSelectAllHandler).to.be.undefined;
    });

    it('should be set on a multi-select combo box', () => {
      const comboBox: FlowMultiSelectComboBox = fixtureSync(
        '<vaadin-multi-select-combo-box></vaadin-multi-select-combo-box>'
      );
      init(comboBox);
      expect(comboBox._toggleSelectAllHandler).to.be.a('function');
    });
  });
});
