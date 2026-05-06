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
    it('should store data provider callbacks on the controllers pending requests', () => {
      const callback = sinon.spy();
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: '' }, callback);

      expect(comboBox.__dataProviderController.rootCache.pendingRequests[0]).to.equal(callback);
    });

    it('should clear pending requests on $connector.reset', () => {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: '' }, sinon.spy());
      expect(comboBox.__dataProviderController.rootCache.pendingRequests[0]).to.exist;

      comboBox.$connector.reset();

      expect(comboBox.__dataProviderController.rootCache.pendingRequests).to.deep.equal({});
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

      it('should track the post-debounce callback in the controllers pending requests', () => {
        const callback = sinon.spy();
        comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, callback);

        clock.tick(500);

        expect(comboBox.__dataProviderController.rootCache.pendingRequests[0]).to.equal(callback);
      });

      it('should clear stale pending requests when the filter changes', () => {
        const stale = sinon.spy();
        comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, stale);
        clock.tick(500);
        expect(comboBox.__dataProviderController.rootCache.pendingRequests[0]).to.equal(stale);

        const fresh = sinon.spy();
        comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'b' }, fresh);
        clock.tick(500);

        expect(stale).to.be.calledOnceWithExactly([], comboBox.size);
        expect(comboBox.__dataProviderController.rootCache.pendingRequests[0]).to.equal(fresh);
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
