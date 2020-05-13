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

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.i18n.BaseMessages;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * This class supports overriding of config builders by supplying a IVariables containing a variable in the format of
 * vfs.[scheme].config.parser where [scheme] is one of the VFS schemes (file, http, sftp, etc...)
 *
 * @author cboyden
 */
public class HopFileSystemConfigBuilderFactory {

  private static Class<?> PKG = HopVfs.class; // for i18n purposes, needed by Translator!!

  /**
   * This factory returns a FileSystemConfigBuilder. Custom FileSystemConfigBuilders can be created by implementing the
   * {@link IHopFileSystemConfigBuilder} or overriding the {@link HopGenericFileSystemConfigBuilder}
   *
   * @param varSpace A Hop variable space for resolving VFS config parameters
   * @param scheme   The VFS scheme (FILE, HTTP, SFTP, etc...)
   * @return A FileSystemConfigBuilder that can translate Hop variables into VFS config parameters
   * @throws IOException
   * @see org.apache.commons.vfs.FileSystemConfigBuilder
   */
  public static IHopFileSystemConfigBuilder getConfigBuilder( IVariables varSpace, String scheme ) throws IOException {
    IHopFileSystemConfigBuilder result = null;

    // Attempt to load the Config Builder from a variable: vfs.config.parser = class
    String parserClass = varSpace.getVariable( "vfs." + scheme + ".config.parser" );

    if ( parserClass != null ) {
      try {
        Class<?> configBuilderClass =
          HopFileSystemConfigBuilderFactory.class.getClassLoader().loadClass( parserClass );
        Method mGetInstance = configBuilderClass.getMethod( "getInstance" );
        if ( ( mGetInstance != null )
          && ( IHopFileSystemConfigBuilder.class.isAssignableFrom( mGetInstance.getReturnType() ) ) ) {
          result = (IHopFileSystemConfigBuilder) mGetInstance.invoke( null );
        } else {
          result = (IHopFileSystemConfigBuilder) configBuilderClass.newInstance();
        }
      } catch ( Exception e ) {
        // Failed to load custom parser. Throw exception.
        throw new IOException( BaseMessages.getString( PKG, "CustomVfsSettingsParser.Log.FailedToLoad" ) );
      }
    } else {
      // No custom parser requested, load default
      if ( scheme.equalsIgnoreCase( "sftp" ) ) {
        result = HopSftpFileSystemConfigBuilder.getInstance();
      } else {
        result = HopGenericFileSystemConfigBuilder.getInstance();
      }
    }

    return result;
  }

}
