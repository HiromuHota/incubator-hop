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

package org.apache.hop.pipeline.transform;

import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.plugins.BasePluginType;
import org.apache.hop.core.plugins.IPluginType;
import org.apache.hop.core.plugins.PluginAnnotationType;
import org.apache.hop.core.plugins.PluginMainClassType;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This class represents the row distribution plugin type.
 *
 * @author matt
 */
@PluginMainClassType( IRowDistribution.class )
@PluginAnnotationType( RowDistributionPlugin.class )
public class RowDistributionPluginType extends BasePluginType implements IPluginType {
  private static RowDistributionPluginType pluginType;

  private RowDistributionPluginType() {
    super( RowDistributionPlugin.class, "ROW_DISTRIBUTION", "Row Distribution" );
    populateFolders( "rowdistribution" );
  }

  public static RowDistributionPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new RowDistributionPluginType();
    }
    return pluginType;
  }

  /**
   * Scan & register internal row distribution plugins
   */
  protected void registerNatives() throws HopPluginException {
    // None at this moment
  }

  public String[] getNaturalCategoriesOrder() {
    return new String[ 0 ];
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).code();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).classLoaderGroup();
  }
}
