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

package org.apache.hop.core.logging;

import org.apache.hop.core.gui.IGc;
import org.apache.hop.core.gui.Point;
import org.apache.hop.core.metrics.MetricsDuration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsPainterTest {
  MetricsPainter metricsPainter;
  List<MetricsDuration> durations = null;
  final int heightStub = 0;
  final double pixelsPerMsStub = 0;
  final long periodInMsStub = 0;

  @Before
  public void initialize() {
    metricsPainter = mock( MetricsPainter.class );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalArgumentExceptionNullArgPaint() {
    callPaint( durations );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalArgumentExceptionEmptyArgPaint() {
    durations = new ArrayList<MetricsDuration>();
    callPaint( durations );
  }

  @Test( timeout = 1000 )
  public void testDrawTimeScaleLineInfinityLoop() {
    IGc gCInterfaceMock = mock( IGc.class );
    when( metricsPainter.getGc() ).thenReturn( gCInterfaceMock );
    doCallRealMethod().when( metricsPainter ).drawTimeScaleLine( heightStub, pixelsPerMsStub, periodInMsStub );
    when( gCInterfaceMock.textExtent( anyString() ) ).thenReturn( mock( Point.class ) );

    metricsPainter.drawTimeScaleLine( heightStub, pixelsPerMsStub, periodInMsStub );
  }

  private void callPaint( List<MetricsDuration> durations ) {
    doCallRealMethod().when( metricsPainter ).paint( anyListOf( MetricsDuration.class ) );
    metricsPainter.paint( durations );

  }

}
