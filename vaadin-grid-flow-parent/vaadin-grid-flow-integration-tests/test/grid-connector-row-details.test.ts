import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { getBodyCellContent } from './helpers.js';
import { init } from './shared.js';
import type { FlowGrid, GridConnector } from './shared.js';

describe('grid connector - row details', () => {
  let grid: FlowGrid;
  let connector: GridConnector;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    connector = grid.$connector;

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
