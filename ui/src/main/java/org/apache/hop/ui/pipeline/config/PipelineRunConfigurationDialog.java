/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.ui.pipeline.config;

import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.variables.VariableValueDescription;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.config.IPipelineEngineRunConfiguration;
import org.apache.hop.pipeline.config.PipelineRunConfiguration;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.engine.PipelineEnginePluginType;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiCompositeWidgets;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.metadata.IMetadataDialog;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.HopGuiEnvironment;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@GuiPlugin(
  description = "This dialog allows you to configure the various pipeline run configurations"
)
/**
 * The dialog for metadata object PipelineRunConfiguration
 * Don't move this class around as it's sync'ed with the PipelineRunConfiguration package to find the dialog.
 */
public class PipelineRunConfigurationDialog extends Dialog implements IMetadataDialog {

  private static final Class<?> PKG = PipelineRunConfigurationDialog.class; // Needed by Translator

  private Shell parent;
  private Shell shell;
  private IHopMetadataProvider metadataProvider;
  private PipelineRunConfiguration runConfiguration;
  private PipelineRunConfiguration workingConfiguration;

  private Text wName;
  private Text wDescription;
  private ComboVar wPluginType;

  private Composite wPluginSpecificComp;
  private GuiCompositeWidgets guiCompositeWidgets;

  private final PropsUi props;
  private int middle;
  private int margin;

  private String returnValue;

  private Map<String, IPipelineEngineRunConfiguration> metaMap;
  private TableView wVariables;

  /**
   * @param parent           The parent shell
   * @param metadataProvider metadataProvider
   * @param runConfiguration The object to edit
   */
  public PipelineRunConfigurationDialog( Shell parent, IHopMetadataProvider metadataProvider, PipelineRunConfiguration runConfiguration ) {
    super( parent, SWT.NONE );
    this.parent = parent;
    this.metadataProvider = metadataProvider;
    this.runConfiguration = runConfiguration;
    this.workingConfiguration = new PipelineRunConfiguration( runConfiguration );
    props = PropsUi.getInstance();
    metaMap = populateMetaMap();
    if ( workingConfiguration.getEngineRunConfiguration() != null ) {
      metaMap.put( workingConfiguration.getEngineRunConfiguration().getEnginePluginName(), workingConfiguration.getEngineRunConfiguration() );
    }
    returnValue = null;
  }

  private Map<String, IPipelineEngineRunConfiguration> populateMetaMap() {
    metaMap = new HashMap<>();
    List<IPlugin> plugins = PluginRegistry.getInstance().getPlugins( PipelineEnginePluginType.class );
    for ( IPlugin plugin : plugins ) {
      try {
        IPipelineEngine engine = PluginRegistry.getInstance().loadClass( plugin, IPipelineEngine.class );

        // Get the default run configuration for the engine.
        //
        IPipelineEngineRunConfiguration engineRunConfiguration = engine.createDefaultPipelineEngineRunConfiguration();
        engineRunConfiguration.setEnginePluginId( plugin.getIds()[ 0 ] );
        engineRunConfiguration.setEnginePluginName( plugin.getName() );

        metaMap.put( engineRunConfiguration.getEnginePluginName(), engineRunConfiguration );
      } catch ( Exception e ) {
        HopGui.getInstance().getLog().logError( "Error instantiating pipeline run configuration plugin", e );
      }
    }

    return metaMap;
  }

  public String open() {
    // Create a tabbed interface instead of the confusing left hand side options
    // This will make it more conforming the rest.
    //
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GuiResource.getInstance().getImageToolbarRun() );

    middle = props.getMiddlePct();
    margin = props.getMargin();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.Shell.title" ) );
    shell.setLayout( formLayout );

