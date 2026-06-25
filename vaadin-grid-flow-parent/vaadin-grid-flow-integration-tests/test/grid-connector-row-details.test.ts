import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { getDetailsCell, getBodyCellContent, setRootItems } from './shared.js';
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

    grid.rowDetailsRenderer = (root) => {
      root.textContent = 'details';
    };

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
    await nextFrame();

    expect(getDetailsCell(grid, 0)!.hidden).to.be.false;
    expect(getDetailsCell(grid, 1)!.hidden).to.be.true;
  });

  it('should close details for items closed from data', async () => {
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', detailsOpened: true },
      { key: '1', name: 'bar' }
    ]);

    grid.$connector.set(0, [{ key: '0', name: 'foo' }]);
    expect(getDetailsCell(grid, 0)!.hidden).to.be.true;
    expect(getDetailsCell(grid, 1)!.hidden).to.be.true;
  });

  describe('updateFlatData', () => {
    it('should open details for updated items', async () => {
      grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
      expect(getDetailsCell(grid, 0)!.hidden).to.be.false;
    });

    it('should close details for updated items', () => {
      grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
      grid.$connector.updateFlatData([{ key: '0', name: 'foo' }]);
      expect(getDetailsCell(grid, 0)!.hidden).to.be.true;
    });
  });

  it('should set details hidden on another item click', () => {
    getBodyCellContent(grid, 0, 0)!.click();
    getBodyCellContent(grid, 1, 0)!.click();
    expect(grid.$server.setDetailsVisible).to.be.calledWith('1');
  });

  it('should set details hidden on selected item click', () => {
    grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
    getBodyCellContent(grid, 0, 0)!.click();
    expect(grid.$server.setDetailsVisible).to.be.calledWith(null);
  });

  it('should set details hidden on selected item click when deselect is disallowed', () => {
    grid.__deselectDisallowed = true;
    grid.$connector.updateFlatData([{ key: '0', name: 'foo', detailsOpened: true }]);
    getBodyCellContent(grid, 0, 0)!.click();
    expect(grid.$server.setDetailsVisible).to.be.calledWith(null);
  });

  it('should not set details visible on click when details on click is disallowed', () => {
    grid.__disallowDetailsOnClick = true;
    getBodyCellContent(grid, 0, 0)!.click();
    expect(grid.$server.setDetailsVisible).not.to.be.called;
  });

  it('should not set details visible for item selected from data', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
    expect(grid.$server.setDetailsVisible).not.to.be.calledWith('0');
  });
});
