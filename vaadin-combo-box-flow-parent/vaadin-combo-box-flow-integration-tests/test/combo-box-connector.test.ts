import { expect } from 'chai';
import { fixtureSync } from '@vaadin/testing-helpers';
import { sendKeys } from '@web/test-runner-commands';
import { comboBoxConnector, FlowComboBox, init, Item } from './shared.ts';
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

  it('should not throw when initialized while opened', () => {
    // When a combo box is made visible and opened in the same round-trip,
    // initLazy runs while it is already opened. Assigning the data provider
    // then triggers a first-page load that calls back into the connector,
    // before `$connector` has even been assigned on the combo box.
    comboBox = fixtureSync('<vaadin-combo-box opened></vaadin-combo-box>');
    expect(() => init(comboBox)).to.not.throw();
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

  describe('focusSelectedItem', () => {
    const APPLES: Item[] = Array.from({ length: 10 }, (_, i) => ({ key: `a${i}`, label: `Apple ${i}` }));
    const BANANAS: Item[] = Array.from({ length: 20 }, (_, i) => ({ key: `b${i}`, label: `Banana ${i}` }));
    const ITEMS: Item[] = [...APPLES, ...BANANAS];
    // "Banana 5" is at index 15 of all items, and at index 5 among the bananas
    const SELECTED_KEY = 'b5';
    const SELECTED_INDEX = 15;

    let focusIndex: sinon.SinonStub;

    beforeEach(() => {
      focusIndex = sinon.stub(comboBox, '__focusIndex');
    });

    // Runs the round-trip that passes all items to the combo box, which then
    // shows them either as they are or filtered on the client
    function loadItems(): void {
      comboBox.$connector.updateSize(ITEMS.length);
      comboBox.__dataProviderController.loadFirstPage();
      comboBox.$connector.set(0, ITEMS, '');
      comboBox.$connector.confirm(1, '');
    }

    // The server only requests focusing the selected item while the dropdown is
    // open, which is also the only state a filter can be typed in
    async function open(): Promise<void> {
      comboBox.opened = true;
      await comboBox.updateComplete;
    }

    async function openAndFilter(filter: string): Promise<void> {
      comboBox.opened = true;
      comboBox.filter = filter;
      await comboBox.updateComplete;
    }

    // Runs the round-trip that passes the items matching a server-side filter,
    // skipping the debounce that the typed filter is otherwise delayed by
    function loadFilteredItems(filter: string): void {
      comboBox._filterDebouncer?.flush();
      comboBox.$connector.updateSize(BANANAS.length);
      comboBox.$connector.set(0, BANANAS, filter);
      comboBox.$connector.confirm(1, filter);
      // Guards the tests below against passing for lack of items to focus
      expect(comboBox._dropdownItems).to.have.lengthOf(BANANAS.length);
    }

    it('should focus the item at the index resolved by the server', async () => {
      loadItems();
      comboBox.value = SELECTED_KEY;
      await open();

      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      expect(focusIndex).to.be.calledOnceWith(SELECTED_INDEX);
    });

    it('should focus the selected item at its index among client-side filtered items', async () => {
      comboBox._clientSideFilter = true;
      loadItems();
      comboBox.value = SELECTED_KEY;
      await openAndFilter('Banana');

      // The server resolved the index against all items, while the dropdown
      // only shows the bananas
      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      expect(focusIndex).to.be.calledOnceWith(5);
    });

    it('should not focus any item when the selected item does not match the client-side filter', async () => {
      comboBox._clientSideFilter = true;
      loadItems();
      comboBox.value = SELECTED_KEY;
      await openAndFilter('Apple');

      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      expect(focusIndex).to.be.not.called;
    });

    it('should focus the index resolved by the server when the selected item is not loaded', async () => {
      // A selection outside the loaded pages, as with lazy loading: only the
      // server can tell where the item is
      comboBox.value = 'b99';
      await openAndFilter('Banana');
      loadFilteredItems('Banana');

      comboBox.$connector.focusSelectedItem(60, 'Banana');

      expect(focusIndex).to.be.calledOnceWith(60);
    });

    it('should not focus the index resolved by the server for another filter', async () => {
      comboBox.value = 'b99';
      await openAndFilter('Banana');
      loadFilteredItems('Banana');

      // The typed filter had not reached the server yet when it resolved the
      // index, so the index counts other items than the dropdown shows
      comboBox.$connector.focusSelectedItem(60, '');

      expect(focusIndex).to.be.not.called;
    });

    it('should focus the selected item once the filtered items arrive', async () => {
      comboBox._clientSideFilter = true;
      comboBox.value = SELECTED_KEY;
      await openAndFilter('Banana');

      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');
      expect(focusIndex).to.be.not.called;

      loadItems();

      expect(focusIndex).to.be.calledOnceWith(5);
    });

    it('should not focus any item once the connector has been reset', async () => {
      comboBox.value = SELECTED_KEY;
      await open();
      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      comboBox.$connector.reset();
      loadItems();

      expect(focusIndex).to.be.not.called;
    });

    it('should focus the selected item once the items arrive with a server-side filter', async () => {
      comboBox.value = SELECTED_KEY;
      await open();

      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');
      expect(focusIndex, 'no items to resolve the request against yet').to.be.not.called;

      loadItems();

      expect(focusIndex).to.be.calledOnceWith(SELECTED_INDEX);
    });

    it('should not focus any item once the dropdown has been closed', async () => {
      comboBox.value = SELECTED_KEY;
      await open();
      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      // The user closes the dropdown before any items arrive. Focusing an item
      // now would be committed as the value the next time it closes.
      comboBox.opened = false;
      await comboBox.updateComplete;
      loadItems();

      expect(focusIndex).to.be.not.called;
    });

    it('should not focus any item once the value has changed', async () => {
      comboBox.value = SELECTED_KEY;
      await open();
      comboBox.$connector.focusSelectedItem(SELECTED_INDEX, '');

      // The server clears the value while the request is still pending, and
      // sends no new request because there is no selected item to focus
      comboBox.value = '';
      await comboBox.updateComplete;
      loadItems();

      expect(focusIndex).to.be.not.called;
    });
  });
});
