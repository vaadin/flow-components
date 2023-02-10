import './env-setup.js';
import '../../vaadin-grid-flow/src/main/resources/META-INF/resources/frontend/gridConnector.js';
import '../../../node_modules/@vaadin/grid/vaadin-grid.js';
import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import type {} from '@web/test-runner-mocha';
import sinon from 'sinon';
import { getBodyCellContent, getBodyRowCount, getCellText } from './helpers.js';
import type { Grid } from '../../../node_modules/@vaadin/grid/vaadin-grid.js';

type Vaadin = {
  Flow: {
    gridConnector: any;
  };
};

type FlowGrid = Grid & {
  $connector: any;
  $server: any;
  __deselectDisallowed: boolean;
  __disallowDetailsOnClick: boolean;
};

const Vaadin = window.Vaadin as Vaadin;
const gridConnector = Vaadin.Flow.gridConnector;

describe('grid connector', () => {
  let grid: FlowGrid;
  let connector: typeof gridConnector;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    // Stub the necessary server-side methods
    grid.$server = {};
    grid.$server.confirmUpdate = sinon.stub();

    gridConnector.initLazy(grid);
    connector = grid.$connector;
  });

  it('should not reinitialize the connector', () => {
    gridConnector.initLazy(grid);
    expect(grid.$connector).to.equal(connector);
  });

  it('should add root level items', async () => {
    connector.updateSize(1);
    connector.set(0, [{ key: '0', name: 'foo' }], null);
    connector.confirm(0);
    await nextFrame();

    expect(getBodyRowCount(grid)).to.equal(1);
    expect(getCellText(grid, 0, 0)).to.equal('foo');
  });

  describe('selection', () => {
    beforeEach(async () => {
      grid.$server.select = sinon.spy();
      grid.$server.deselect = sinon.spy();
      grid.$server.setDetailsVisible = sinon.spy();

      connector.updateSize(2);
      connector.set(
        0,
        [
          { key: '0', name: 'foo' },
          { key: '1', name: 'bar' }
        ],
        null
      );
      connector.confirm(0);
      await nextFrame();
    });

    describe('single selection mode', () => {
      beforeEach(() => {
        grid.$connector.setSelectionMode('SINGLE');
      });

      it('should select item on click', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems.length).to.equal(1);
        expect(grid.selectedItems[0].key).to.equal('0');
      });

      it('should mark the item selected', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems[0].selected).to.be.true;
      });

      it('should deselect old selection on another item click', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        getBodyCellContent(grid, 1, 0)!.click();
        expect(grid.selectedItems.length).to.equal(1);
        expect(grid.selectedItems[0].key).to.equal('1');
      });

      it('should mark the item deselected', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        const item = grid.selectedItems[0];
        getBodyCellContent(grid, 0, 0)!.click();
        expect(item.selected).not.to.be.true;
      });

      it('should deselect on selected item click', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems).to.be.empty;
      });

      it('should not deselect on selected item click when deselect is disallowed', () => {
        grid.__deselectDisallowed = true;
        getBodyCellContent(grid, 0, 0)!.click();
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems.length).to.equal(1);
        expect(grid.selectedItems[0].key).to.equal('0');
      });

      it('should not select item on click when grid is disabled', () => {
        grid.disabled = true;
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems).to.be.empty;
      });

      it('should select on server', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.$server.select).to.be.calledWith('0');
      });

      it('should deselect on server', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.$server.deselect).to.be.calledWith('0');
      });

      it('should apply selection from data', async () => {
        connector.set(0, [{ key: '0', name: 'foo', selected: true }], null);
        expect(grid.selectedItems.length).to.equal(1);
        expect(grid.selectedItems[0].key).to.equal('0');
      });

      it('should apply deselection from data', async () => {
        getBodyCellContent(grid, 0, 0)!.click();
        connector.set(0, [{ key: '0', name: 'foo' }], null);
        expect(grid.selectedItems).to.be.empty;
      });
    });

    describe('none selection mode', () => {
      beforeEach(() => {
        grid.$connector.setSelectionMode('NONE');
      });

      it('should not select item on click', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems).to.be.empty;
      });

      it('should not apply selection from data', async () => {
        connector.set(0, [{ key: '0', name: 'foo', selected: true }], null);
        expect(grid.selectedItems).to.be.empty;
      });
    });
  });

  describe('row details', () => {
    beforeEach(async () => {
      grid.$server.select = sinon.spy();
      grid.$server.deselect = sinon.spy();
      grid.$server.setDetailsVisible = sinon.spy();

      connector.updateSize(2);
      connector.set(
        0,
        [
          { key: '0', name: 'foo' },
          { key: '1', name: 'bar' }
        ],
        null
      );
      connector.confirm(0);
      await nextFrame();
    });

    it('should set details visible on click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.setDetailsVisible).to.be.calledWith('0');
    });

    it('should set details hidden on another item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 1, 0)!.click();
      expect(grid.$server.setDetailsVisible).to.be.calledWith('1');
    });

    it('should set details hidden on selected item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.setDetailsVisible).to.be.calledWith(null);
    });

    it('should not set details hidden on selected item click when deselect is disallowed', () => {
      grid.__deselectDisallowed = true;
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.setDetailsVisible).not.to.be.calledWith(null);
    });

    it('should not set details visible on click when details on click is disallowed', () => {
      grid.__disallowDetailsOnClick = true;
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.setDetailsVisible).not.to.be.called;
    });

    it('should set details visible for item selected from data', async () => {
      connector.set(0, [{ key: '0', name: 'foo', selected: true }], null);
      expect(grid.$server.setDetailsVisible).to.be.calledWith('0');
    });
  });
});
