/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.core.plugins;

import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.exception.HopFileException;
import org.scannotation.AnnotationDB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JarFileCache {

  private static JarFileCache cache;

  private final Map<IPluginFolder, FileObject[]> folderMap;

  private final Map<FileObject, AnnotationDB> annotationMap;

  private JarFileCache() {
    annotationMap = new HashMap<>();
    folderMap = new HashMap<>();
  }

  public static JarFileCache getInstance() {
    if ( cache == null ) {
      cache = new JarFileCache();
    }
    return cache;
  }

  public AnnotationDB getAnnotationDB( FileObject fileObject ) throws IOException {
    AnnotationDB result = annotationMap.get( fileObject );
    if ( result == null ) {
      result = new AnnotationDB();
      result.scanArchives( fileObject.getURL() );
      annotationMap.put( fileObject, result );
    }
    return result;
  }

  public FileObject[] getFileObjects( IPluginFolder pluginFolderInterface ) throws HopFileException {
    FileObject[] result = folderMap.get( pluginFolderInterface );
    if ( result == null ) {
      result = pluginFolderInterface.findJarFiles();
      folderMap.put( pluginFolderInterface, result );
    }
    return result;
  }

  public void clear() {
    annotationMap.clear();
    folderMap.clear();
  }
}
