import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { sendKeys } from '@web/test-runner-commands';
import { comboBoxConnector, FlowComboBox, init } from './shared.ts';
import '@vaadin/combo-box';
import '@vaadin/multi-select-combo-box';
import * as sinon from 'sinon';

describe('combo-box connector', () => {
  let comboBox: FlowComboBox;

  beforeEach(() => {
    comboBox = fixtureSync('<vaadin-combo-box></vaadin-combo-box>');
    init(comboBox);
  });

  it('should not reinitialize the connector', () => {
    const connector = comboBox.$connector;
    comboBoxConnector.initLazy(comboBox);
    expect(comboBox.$connector).to.equal(connector);
  });

  describe('filter debouncing', () => {
    let clock: sinon.SinonFakeTimers;

    beforeEach(async () => {
      clock = sinon.useFakeTimers({
        toFake: ['setTimeout', 'clearTimeout']
      });
    });

    afterEach(() => {
      clock.restore();
    });

    it('should debounce filter requests with default timeout', () => {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, () => {});
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;

      comboBox.$server.setViewportRange.resetHistory();

      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'ab' }, () => {});
      clock.tick(250);
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'abc' }, () => {});
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should cancel filter request when the connector is reset', () => {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'test' }, () => {});
      expect(comboBox._filterDebouncer).to.exist;

      comboBox.$connector.reset();
      expect(comboBox._filterDebouncer).to.not.exist;

      clock.tick(600);

      expect(comboBox.$server.setViewportRange).to.not.be.called;
    });
  });

  describe('data communicator reset', () => {
    async function loadFirstPage(target: FlowComboBox) {
      // Load the first page, like the server responds to the request
      target.opened = true;
      await nextFrame();
      const items = Array.from({ length: target.pageSize }, (_, i) => ({ key: `${i}`, label: `Item ${i}` }));
      target.$connector.updateSize(200);
      target.$connector.set(0, items, '');
      target.$connector.confirm(1, '');
      target.opened = false;
      await nextFrame();
      target.$server.setViewportRange.resetHistory();
    }

    beforeEach(async () => {
      await loadFirstPage(comboBox);
    });

    it('should reset the data communicator when reopened after the web component cleared its cache', async () => {
      comboBox.clearCache();

      comboBox.opened = true;
      await nextFrame();

      sinon.assert.calledOnce(comboBox.$server.resetDataCommunicator);
    });

    it('should not reset the data communicator when loading another page', async () => {
      comboBox.inputElement.focus();
      comboBox.opened = true;
      await nextFrame();

      // Highlights the last item, which scrolls to the last page
      await sendKeys({ press: 'ArrowUp' });
      await nextFrame();

      sinon.assert.called(comboBox.$server.setViewportRange);
      sinon.assert.notCalled(comboBox.$server.resetDataCommunicator);
    });

    it('should not reset the data communicator when reopened after the connector is reset', async () => {
      comboBox.$connector.reset();

      comboBox.opened = true;
      await nextFrame();

      sinon.assert.notCalled(comboBox.$server.resetDataCommunicator);
    });

    it('should reset the data communicator when reopened after readonly is turned off on a multi-select combo box', async () => {
      // The multi-select combo box keeps its data in an internal combo box
      const multiSelect: FlowComboBox = fixtureSync('<vaadin-multi-select-combo-box></vaadin-multi-select-combo-box>');
      init(multiSelect);
      await loadFirstPage(multiSelect);

      multiSelect.readonly = true;
      await nextFrame();
      multiSelect.readonly = false;
      await nextFrame();

      multiSelect.opened = true;
      await nextFrame();

      sinon.assert.calledOnce(multiSelect.$server.resetDataCommunicator);
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

      it('should reset the data communicator when filtering', async () => {
        comboBox.opened = true;
        await nextFrame();

        comboBox.filter = 'a';
        clock.tick(500);

        sinon.assert.calledWith(comboBox.$server.setViewportRange, 0, comboBox.pageSize, 'a');
        sinon.assert.calledOnce(comboBox.$server.resetDataCommunicator);
      });

      it('should reset the data communicator when the filter of a multi-select combo box changes back', async () => {
        // The multi-select combo box clears the cache of its internal combo box
        // on filter changes, which the connector does not see
        const multiSelect: FlowComboBox = fixtureSync(
          '<vaadin-multi-select-combo-box></vaadin-multi-select-combo-box>'
        );
        init(multiSelect);
        multiSelect.opened = true;
        await nextFrame();
        multiSelect.$server.setViewportRange.resetHistory();

        multiSelect.filter = 'a';
        multiSelect.filter = '';
        clock.tick(500);

        sinon.assert.calledWith(multiSelect.$server.setViewportRange, 0, multiSelect.pageSize, '');
        sinon.assert.calledOnce(multiSelect.$server.resetDataCommunicator);
      });
    });
  });
});
