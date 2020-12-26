/*
 * *****************************************************************************
 *
 *  Hop : The Hop Orchestration Platform
 *
 *  Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.apache.hop.ui.core.widget.text;

import java.util.regex.Matcher;

/**
 * Created by bmorrise on 9/15/17.
 */
public class UrlFormatRule extends FormatRule {
  private static final int LINK_TEXT = 0;
  private static final int LINK_URL = 1;
  private static final String PATTERN = "\\[(.*?)\\]\\((https?://[\\w:.\\?#/=?]+)\\)";

  public UrlFormatRule() {
    super( PATTERN );
  }

  public Format execute( String value ) {
    Format format = new Format();
    Matcher matcher = parse( value );
    while ( matcher.find() ) {
      value = value.replace( matcher.group( LINK_TEXT ), matcher.group( LINK_URL ) );
    }
    format.setText( value );
    return format;
  }
}
