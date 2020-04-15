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

package org.apache.hop.ui.pipeline.transforms.tableinput;

import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.PipelinePreviewFactory;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.errorhandling.IStream;
import org.apache.hop.pipeline.transforms.tableinput.TableInputMeta;
import org.apache.hop.ui.core.database.dialog.DatabaseExplorerDialog;
import org.apache.hop.ui.core.dialog.EnterNumberDialog;
import org.apache.hop.ui.core.dialog.EnterTextDialog;
import org.apache.hop.ui.core.dialog.PreviewRowsDialog;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.core.widget.StyledTextComp;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.file.workflow.HopGuiWorkflowGraph;
import org.apache.hop.ui.pipeline.dialog.PipelinePreviewProgressDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.List;

public class TableInputDialog extends BaseTransformDialog implements ITransformDialog {
  private static Class<?> PKG = TableInputMeta.class; // for i18n purposes, needed by Translator!!

  private MetaSelectionLine<DatabaseMeta> wConnection;

  private Label wlSQL;
  private StyledTextComp wSql;
  private FormData fdlSQL, fdSQL;

  private Label wlDatefrom;
  private CCombo wDatefrom;
  private FormData fdlDatefrom, fdDatefrom;
  private Listener lsDatefrom;

  private Label wlLimit;
  private TextVar wLimit;
  private FormData fdlLimit, fdLimit;

  private Label wlEachRow;
  private Button wEachRow;
  private FormData fdlEachRow, fdEachRow;

  private Label wlVariables;
  private Button wVariables;
  private FormData fdlVariables, fdVariables;

  private Label wlLazyConversion;
  private Button wLazyConversion;
  private FormData fdlLazyConversion, fdLazyConversion;

  private Button wbTable;
  private FormData fdbTable;
  private Listener lsbTable;

  private TableInputMeta input;
  private boolean changedInDialog;

  private Label wlPosition;
  private FormData fdlPosition;

  public TableInputDialog( Shell parent, Object in, PipelineMeta pipelineMeta, String sname ) {
    super( parent, (BaseTransformMeta) in, pipelineMeta, sname );
    input = (TableInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        changedInDialog = false; // for prompting if dialog is simply closed
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "TableInputDialog.TableInput" ) );

    int middle = props.getMiddlePct();
    int margin = props.getMargin();

    // TransformName line
    wlTransformName = new Label( shell, SWT.RIGHT );
    wlTransformName.setText( BaseMessages.getString( PKG, "TableInputDialog.TransformName" ) );
    props.setLook( wlTransformName );
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment( 0, 0 );
    fdlTransformName.right = new FormAttachment( middle, -margin );
    fdlTransformName.top = new FormAttachment( 0, margin );
    wlTransformName.setLayoutData( fdlTransformName );
    wTransformName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTransformName.setText( transformName );
    props.setLook( wTransformName );
    wTransformName.addModifyListener( lsMod );
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment( middle, 0 );
    fdTransformName.top = new FormAttachment( 0, margin );
    fdTransformName.right = new FormAttachment( 100, 0 );
    wTransformName.setLayoutData( fdTransformName );

    // Connection line
    wConnection = addConnectionLine( shell, wTransformName, input.getDatabaseMeta(), lsMod );

    // Some buttons
    wOk = new Button( shell, SWT.PUSH );
    wOk.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    // wHelp = createHelpButton(shell, transformMeta);

    setButtonPositions( new Button[] { wOk, wPreview, wCancel }, margin, null );

