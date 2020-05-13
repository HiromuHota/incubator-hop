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

package org.apache.hop.core.vfs.configuration;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.util.DelegatingFileSystemOptionsBuilder;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.vfs.HopVfs;

import java.io.IOException;

/**
 * A generic FileSystemConfigBuilder that inserts parameters and values as literally specified.
 * <p>
 * Note: ALL parameters are case sensitive! Please see individual FileSystemConfigBuilder for list of available
 * parameters. Please also see Hop FileSystemConfigBuilder overrides for additional parameters.
 *
 * @author cboyden
 * @see HopSftpFileSystemConfigBuilder
 */
public class HopGenericFileSystemConfigBuilder extends FileSystemConfigBuilder implements
  IHopFileSystemConfigBuilder {

  private static final HopGenericFileSystemConfigBuilder builder = new HopGenericFileSystemConfigBuilder();
  private static final ILogChannel log = new LogChannel( "cfgbuilder" );

  @Override
  public String parseParameterName( String parameter, String scheme ) {
    String result = null;

    // Frame the parameter name
    int begin = parameter.indexOf( ".", parameter.indexOf( "." ) + 1 ) + 1; // Get the index of the second "."
    // (vfs.scheme.parameter)
    int end = -1;

    end = parameter.indexOf( '.', begin );

    if ( end < 0 ) {
      end = parameter.length();
    }

    if ( end > begin ) {
      result = parameter.substring( begin, end );
    }

    return result;
  }

  public static HopGenericFileSystemConfigBuilder getInstance() {
    return builder;
  }

  /**
   * Extract the scheme from a Hop VFS configuration paramter (vfs.scheme.parameter)
   *
   * @param fullParameterName A VFS configuration parameter in the form of 'vfs.scheme.parameter'
   */
  public static String extractScheme( String fullParameterName ) throws IllegalArgumentException {
    String result = null;

    // Verify that this is a Hop VFS configuration parameter
    if ( ( fullParameterName != null )
      && ( fullParameterName.length() > 4 ) && ( fullParameterName.startsWith( "vfs." ) ) ) {
      int schemeEnd = fullParameterName.indexOf( ".", 4 );
      if ( schemeEnd > 4 ) {
        result = fullParameterName.substring( 4, schemeEnd );
      } else {
        throw new IllegalArgumentException( "The configuration parameter does not match a valid scheme: "
          + fullParameterName );
      }
    } else {
      throw new IllegalArgumentException( "The configuration parameter does not match a valid scheme: "
        + fullParameterName );
    }

    return result;
  }

  protected HopGenericFileSystemConfigBuilder() {
    super();
  }

  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    return FileSystem.class;
  }

  @Override
  public void setParameter( FileSystemOptions opts, String name, String value, String fullParameterName,
                            String vfsUrl ) throws IOException {
    // Use the DelgatingFileSystemOptionsBuilder to insert generic parameters
    // This must be done to assure the correct VFS FileSystem drivers will process the parameters
    String scheme = extractScheme( fullParameterName );
    try {
      DelegatingFileSystemOptionsBuilder delegateFSOptionsBuilder =
        new DelegatingFileSystemOptionsBuilder( HopVfs.getInstance().getFileSystemManager() );
      if ( scheme != null ) {
        delegateFSOptionsBuilder.setConfigString( opts, scheme, name, value );
      } else {
        log.logMinimal( "Warning: Cannot process VFS parameters if no scheme is specified: " + vfsUrl );
      }
    } catch ( FileSystemException e ) {
      if ( ( e.getCode() != null ) && ( e.getCode().equalsIgnoreCase( "vfs.provider/config-key-invalid.error" ) ) ) {
        // This key is not supported by the default scheme config builder. This may be a custom key of another config
        // builder
        log.logMinimal( "Warning: The configuration parameter ["
          + name + "] is not supported by the default configuration builder for scheme: " + scheme );
      } else {
        // An unexpected error has occurred loading in parameters
        throw new IOException( e.getLocalizedMessage(), e );
      }
    }
  }
}
