/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.pipeline.transforms.xml.xmlinputstream;

import java.util.Arrays;
import java.util.List;

import org.apache.hop.core.exception.HopException;
import org.junit.Test;

public class XMLInputStreamMetaTest {

  @Test
  public void testLoadSaveRoundTrip() throws HopException {
    List<String> attributes =
      Arrays.asList( "filename", "addResultFile", "nrRowsToSkip", "rowLimit", "defaultStringLen", "encoding",
        "enableNamespaces", "enableTrim", "includeFilenameField", "filenameField", "includeRowNumberField",
        "rowNumberField", "includeXmlDataTypeNumericField", "xmlDataTypeNumericField",
        "includeXmlDataTypeDescriptionField", "xmlDataTypeDescriptionField", "includeXmlLocationLineField",
        "xmlLocationLineField", "includeXmlLocationColumnField", "xmlLocationColumnField",
        "includeXmlElementIDField", "xmlElementIDField", "includeXmlParentElementIDField",
        "xmlParentElementIDField", "includeXmlElementLevelField", "xmlElementLevelField",
        "includeXmlPathField", "xmlPathField", "includeXmlParentPathField", "xmlParentPathField",
        "includeXmlDataNameField", "xmlDataNameField", "includeXmlDataValueField", "xmlDataValueField" );

//    TransformLoadSaveTester<XmlInputStreamMeta> loadSaveTester =
//      new TransformLoadSaveTester<XmlInputStreamMeta>( XmlInputStreamMeta.class, attributes );
//
//    loadSaveTester.testSerialization();
  }
}
