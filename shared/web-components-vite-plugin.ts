import path from 'path';
import { PluginOption } from 'vite';

/**
 * Vite plugin that resolves Vaadin component JS modules to a
 * local checkout of the web-components repository
 *
 * @param webComponentsRepoPath
 */
export function useLocalWebComponents(webComponentsRepoPath: string): PluginOption {
  const nodeModulesPath = path.resolve(__dirname, `${webComponentsRepoPath}/node_modules`);

  return {
    name: 'use-local-web-components',
    enforce: 'pre',
    config() {
      return {
        server: {
          fs: {
            allow: [nodeModulesPath]
          },
          watch: {
            ignored: [`!${nodeModulesPath}/**`]
          }
        }
      };
    },
    resolveId(id) {
      if (/^(@polymer|@vaadin)/.test(id)) {
        return this.resolve(path.join(nodeModulesPath, id));
      }
    }
  };
}