    // Limit input ...
    wlLimit = new Label( shell, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "TableInputDialog.LimitSize" ) );
    props.setLook( wlLimit );
    fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.right = new FormAttachment( middle, -margin );
    fdlLimit.bottom = new FormAttachment( wOk, -2 * margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new TextVar( pipelineMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.right = new FormAttachment( 100, 0 );
    fdLimit.bottom = new FormAttachment( wOk, -2 * margin );
    wLimit.setLayoutData( fdLimit );

    // Execute for each row?
    wlEachRow = new Label( shell, SWT.RIGHT );
    wlEachRow.setText( BaseMessages.getString( PKG, "TableInputDialog.ExecuteForEachRow" ) );
    props.setLook( wlEachRow );
    fdlEachRow = new FormData();
    fdlEachRow.left = new FormAttachment( 0, 0 );
    fdlEachRow.right = new FormAttachment( middle, -margin );
    fdlEachRow.bottom = new FormAttachment( wLimit, -margin );
    wlEachRow.setLayoutData( fdlEachRow );
    wEachRow = new Button( shell, SWT.CHECK );
    props.setLook( wEachRow );
    fdEachRow = new FormData();
    fdEachRow.left = new FormAttachment( middle, 0 );
    fdEachRow.right = new FormAttachment( 100, 0 );
    fdEachRow.bottom = new FormAttachment( wLimit, -margin );
    wEachRow.setLayoutData( fdEachRow );
    SelectionAdapter lsSelMod = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    };
    wEachRow.addSelectionListener( lsSelMod );

    // Read date from...
    wlDatefrom = new Label( shell, SWT.RIGHT );
    wlDatefrom.setText( BaseMessages.getString( PKG, "TableInputDialog.InsertDataFromTransform" ) );
    props.setLook( wlDatefrom );
    fdlDatefrom = new FormData();
    fdlDatefrom.left = new FormAttachment( 0, 0 );
    fdlDatefrom.right = new FormAttachment( middle, -margin );
    fdlDatefrom.bottom = new FormAttachment( wEachRow, -margin );
    wlDatefrom.setLayoutData( fdlDatefrom );
    wDatefrom = new CCombo( shell, SWT.BORDER );
    props.setLook( wDatefrom );

    List<TransformMeta> previousTransforms = pipelineMeta.findPreviousTransforms( pipelineMeta.findTransform( transformName ) );
    for ( TransformMeta transformMeta : previousTransforms ) {
      wDatefrom.add( transformMeta.getName() );
    }

    wDatefrom.addModifyListener( lsMod );
    fdDatefrom = new FormData();
    fdDatefrom.left = new FormAttachment( middle, 0 );
    fdDatefrom.right = new FormAttachment( 100, 0 );
    fdDatefrom.bottom = new FormAttachment( wEachRow, -margin );
    wDatefrom.setLayoutData( fdDatefrom );

    // Replace variables in SQL?
    //
    wlVariables = new Label( shell, SWT.RIGHT );
    wlVariables.setText( BaseMessages.getString( PKG, "TableInputDialog.ReplaceVariables" ) );
    props.setLook( wlVariables );
    fdlVariables = new FormData();
    fdlVariables.left = new FormAttachment( 0, 0 );
    fdlVariables.right = new FormAttachment( middle, -margin );
    fdlVariables.bottom = new FormAttachment( wDatefrom, -margin );
    wlVariables.setLayoutData( fdlVariables );
    wVariables = new Button( shell, SWT.CHECK );
    props.setLook( wVariables );
    fdVariables = new FormData();
    fdVariables.left = new FormAttachment( middle, 0 );
    fdVariables.right = new FormAttachment( 100, 0 );
    fdVariables.bottom = new FormAttachment( wDatefrom, -margin );
    wVariables.setLayoutData( fdVariables );
    wVariables.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        setSqlToolTip();
      }
    } );

    // Lazy conversion?
    //
    wlLazyConversion = new Label( shell, SWT.RIGHT );
    wlLazyConversion.setText( BaseMessages.getString( PKG, "TableInputDialog.LazyConversion" ) );
    props.setLook( wlLazyConversion );
    fdlLazyConversion = new FormData();
    fdlLazyConversion.left = new FormAttachment( 0, 0 );
    fdlLazyConversion.right = new FormAttachment( middle, -margin );
    fdlLazyConversion.bottom = new FormAttachment( wVariables, -margin );
    wlLazyConversion.setLayoutData( fdlLazyConversion );
    wLazyConversion = new Button( shell, SWT.CHECK );
    props.setLook( wLazyConversion );
    fdLazyConversion = new FormData();
    fdLazyConversion.left = new FormAttachment( middle, 0 );
    fdLazyConversion.right = new FormAttachment( 100, 0 );
    fdLazyConversion.bottom = new FormAttachment( wVariables, -margin );
    wLazyConversion.setLayoutData( fdLazyConversion );
    wLazyConversion.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        setSqlToolTip();
      }
    } );

    wlPosition = new Label( shell, SWT.NONE );
    props.setLook( wlPosition );
    fdlPosition = new FormData();
    fdlPosition.left = new FormAttachment( 0, 0 );
    fdlPosition.right = new FormAttachment( 100, 0 );
    fdlPosition.bottom = new FormAttachment( wLazyConversion, -margin );
    wlPosition.setLayoutData( fdlPosition );

    // Table line...
    wlSQL = new Label( shell, SWT.NONE );
    wlSQL.setText( BaseMessages.getString( PKG, "TableInputDialog.SQL" ) );
    props.setLook( wlSQL );
    fdlSQL = new FormData();
    fdlSQL.left = new FormAttachment( 0, 0 );
    fdlSQL.top = new FormAttachment( wConnection, margin * 2 );
    wlSQL.setLayoutData( fdlSQL );

    wbTable = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTable );
    wbTable.setText( BaseMessages.getString( PKG, "TableInputDialog.GetSQLAndSelectStatement" ) );
    fdbTable = new FormData();
    fdbTable.right = new FormAttachment( 100, 0 );
    fdbTable.top = new FormAttachment( wConnection, margin * 2 );
    wbTable.setLayoutData( fdbTable );

    wSql =
      new StyledTextComp( pipelineMeta, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    props.setLook( wSql, Props.WIDGET_STYLE_FIXED );
    wSql.addModifyListener( lsMod );
    fdSQL = new FormData();
    fdSQL.left = new FormAttachment( 0, 0 );
    fdSQL.top = new FormAttachment( wbTable, margin );
    fdSQL.right = new FormAttachment( 100, -2 * margin );
    fdSQL.bottom = new FormAttachment( wlPosition, -margin );
    wSql.setLayoutData( fdSQL );
    wSql.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setSqlToolTip();
        setPosition();
      }
    } );

    wSql.addKeyListener( new KeyAdapter() {
      public void keyPressed( KeyEvent e ) {
        setPosition();
      }

      public void keyReleased( KeyEvent e ) {
        setPosition();
      }
    } );
    wSql.addFocusListener( new FocusAdapter() {
      public void focusGained( FocusEvent e ) {
        setPosition();
      }

      public void focusLost( FocusEvent e ) {
        setPosition();
      }
    } );
    wSql.addMouseListener( new MouseAdapter() {
      public void mouseDoubleClick( MouseEvent e ) {
        setPosition();
      }

      public void mouseDown( MouseEvent e ) {
        setPosition();
      }

      public void mouseUp( MouseEvent e ) {
        setPosition();
      }
    } );

    // Text Higlighting
    wSql.addLineStyleListener( new SqlValuesHighlight() );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsOk = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsbTable = new Listener() {
      public void handleEvent( Event e ) {
        getSql();
      }
    };
    lsDatefrom = new Listener() {
      public void handleEvent( Event e ) {
        setFlags();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wPreview.addListener( SWT.Selection, lsPreview );
    wOk.addListener( SWT.Selection, lsOk );
    wbTable.addListener( SWT.Selection, lsbTable );
    wDatefrom.addListener( SWT.Selection, lsDatefrom );
    wDatefrom.addListener( SWT.FocusOut, lsDatefrom );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wTransformName.addSelectionListener( lsDef );
    wLimit.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        checkCancel( e );
      }
    } );

    getData();
    changedInDialog = false; // for prompting if dialog is simply closed
    input.setChanged( changed );

    // Set the shell size, based upon previous time...
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return transformName;
  }

  public void setPosition() {

    String scr = wSql.getText();
    int linenr = wSql.getLineAtOffset( wSql.getCaretOffset() ) + 1;
    int posnr = wSql.getCaretOffset();

    // Go back from position to last CR: how many positions?
    int colnr = 0;
    while ( posnr > 0 && scr.charAt( posnr - 1 ) != '\n' && scr.charAt( posnr - 1 ) != '\r' ) {
      posnr--;
      colnr++;
    }
    wlPosition.setText( BaseMessages.getString( PKG, "TableInputDialog.Position.Label", "" + linenr, "" + colnr ) );

  }

  protected void setSqlToolTip() {
    if ( wVariables.getSelection() ) {
      wSql.setToolTipText( pipelineMeta.environmentSubstitute( wSql.getText() ) );
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getSql() != null ) {
      wSql.setText( input.getSql() );
    }
    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    }
    wLimit.setText( Const.NVL( input.getRowLimit(), "" ) );

    IStream infoStream = input.getTransformIOMeta().getInfoStreams().get( 0 );
    if ( infoStream.getTransformMeta() != null ) {
      wDatefrom.setText( infoStream.getTransformName() );
      wEachRow.setSelection( input.isExecuteEachInputRow() );
    } else {
      wEachRow.setEnabled( false );
      wlEachRow.setEnabled( false );
    }

    wVariables.setSelection( input.isVariableReplacementActive() );
    wLazyConversion.setSelection( input.isLazyConversionActive() );

    setSqlToolTip();
    setFlags();

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void checkCancel( ShellEvent e ) {
    if ( changedInDialog ) {
      int save = HopGuiWorkflowGraph.showChangedWarning( shell, wTransformName.getText() );
      if ( save == SWT.CANCEL ) {
        e.doit = false;
      } else if ( save == SWT.YES ) {
        ok();
      } else {
        cancel();
      }
    } else {
      cancel();
    }
  }

  private void cancel() {
    transformName = null;
    input.setChanged( changed );
    dispose();
  }

  private void getInfo( TableInputMeta meta, boolean preview ) {
    meta.setSql( preview && !Utils.isEmpty( wSql.getSelectionText() ) ? wSql.getSelectionText() : wSql.getText() );
    meta.setDatabaseMeta( pipelineMeta.findDatabase( wConnection.getText() ) );
    meta.setRowLimit( wLimit.getText() );
    IStream infoStream = input.getTransformIOMeta().getInfoStreams().get( 0 );
    infoStream.setTransformMeta( pipelineMeta.findTransform( wDatefrom.getText() ) );
    meta.setExecuteEachInputRow( wEachRow.getSelection() );
    meta.setVariableReplacementActive( wVariables.getSelection() );
    meta.setLazyConversionActive( wLazyConversion.getSelection() );
  }

  private void ok() {
    if ( Utils.isEmpty( wTransformName.getText() ) ) {
      return;
    }

    transformName = wTransformName.getText(); // return value
    // copy info to TextFileInputMeta class (input)

    getInfo( input, false );

    if ( input.getDatabaseMeta() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TableInputDialog.SelectValidConnection" ) );
      mb.setText( BaseMessages.getString( PKG, "TableInputDialog.DialogCaptionError" ) );
      mb.open();
      return;
    }

    dispose();
  }

  private void getSql() {
    DatabaseMeta inf = pipelineMeta.findDatabase( wConnection.getText() );
    if ( inf != null ) {
      DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, pipelineMeta.getDatabases() );
      if ( std.open() ) {
        String sql =
          "SELECT *"
            + Const.CR + "FROM "
            + inf.getQuotedSchemaTableCombination( std.getSchemaName(), std.getTableName() ) + Const.CR;
        wSql.setText( sql );

        MessageBox yn = new MessageBox( shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION );
        yn.setMessage( BaseMessages.getString( PKG, "TableInputDialog.IncludeFieldNamesInSQL" ) );
        yn.setText( BaseMessages.getString( PKG, "TableInputDialog.DialogCaptionQuestion" ) );
        int id = yn.open();
        switch ( id ) {
          case SWT.CANCEL:
            break;
          case SWT.NO:
            wSql.setText( sql );
            break;
          case SWT.YES:
            Database db = new Database( loggingObject, inf );
            db.shareVariablesWith( pipelineMeta );
            try {
              db.connect();
              IRowMeta fields = db.getQueryFields( sql, false );
              if ( fields != null ) {
                sql = "SELECT" + Const.CR;
                for ( int i = 0; i < fields.size(); i++ ) {
                  IValueMeta field = fields.getValueMeta( i );
                  if ( i == 0 ) {
                    sql += "  ";
                  } else {
                    sql += ", ";
                  }
                  sql += inf.quoteField( field.getName() ) + Const.CR;
                }
                sql +=
                  "FROM "
                    + inf.getQuotedSchemaTableCombination( std.getSchemaName(), std.getTableName() )
                    + Const.CR;
                wSql.setText( sql );
              } else {
                MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage( BaseMessages.getString( PKG, "TableInputDialog.ERROR_CouldNotRetrieveFields" )
                  + Const.CR + BaseMessages.getString( PKG, "TableInputDialog.PerhapsNoPermissions" ) );
                mb.setText( BaseMessages.getString( PKG, "TableInputDialog.DialogCaptionError2" ) );
                mb.open();
              }
            } catch ( HopException e ) {
              MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
              mb.setText( BaseMessages.getString( PKG, "TableInputDialog.DialogCaptionError3" ) );
              mb.setMessage( BaseMessages.getString( PKG, "TableInputDialog.AnErrorOccurred" )
                + Const.CR + e.getMessage() );
              mb.open();
            } finally {
              db.disconnect();
            }
            break;
          default:
            break;
        }
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TableInputDialog.ConnectionNoLongerAvailable" ) );
      mb.setText( BaseMessages.getString( PKG, "TableInputDialog.DialogCaptionError4" ) );
      mb.open();
    }

  }

  private void setFlags() {
    if ( !Utils.isEmpty( wDatefrom.getText() ) ) {
      // The foreach check box...
      wEachRow.setEnabled( true );
      wlEachRow.setEnabled( true );

      // The preview button...
      wPreview.setEnabled( false );
    } else {
      // The foreach check box...
      wEachRow.setEnabled( false );
      wEachRow.setSelection( false );
      wlEachRow.setEnabled( false );

      // The preview button...
      wPreview.setEnabled( true );
    }

  }

  /**
   * Preview the data generated by this transform. This generates a pipeline using this transform & a dummy and previews it.
   */
  private void preview() {
    // Create the table input reader transform...
    TableInputMeta oneMeta = new TableInputMeta();
    getInfo( oneMeta, true );

    PipelineMeta previewMeta = PipelinePreviewFactory.generatePreviewPipeline( pipelineMeta, oneMeta, wTransformName.getText() );

    EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
      BaseMessages.getString( PKG, "TableInputDialog.EnterPreviewSize" ),
      BaseMessages.getString( PKG, "TableInputDialog.NumberOfRowsToPreview" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      PipelinePreviewProgressDialog progressDialog =
        new PipelinePreviewProgressDialog(
          shell, previewMeta, new String[] { wTransformName.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Pipeline pipeline = progressDialog.getPipeline();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( pipeline.getResult() != null && pipeline.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog(
              shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
              .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        } else {
          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              shell, pipelineMeta, SWT.NONE, wTransformName.getText(), progressDialog.getPreviewRowsMeta( wTransformName
              .getText() ), progressDialog.getPreviewRows( wTransformName.getText() ), loggingText );
          prd.open();
        }
      }

    }
  }

}