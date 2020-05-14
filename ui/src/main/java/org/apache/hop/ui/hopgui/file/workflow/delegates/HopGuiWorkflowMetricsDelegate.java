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

package org.apache.hop.ui.hopgui.file.workflow.delegates;

import org.apache.hop.core.gui.Point;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LoggingRegistry;
import org.apache.hop.core.logging.MetricsPainter;
import org.apache.hop.core.logging.MetricsPainter.MetricsDrawArea;
import org.apache.hop.core.metrics.MetricsDuration;
import org.apache.hop.core.metrics.MetricsUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.file.workflow.HopGuiWorkflowGraph;
import org.apache.hop.ui.hopgui.shared.SwtGc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HopGuiWorkflowMetricsDelegate {
  private static Class<?> PKG = HopGui.class; // for i18n purposes, needed by Translator!!

  // private static final LogWriter log = LogWriter.getInstance();

  private HopGui hopGui;
  private HopGuiWorkflowGraph workflowGraph;

  private CTabItem jobMetricsTab;

  private Canvas canvas;
  private Image image;

  private ScrolledComposite sMetricsComposite;
  private Composite metricsComposite;
  private boolean emptyGraph;

  private List<MetricsDrawArea> drawAreas;

  /**
   * @param hopGui
   * @param workflowGraph
   */
  public HopGuiWorkflowMetricsDelegate( HopGui hopGui, HopGuiWorkflowGraph workflowGraph ) {
    this.hopGui = hopGui;
    this.workflowGraph = workflowGraph;
  }

  public void addJobMetrics() {
    // First, see if we need to add the extra view...
    //
    if ( workflowGraph.extraViewComposite == null || workflowGraph.extraViewComposite.isDisposed() ) {
      workflowGraph.addExtraView();
    } else {
      if ( jobMetricsTab != null && !jobMetricsTab.isDisposed() ) {
        // just set this one active and get out...
        //
        workflowGraph.extraViewTabFolder.setSelection( jobMetricsTab );
        return;
      }
    }

    // Add a pipelineMetricsTab : displays the metrics information in a graphical way...
    //
    jobMetricsTab = new CTabItem( workflowGraph.extraViewTabFolder, SWT.NONE );
    jobMetricsTab.setImage( GuiResource.getInstance().getImageGantt() );
    jobMetricsTab.setText( BaseMessages.getString( PKG, "HopGui.JobGraph.MetricsTab.Name" ) );

    sMetricsComposite = new ScrolledComposite( workflowGraph.extraViewTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    sMetricsComposite.setLayout( new FillLayout() );

    // Create a composite, slam everything on there like it was in the history tab.
    //
    metricsComposite = new Composite( sMetricsComposite, SWT.NONE );
    metricsComposite.setBackground( GuiResource.getInstance().getColorBackground() );
    metricsComposite.setLayout( new FormLayout() );

    hopGui.getProps().setLook( metricsComposite );

    setupContent();

    sMetricsComposite.setContent( metricsComposite );
    sMetricsComposite.setExpandHorizontal( true );
    sMetricsComposite.setExpandVertical( true );
    sMetricsComposite.setMinWidth( 800 );
    sMetricsComposite.setMinHeight( 500 );

    jobMetricsTab.setControl( sMetricsComposite );

    workflowGraph.extraViewTabFolder.setSelection( jobMetricsTab );

    workflowGraph.extraViewTabFolder.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent arg0 ) {
        layoutMetricsComposite();
        updateGraph();
      }
    } );
  }

  public void setupContent() {

    if ( metricsComposite.isDisposed() ) {
      return;
    }

    // Remove anything on the perf composite, like an empty page message
    //
    for ( Control control : metricsComposite.getChildren() ) {
      if ( !control.isDisposed() ) {
        control.dispose();
      }
    }

    emptyGraph = false;

    canvas = new Canvas( metricsComposite, SWT.NONE );
    hopGui.getProps().setLook( canvas );
    FormData fdCanvas = new FormData();
    fdCanvas.left = new FormAttachment( 0, 0 );
    fdCanvas.right = new FormAttachment( 100, 0 );
    fdCanvas.top = new FormAttachment( 0, 0 );
    fdCanvas.bottom = new FormAttachment( 100, 0 );
    canvas.setLayoutData( fdCanvas );

    metricsComposite.addControlListener( new ControlAdapter() {
      public void controlResized( ControlEvent event ) {
        updateGraph();
      }
    } );

    metricsComposite.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        if ( image != null ) {
          image.dispose();
        }
      }
    } );

    canvas.addPaintListener( new PaintListener() {

      public void paintControl( PaintEvent event ) {

        if ( workflowGraph.getWorkflow() != null && ( workflowGraph.getWorkflow().isFinished() || workflowGraph.getWorkflow().isStopped() ) ) {
          refreshImage( event.gc );

//          if ( image != null && !image.isDisposed() ) {
//            event.gc.drawImage( image, 0, 0 );
//          }
        } else {
          Rectangle bounds = canvas.getBounds();
          if ( bounds.width <= 0 || bounds.height <= 0 ) {
            return;
          }

          event.gc.setForeground( GuiResource.getInstance().getColorWhite() );
          event.gc.setBackground( GuiResource.getInstance().getColorWhite() );
          event.gc.fillRectangle( new Rectangle( 0, 0, bounds.width, bounds.height ) );
          event.gc.setForeground( GuiResource.getInstance().getColorBlack() );
          String metricsMessage = BaseMessages.getString( PKG, "JobMetricsDelegate.JobIsNotRunning.Message" );
          org.eclipse.swt.graphics.Point extent = event.gc.textExtent( metricsMessage );
          event.gc.drawText( metricsMessage, ( bounds.width - extent.x ) / 2, ( bounds.height - extent.y ) / 2 );
        }
      }
    } );

    // Refresh automatically every 5 seconds as well.
    //
    final Timer timer = new Timer( "HopGuiWorkflowMetricsDelegate Timer" );
    timer.schedule( new TimerTask() {
      public void run() {
        updateGraph();
      }
    }, 0, 5000 );

    // When the tab is closed, we remove the update timer
    //
    jobMetricsTab.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent arg0 ) {
        timer.cancel();
      }
    } );

    // When the browser tab/window is closed, we remove the update timer
    jobMetricsTab.getDisplay().disposeExec( new Runnable() {
      @Override
      public void run() {
        timer.cancel();
      }
    } );

    // Show tool tips with details...
    //
    canvas.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDown( MouseEvent event ) {
        if ( drawAreas == null ) {
          return;
        }

        for ( int i = drawAreas.size() - 1; i >= 0; i-- ) {
          MetricsDrawArea drawArea = drawAreas.get( i );
          if ( drawArea.getArea().contains( event.x, event.y ) ) {
            MetricsDuration duration = drawArea.getDuration();
            if ( duration == null ) {
              continue;
            }

            ILoggingObject loggingObject =
              LoggingRegistry.getInstance().getLoggingObject( duration.getLogChannelId() );
            if ( loggingObject == null ) {
              return;
            }
          }
        }
      }
    } );

    canvas.addControlListener( new ControlAdapter() {

      @Override
      public void controlResized( ControlEvent arg0 ) {
        lastRefreshTime = 0; // force a refresh
      }
    } );
  }

  public void showMetricsView() {
    // What button?
    //
    if ( jobMetricsTab == null || jobMetricsTab.isDisposed() ) {
      addJobMetrics();
    } else {
      jobMetricsTab.dispose();

      workflowGraph.checkEmptyExtraView();
    }
  }

  public void updateGraph() {
    if ( jobGraph.getDisplay().isDisposed() ) {
      return;
    }

    workflowGraph.getDisplay().asyncExec( new Runnable() {
      public void run() {
        if ( metricsComposite != null && !metricsComposite.isDisposed() && canvas != null && !canvas.isDisposed()
          && jobMetricsTab != null && !jobMetricsTab.isDisposed() ) {
          if ( jobMetricsTab.isShowing() ) {
            canvas.redraw();
          }
        }
      }
    } );
  }

  private long lastRefreshTime = 0;

  public void resetLastRefreshTime() {
    lastRefreshTime = 0;
  }

  private void refreshImage( GC canvasGc ) {
    List<MetricsDuration> durations = MetricsUtil.getAllDurations( workflowGraph.getWorkflow().getLogChannelId() );
    if ( Utils.isEmpty( durations ) ) {
      // In case of an empty durations or null there is nothing to draw
      return;
    }

    // Sort the metrics.
    Collections.sort( durations, new Comparator<MetricsDuration>() {
      @Override
      public int compare( MetricsDuration o1, MetricsDuration o2 ) {
        return o1.getDate().compareTo( o2.getDate() );
      }
    } );

    Rectangle bounds = canvas.getBounds();
    if ( bounds.width <= 0 || bounds.height <= 0 ) {
      return;
    }

    if ( workflowGraph.getWorkflow() == null ) {
      image = null;
      return;
    }

    // For performance reasons, only ever refresh this image at most every 5 seconds...
    //
    if ( image != null && ( System.currentTimeMillis() - lastRefreshTime ) < 5000 ) {
      return;
    }
    lastRefreshTime = System.currentTimeMillis();

    if ( image != null ) {
      image.dispose(); // prevent out of memory...
      image = null;
    }

    // Correct size of canvas.
    //

    org.eclipse.swt.graphics.Point textExtent = canvasGc.textExtent( "AagKkiw" );
    int barHeight = textExtent.y + 8;

    // Make the height larger if needed for clarify
    //
    bounds.height = Math.max( durations.size() * barHeight, bounds.height );
    canvas.setSize( bounds.width, bounds.height );

    GCInterface gc =
        new SwtGc( canvasGc, new Point( bounds.width, bounds.height ), PropsUi.getInstance().getIconSize() );
    MetricsPainter painter = new MetricsPainter( gc, barHeight );
    // checking according to method's contract
    drawAreas = painter.paint( durations );
//    image = (Image) gc.getImage();

    // refresh the scrolled composite
    //
    // sMetricsComposite.setMinHeight(bounds.height);
    // sMetricsComposite.setMinWidth(bounds.width);
    sMetricsComposite.layout( true, true );

    // close shop on the SWT GC side.
    //
    gc.dispose();

    // Draw the image on the canvas...
    //
//    canvas.redraw();
  }

  /**
   * @return the jobMetricsTab
   */
  public CTabItem getJobMetricsTab() {
    return jobMetricsTab;
  }

  /**
   * @return the emptyGraph
   */
  public boolean isEmptyGraph() {
    return emptyGraph;
  }

  public void layoutMetricsComposite() {
    if ( !metricsComposite.isDisposed() ) {
      metricsComposite.layout( true, true );
    }
  }

  public void refresh() {
    canvas.update();
  }

}