    // Add buttons at the bottom
    Button wOk = new Button( shell, SWT.PUSH );
    wOk.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wOk.addListener( SWT.Selection, this::ok );

    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, this::cancel );

    Button[] buttons = new Button[] { wOk, wCancel };
    BaseTransformDialog.positionBottomButtons( shell, buttons, margin, null );

    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder );
    wTabFolder.setSimple( false );

    CTabItem wMainTab = new CTabItem( wTabFolder, SWT.NONE );
    wMainTab.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.MainTab.TabTitle" ) );

    ScrolledComposite wMainSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wMainSComp.setLayout( new FillLayout() );

    Composite wMainComp = new Composite( wMainSComp, SWT.NONE );
    props.setLook( wMainComp );

    FormLayout mainLayout = new FormLayout();
    mainLayout.marginWidth = 3;
    mainLayout.marginHeight = 3;
    wMainComp.setLayout( mainLayout );


    // The generic widgets: name, description and pipeline engine type
    //
    // What's the name
    //
    Label wlName = new Label( wMainComp, SWT.RIGHT );
    props.setLook( wlName );
    wlName.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.label.name" ) );
    FormData fdlName = new FormData();
    fdlName.top = new FormAttachment( 0, 0 );
    fdlName.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlName.right = new FormAttachment( middle, 0 );
    wlName.setLayoutData( fdlName );
    wName = new Text( wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    FormData fdName = new FormData();
    fdName.top = new FormAttachment( wlName, 0, SWT.CENTER );
    fdName.left = new FormAttachment( middle, margin ); // To the right of the label
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );
    Control lastControl = wName;

    Label wlDescription = new Label( wMainComp, SWT.RIGHT );
    props.setLook( wlDescription );
    wlDescription.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.label.Description" ) );
    FormData fdlDescription = new FormData();
    fdlDescription.top = new FormAttachment( lastControl, margin );
    fdlDescription.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlDescription.right = new FormAttachment( middle, 0 );
    wlDescription.setLayoutData( fdlDescription );
    wDescription = new Text( wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDescription );
    FormData fdDescription = new FormData();
    fdDescription.top = new FormAttachment( wlDescription, 0, SWT.CENTER );
    fdDescription.left = new FormAttachment( middle, margin ); // To the right of the label
    fdDescription.right = new FormAttachment( 100, 0 );
    wDescription.setLayoutData( fdDescription );
    lastControl = wDescription;

    // What's the type of engine?
    //
    Label wlPluginType = new Label( wMainComp, SWT.RIGHT );
    props.setLook( wlPluginType );
    wlPluginType.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.label.EngineType" ) );
    FormData fdlPluginType = new FormData();
    fdlPluginType.top = new FormAttachment( lastControl, margin );
    fdlPluginType.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPluginType.right = new FormAttachment( middle, 0 );
    wlPluginType.setLayoutData( fdlPluginType );
    wPluginType = new ComboVar( runConfiguration, wMainComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPluginType );
    wPluginType.setItems( getPluginTypes() );
    FormData fdPluginType = new FormData();
    fdPluginType.top = new FormAttachment( wlPluginType, 0, SWT.CENTER );
    fdPluginType.left = new FormAttachment( middle, margin ); // To the right of the label
    fdPluginType.right = new FormAttachment( 100, 0 );
    wPluginType.setLayoutData( fdPluginType );
    lastControl = wPluginType;


    // Add a composite area
    //
    wPluginSpecificComp = new Composite( wMainComp, SWT.BACKGROUND );
    props.setLook( wPluginSpecificComp );
    wPluginSpecificComp.setLayout( new FormLayout() );
    FormData fdPluginSpecificComp = new FormData();
    fdPluginSpecificComp.left = new FormAttachment( 0, 0 );
    fdPluginSpecificComp.right = new FormAttachment( 100, 0 );
    fdPluginSpecificComp.top = new FormAttachment( lastControl, 3 * margin );
    fdPluginSpecificComp.bottom = new FormAttachment( 100, 0 );
    wPluginSpecificComp.setLayoutData( fdPluginSpecificComp );

    // Now add the run configuration plugin specific widgets
    //
    guiCompositeWidgets = new GuiCompositeWidgets( runConfiguration, 25 ); // max 8 lines

    // Add the plugin specific widgets
    //
    addGuiCompositeWidgets();

    FormData fdMainComp = new FormData();
    fdMainComp.left = new FormAttachment( 0, 0 );
    fdMainComp.top = new FormAttachment( 0, 0 );
    fdMainComp.right = new FormAttachment( 100, 0 );
    fdMainComp.bottom = new FormAttachment( 100, 0 );
    wMainComp.setLayoutData( fdMainComp );

    wMainComp.pack();
    Rectangle mainBounds = wMainComp.getBounds();

    wMainSComp.setContent( wMainComp );
    wMainSComp.setExpandHorizontal( true );
    wMainSComp.setExpandVertical( true );
    wMainSComp.setMinWidth( mainBounds.width );
    wMainSComp.setMinHeight( mainBounds.height );

    wMainTab.setControl( wMainSComp );


    // Add the variables tab
    //
    CTabItem wVariablesTab = new CTabItem( wTabFolder, SWT.NONE );
    wVariablesTab.setText( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.VariablesTab.TabTitle" ) );

    ScrolledComposite wVariablesSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wVariablesSComp.setLayout( new FillLayout() );

    Composite wVariablesComp = new Composite( wVariablesSComp, SWT.NONE );
    props.setLook( wVariablesComp );

    FormLayout variablesLayout = new FormLayout();
    variablesLayout.marginWidth = 3;
    variablesLayout.marginHeight = 3;
    wVariablesComp.setLayout( variablesLayout );


    ColumnInfo[] columns = {
      new ColumnInfo( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.Variables.Column.Name" ), ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.Variables.Column.Value" ), ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( BaseMessages.getString( PKG, "PipelineRunConfigurationDialog.Variables.Column.Description" ), ColumnInfo.COLUMN_TYPE_TEXT ),
    };

    wVariables = new TableView( workingConfiguration, wVariablesComp, SWT.NONE, columns, workingConfiguration.getConfigurationVariables().size(), null, props );
    props.setLook( wVariables );
    FormData fdVariables = new FormData();
    fdVariables.top = new FormAttachment( 0, 0 );
    fdVariables.left = new FormAttachment( 0, 0 );
    fdVariables.bottom = new FormAttachment( 100, 0 );
    fdVariables.right = new FormAttachment( 100, 0 );
    wVariables.setLayoutData( fdVariables );

    FormData fdVariablesComp = new FormData();
    fdVariablesComp.left = new FormAttachment( 0, 0 );
    fdVariablesComp.top = new FormAttachment( 0, 0 );
    fdVariablesComp.right = new FormAttachment( 100, 0 );
    fdVariablesComp.bottom = new FormAttachment( 100, 0 );
    wVariablesComp.setLayoutData( fdVariablesComp );

    wVariablesComp.pack();
    Rectangle variablesBound = wVariablesComp.getBounds();

    wVariablesSComp.setContent( wVariablesComp );
    wVariablesSComp.setExpandHorizontal( true );
    wVariablesSComp.setExpandVertical( true );
    wVariablesSComp.setMinWidth( variablesBound.width );
    wVariablesSComp.setMinHeight( variablesBound.height );

    wVariablesTab.setControl( wVariablesSComp );


    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( 0, 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( wOk, -margin * 2 );
    wTabFolder.setLayoutData( fdTabFolder );


    getData();

    wTabFolder.setSelection( 0 );

    // Add listeners...
    //
    wPluginType.addModifyListener( e -> changeConnectionType() );

    wName.addListener( SWT.DefaultSelection, this::ok );
    wDescription.addListener( SWT.DefaultSelection, this::ok );

    BaseTransformDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return returnValue;
  }

  private Listener okListener = this::ok;

  private void addGuiCompositeWidgets() {

    // Remove existing children
    //
    for ( Control child : wPluginSpecificComp.getChildren() ) {
      child.removeListener( SWT.DefaultSelection, okListener );
      child.dispose();
    }

    if ( workingConfiguration.getEngineRunConfiguration() != null ) {
      guiCompositeWidgets = new GuiCompositeWidgets( runConfiguration, 25 );
      guiCompositeWidgets.createCompositeWidgets( workingConfiguration.getEngineRunConfiguration(), null, wPluginSpecificComp, PipelineRunConfiguration.GUI_PLUGIN_ELEMENT_PARENT_ID, null );
      for ( Control control : guiCompositeWidgets.getWidgetsMap().values() ) {
        control.addListener( SWT.DefaultSelection, okListener );
      }
    }
  }

  private AtomicBoolean busyChangingPluginType = new AtomicBoolean( false );

  private void changeConnectionType() {

    if ( busyChangingPluginType.get() ) {
      return;
    }
    busyChangingPluginType.set( true );

    // Capture any information on the widgets
    //
    getInfo( workingConfiguration );

    // Save the state of this type so we can switch back and forth
    if ( workingConfiguration.getEngineRunConfiguration() != null ) {
      metaMap.put( workingConfiguration.getEngineRunConfiguration().getEnginePluginName(), workingConfiguration.getEngineRunConfiguration() );
    }

    changeWorkingEngineConfiguration( workingConfiguration );

    // Add the plugin widgets
    //
    addGuiCompositeWidgets();

    // Put the data back
    //
    getData();

    shell.layout( true, true );

    busyChangingPluginType.set( false );
  }


  private void ok( Event event ) {
    changeWorkingEngineConfiguration( runConfiguration );
    getInfo( runConfiguration );
    returnValue = runConfiguration.getName();
    dispose();
  }

  private void cancel( Event event ) {
    dispose();
  }

  private void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy data from the metadata into the dialog.
   */
  private void getData() {

    wName.setText( Const.NVL( workingConfiguration.getName(), "" ) );
    wDescription.setText( Const.NVL( workingConfiguration.getDescription(), "" ) );
    if ( workingConfiguration.getEngineRunConfiguration() != null ) {
      wPluginType.setText( Const.NVL( workingConfiguration.getEngineRunConfiguration().getEnginePluginName(), "" ) );
      guiCompositeWidgets.setWidgetsContents( workingConfiguration.getEngineRunConfiguration(), wPluginSpecificComp,
        PipelineRunConfiguration.GUI_PLUGIN_ELEMENT_PARENT_ID );
    } else {
      wPluginType.setText( "" );
    }

    for ( int i = 0; i < workingConfiguration.getConfigurationVariables().size(); i++ ) {
      VariableValueDescription vvd = workingConfiguration.getConfigurationVariables().get( i );
      TableItem item = wVariables.table.getItem( i );
      int col = 1;
      item.setText( col++, Const.NVL( vvd.getName(), "" ) );
      item.setText( col++, Const.NVL( vvd.getValue(), "" ) );
      item.setText( col++, Const.NVL( vvd.getDescription(), "" ) );
    }
    wVariables.setRowNums();
    wVariables.optWidth( true );
  }

  private PipelineRunConfiguration getInfo( PipelineRunConfiguration meta ) {

    meta.setName( wName.getText() );
    meta.setDescription( wDescription.getText() );

    // Get the plugin specific information from the widgets on the screen
    //
    if ( meta.getEngineRunConfiguration() != null && guiCompositeWidgets != null && !guiCompositeWidgets.getWidgetsMap().isEmpty() ) {
      guiCompositeWidgets.getWidgetsContents( meta.getEngineRunConfiguration(), PipelineRunConfiguration.GUI_PLUGIN_ELEMENT_PARENT_ID );
    }

    // The variables
    //
    meta.getConfigurationVariables().clear();
    for ( int i = 0; i < wVariables.nrNonEmpty(); i++ ) {
      TableItem item = wVariables.getNonEmpty( i );
      String name = item.getText( 1 );
      String value = item.getText( 2 );
      String description = item.getText( 3 );
      meta.getConfigurationVariables().add( new VariableValueDescription( name, value, description ) );
    }

    return meta;
  }

  private void changeWorkingEngineConfiguration( PipelineRunConfiguration meta ) {
    String pluginName = wPluginType.getText();
    IPipelineEngineRunConfiguration engineRunConfiguration = metaMap.get( pluginName );
    if ( engineRunConfiguration != null ) {
      // Switch to the right plugin type
      //
      meta.setEngineRunConfiguration( engineRunConfiguration );
    } else {
      meta.setEngineRunConfiguration( null );
    }
  }

  private String[] getPluginTypes() {
    PluginRegistry registry = PluginRegistry.getInstance();
    List<IPlugin> plugins = registry.getPlugins( PipelineEnginePluginType.class );
    String[] types = new String[ plugins.size() ];
    for ( int i = 0; i < types.length; i++ ) {
      types[ i ] = plugins.get( i ).getName();
    }
    Arrays.sort( types, String.CASE_INSENSITIVE_ORDER );
    return types;
  }

  public static void main( String[] args ) throws HopException {
    Display display = new Display();
    Shell shell = new Shell( display, SWT.MIN | SWT.MAX | SWT.RESIZE );

    HopClientEnvironment.init();
    HopEnvironment.init();
    HopGuiEnvironment.init();
    // LocalWorkflowRunConfiguration localConfig = new LocalWorkflonConfiguration( "Local", "Local pipeline engine", "5000" );
    PipelineRunConfiguration configuration = new PipelineRunConfiguration( "test", "A test run config", new ArrayList<>(), null );
    PipelineRunConfigurationDialog dialog = new PipelineRunConfigurationDialog( shell, null, configuration );
    String name = dialog.open();
    if ( name != null ) {
      // Re-open with a new dialog...
      //
      PipelineRunConfigurationDialog newDialog = new PipelineRunConfigurationDialog( shell, null, configuration );
      newDialog.open();
    }

    display.dispose();
  }
}
