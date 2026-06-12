import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { getBodyCellContent, setRootItems } from './shared.js';
import { init } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - row details', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  it('should not inform server about details visibility on attach', async () => {
    // Initialize the connector for a detached grid and attach it afterwards,
    // like Flow does
    const detachedGrid = document.createElement('vaadin-grid') as FlowGrid;
    init(detachedGrid);

    grid.parentElement!.appendChild(detachedGrid);
    await nextFrame();

    expect(detachedGrid.$server.setDetailsVisible.called).to.be.false;
    detachedGrid.remove();
  });

  it('should set details visible on click', () => {
    getBodyCellContent(grid, 0, 0)!.click();
    expect(grid.$server.setDetailsVisible).to.be.calledWith('0');
  });

  it('should open details for items opened from data', async () => {
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', detailsOpened: true },
      { key: '1', name: 'bar' }
    ]);
    expect(grid.detailsOpenedItems).to.have.lengthOf(1);
    expect(grid.detailsOpenedItems[0].key).to.equal('0');
  });

  it('should close details for items closed from data', async () => {
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', detailsOpened: true },
      { key: '1', name: 'bar' }
    ]);

    grid.$connector.set(0, [{ key: '0', name: 'foo' }]);
    expect(grid.detailsOpenedItems).to.be.empty;
  });

  it('should close details for cleared items', async () => {
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', detailsOpened: true },
      { key: '1', name: 'bar' }
    ]);

    grid.$connector.clear(0, 2);
    expect(grid.detailsOpenedItems).to.be.empty;
  });

  describe('updateFlatData', () => {
    it('should open details for updated items', () => {
      grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
      expect(grid.detailsOpenedItems).to.have.lengthOf(1);
      expect(grid.detailsOpenedItems[0].key).to.equal('0');
    });

    it('should close details for updated items', () => {
      grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
      grid.$connector.updateFlatData([{ key: '0', name: 'foo' }]);
      expect(grid.detailsOpenedItems).to.be.empty;
    });
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
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
    expect(grid.$server.setDetailsVisible).to.be.calledWith('0');
  });
});
