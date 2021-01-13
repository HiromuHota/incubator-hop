/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.ui.core.widget.text;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by bmorrise on 9/15/17.
 */
public class TextFormatterTest {

  @Test
  public void testUrlFormatter() {
    String value =
      "This has one [this is the first value](http://www.example.com/page/index.html?query=test1) [this is the second"
        + " value](http://www.example.com/page/index.html?query=test2)";

    Format format = TextFormatter.getInstance().execute( value );

    Assert.assertEquals( "This has one this is the first value this is the second value", format.getText() );
  }

}
