import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
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
    setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
    expect(grid.$server.setDetailsVisible).to.be.calledWith('0');
  });
});
