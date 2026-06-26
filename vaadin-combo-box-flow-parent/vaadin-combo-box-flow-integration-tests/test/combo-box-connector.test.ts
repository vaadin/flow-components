import { expect } from 'chai';
import { fixtureSync } from '@vaadin/testing-helpers';
import { sendKeys } from '@web/test-runner-commands';
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

  describe('initialization while opened', () => {
    // Flow defers the connector initialization for an initially hidden combo
    // box until it becomes visible. When the server makes it visible and opens
    // it in the same round-trip, initLazy runs while the combo box is already
    // opened. Assigning the data provider then synchronously triggers a
    // first-page load that calls back into the connector, so all $connector
    // functions must already be defined by that point (see issue #9622).
    beforeEach(() => {
      comboBox = fixtureSync('<vaadin-combo-box opened></vaadin-combo-box>');
      init(comboBox);
    });

    it('should request the first page of data', () => {
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should load items into the dropdown', () => {
      comboBox.$connector.set(0, [{ key: '1', label: 'one' }], '');
      comboBox.$connector.confirm(1, '');

      expect(comboBox.filteredItems).to.eql([{ key: '1', label: 'one' }]);
    });
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

    beforeEach(() => {
      clock = sinon.useFakeTimers({
        toFake: ['setTimeout', 'clearTimeout']
      });
      comboBox.inputElement.focus();
    });

    afterEach(() => {
      clock.restore();
    });

    it('should debounce filter requests with default timeout', async () => {
      await sendKeys({ type: 'a' });
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;

      comboBox.$server.setViewportRange.resetHistory();

      await sendKeys({ type: 'b' });
      clock.tick(250);
      await sendKeys({ type: 'c' });
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should debounce filter requests with custom timeout', async () => {
      comboBox._filterTimeout = 1000;

      await sendKeys({ type: 'a' });
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should cancel filter request when the connector is reset', async () => {
      await sendKeys({ type: 'test' });
      expect(comboBox.$server.setViewportRange).to.be.not.called;

      comboBox.$connector.reset();
      clock.tick(600);

      // Reset triggers a single fresh fetch; the cancelled debounced fetch
      // must not also fire.
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });
  });
});
