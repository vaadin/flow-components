import { expect, fixtureSync } from '@open-wc/testing';
import { comboBoxConnector, FlowComboBox, init } from './shared.ts';
import '@vaadin/combo-box';
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

  describe('pending requests', () => {
    let dataProviderController: FlowComboBox['__dataProviderController'];

    beforeEach(() => {
      dataProviderController = comboBox.__dataProviderController;
    });

    it('should be populated when the controller loads a page', () => {
      dataProviderController.loadFirstPage();

      expect(dataProviderController.rootCache.pendingRequests[0]).to.be.a('function');
    });

    it('should be cleared by $connector.confirm when items are cached', () => {
      dataProviderController.loadFirstPage();

      comboBox.$connector.set(0, [{ key: '1', label: 'one' }], '');
      comboBox.$connector.confirm(1, '');

      expect(dataProviderController.rootCache.pendingRequests[0]).to.be.undefined;
    });

    it('should be cleared by $connector.reset', () => {
      dataProviderController.loadFirstPage();
      expect(dataProviderController.rootCache.pendingRequests[0]).to.be.a('function');

      comboBox.$connector.reset();

      expect(dataProviderController.rootCache.pendingRequests).to.deep.equal({});
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

      it('should be populated only after the debounce timeout', () => {
        comboBox.filter = 'a';
        dataProviderController.loadFirstPage();

        const requestsDuringDebounce = dataProviderController.rootCache.pendingRequests;
        expect(Object.keys(requestsDuringDebounce)).to.have.lengthOf(1);

        clock.tick(500);

        expect(dataProviderController.rootCache.pendingRequests[0]).to.be.a('function');
      });

      it('should not be resolved by a stale-filter $connector.confirm after the filter changed', () => {
        // Filter "a" — controller schedules a fetch, debouncer fires, "a" is sent to the server.
        comboBox.filter = 'a';
        dataProviderController.loadFirstPage();
        clock.tick(500);

        // User types "b" while the "a" response is still in flight; the connector
        // synchronously moves on to "b".
        comboBox.filter = 'b';
        dataProviderController.loadFirstPage();

        // Late response for filter "a" arrives — must be dropped because the
        // connector has already advanced to "b".
        comboBox.$connector.set(0, [{ key: '1', label: 'a-one' }], 'a');
        comboBox.$connector.confirm(1, 'a');

        expect(dataProviderController.rootCache.pendingRequests[0]).to.be.a('function');
      });
    });
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

    it('should debounce filter requests with custom timeout', () => {
      comboBox._filterTimeout = 1000;

      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, () => {});
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
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
});
