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

package org.apache.hop.ui.workflow.dialog;

import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.plugins.BasePluginType;
import org.apache.hop.core.plugins.PluginAnnotationType;
import org.apache.hop.core.plugins.PluginMainClassType;
import org.apache.hop.core.plugins.IPluginType;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This plugin allows you to capture additional information concerning actions.
 *
 * @author matt
 */
@PluginMainClassType( IWorkflowDialogPlugin.class )
@PluginAnnotationType( WorkflowDialogPlugin.class )
public class WorkflowDialogPluginType extends BasePluginType implements IPluginType {
  // private static Class<?> PKG = WorkflowDialogPluginType.class; // for i18n purposes, needed by Translator!!

  private static WorkflowDialogPluginType pluginType;

  private WorkflowDialogPluginType() {
    super( WorkflowDialogPlugin.class, "JOBDIALOG", "Workflow dialog" );
    populateFolders( "jobdialog" );
  }

  public static WorkflowDialogPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new WorkflowDialogPluginType();
    }
    return pluginType;
  }

  /**
   * Scan & register internal transform plugins
   */
  protected void registerNatives() throws HopPluginException {
    // No native plugins
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (WorkflowDialogPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (WorkflowDialogPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (WorkflowDialogPlugin) annotation ).name();
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
    return ( (WorkflowDialogPlugin) annotation ).i18nPackageName();
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
    return ( (WorkflowDialogPlugin) annotation ).classLoaderGroup();
  }
}
