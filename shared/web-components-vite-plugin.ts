import path from 'path';
import { PluginOption } from 'vite';

/**
 * Vite plugin that resolves Vaadin component JS modules to a
 * local checkout of the web-components repository
 * @param webComponentsNodeModulesPath
 */
export function useLocalWebComponents(webComponentsNodeModulesPath: string): PluginOption {
  return {
    name: 'use-local-web-components',
    enforce: 'pre',
    config(config) {
      config.server = config.server ?? {};
      config.server.fs = config.server.fs ?? {};
      config.server.fs.allow = config.server.fs.allow ?? [];
      config.server.fs.allow.push(webComponentsNodeModulesPath);
      config.server.watch = config.server.watch ?? {};
      config.server.watch.ignored = [`!${webComponentsNodeModulesPath}/**`];
    },
    resolveId(id) {
      if (/^(@polymer|@vaadin)/.test(id)) {
        return this.resolve(path.join(webComponentsNodeModulesPath, id));
      }
    },
  }
}
