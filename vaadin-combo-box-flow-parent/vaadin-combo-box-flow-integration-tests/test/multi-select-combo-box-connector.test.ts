import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import * as sinon from 'sinon';
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

  describe('readonly', () => {
    let comboBox: FlowMultiSelectComboBox;

    beforeEach(async () => {
      comboBox = fixtureSync('<vaadin-multi-select-combo-box></vaadin-multi-select-combo-box>');
      init(comboBox);

      // Load the first page, like the server responds to the request
      comboBox.opened = true;
      await nextFrame();
      const items = Array.from({ length: comboBox.pageSize }, (_, i) => ({ key: `${i}`, label: `Item ${i}` }));
      comboBox.$connector.updateSize(200);
      comboBox.$connector.set(0, items, '');
      comboBox.$connector.confirm(1, '');
      comboBox.opened = false;
      await nextFrame();
    });

    it('should reset the data communicator when reopened after readonly is turned off', async () => {
      comboBox.readonly = true;
      await nextFrame();
      comboBox.readonly = false;
      await nextFrame();

      comboBox.opened = true;
      await nextFrame();

      expect(comboBox.$server.resetDataCommunicator).to.be.calledOnce;
    });

    it('should not reset the data communicator when loading another page', async () => {
      comboBox.opened = true;
      await nextFrame();

      comboBox.__dataProviderController.ensureFlatIndexLoaded(comboBox.pageSize);

      expect(comboBox.$server.setViewportRange).to.be.called;
      expect(comboBox.$server.resetDataCommunicator).to.be.not.called;
    });

    describe('with filter debouncing', () => {
      let clock: sinon.SinonFakeTimers;

      beforeEach(() => {
        clock = sinon.useFakeTimers({
          toFake: ['setTimeout', 'clearTimeout']
        });
      });

      afterEach(() => {
        clock.restore();
      });

      it('should not reset the data communicator when filtering', async () => {
        comboBox.opened = true;
        await nextFrame();

        comboBox.filter = 'a';
        clock.tick(500);

        expect(comboBox.$server.setViewportRange).to.be.calledWith(0, comboBox.pageSize, 'a');
        expect(comboBox.$server.resetDataCommunicator).to.be.not.called;
      });
    });

    it('should not reset the data communicator when reopened after the connector is reset', async () => {
      comboBox.$connector.reset();

      comboBox.opened = true;
      await nextFrame();

      expect(comboBox.$server.resetDataCommunicator).to.be.not.called;
    });
  });
});
