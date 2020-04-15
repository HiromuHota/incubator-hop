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

package org.apache.hop.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/**
 * @author nhudak
 * @see BaseHopServerPlugin
 */
public interface IHopServerRequestHandler {
  void handleRequest( ICarteRequest request ) throws IOException;

  interface ICarteRequest {
    String getMethod();

    Map<String, Collection<String>> getHeaders();

    String getHeader( String name );

    Map<String, Collection<String>> getParameters();

    String getParameter( String name );

    InputStream getInputStream() throws IOException;

    ICarteResponse respond( int status );
  }

  interface ICarteResponse {
    void with( String contentType, IWriterResponse response ) throws IOException;

    void with( String contentType, IOutputStreamResponse response ) throws IOException;

    void withMessage( String text ) throws IOException;
  }

  interface IWriterResponse {
    void write( PrintWriter writer ) throws IOException;
  }

  interface IOutputStreamResponse {
    void write( OutputStream outputStream ) throws IOException;
  }
}