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

package org.apache.hop.core.dnd;

import org.apache.commons.codec.binary.Base64;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.xml.IXml;
import org.apache.hop.core.xml.XmlHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;

/**
 * This class contains code to help you drag data from one part of a GUI to another by using XML as an intermediate
 * transform.
 *
 * @author matt
 * @since 2006-04-16
 */
public class DragAndDropContainer implements IXml {
  public static final int TYPE_TRANSFORM = 1;
  public static final int TYPE_BASE_TRANSFORM_TYPE = 2;
  public static final int TYPE_DATABASE_CONNECTION = 3;
  public static final int TYPE_PIPELINE_HOP = 4;
  public static final int TYPE_TEXT = 5;
  public static final int TYPE_JOB_ENTRY = 6;
  public static final int TYPE_BASE_JOB_ENTRY = 7;
  public static final int TYPE_PHYSICAL_TABLE = 8;
  public static final int TYPE_PHYSICAL_COLUMN = 9;
  public static final int TYPE_BUSINESS_VIEW = 10;
  public static final int TYPE_BUSINESS_TABLE = 11;
  public static final int TYPE_BUSINESS_COLUMN = 12;
  public static final int TYPE_RELATIONSHIP = 13;
  public static final int TYPE_BUSINESS_MODEL = 14;

  private static final String[] typeCodes = {
    "", "Transform", "BaseTransform", "DatabaseConnection", "PipelineHop", "Text", "Jobentry", "BaseJobentry",
    "PhysicalTable", "PhysicalColumn", "BusinessView", "BusinessTable", "BusinessColumn", "Relationship",
    "Business Model" };
  private static final String XML_TAG = "DragAndDrop";

  private int type;
  private String id;
  private String data;

  /**
   * Create a new DragAndDropContainer
   *
   * @param type The type of drag&drop to perform
   * @param data The data in the form of a String
   */
  public DragAndDropContainer( int type, String data ) {
    this( type, data, null );
  }

  /**
   * Create a new DragAndDropContainer
   *
   * @param type The type of drag&drop to perform
   * @param data The data in the form of a String
   * @param id   The id of the transform in the form of a String
   */
  public DragAndDropContainer( int type, String data, String id ) {
    this.type = type;
    this.data = data;
    this.id = id;
  }

  public int getType() {
    return type;
  }

  public void setType( int type ) {
    this.type = type;
  }

  public String getData() {
    return data;
  }

  public void setData( String data ) {
    this.data = data;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getTypeCode() {
    if ( type <= 0 || type >= typeCodes.length ) {
      return null;
    }

    return typeCodes[ type ];
  }

  public static final int getType( String typeCode ) {
    for ( int i = 1; i < typeCodes.length; i++ ) {
      if ( typeCodes[ i ].equals( typeCode ) ) {
        return i;
      }
    }
    return 0;
  }

  public String getXml() {
    try {
      StringBuilder xml = new StringBuilder( 100 );

      xml.append( XmlHandler.getXmlHeader() ); // UFT-8 XML header
      xml.append( XmlHandler.openTag( XML_TAG ) ).append( Const.CR );

      if ( id != null ) {
        xml.append( "  " ).append( XmlHandler.addTagValue( "ID", id ) );
      }
      xml.append( "  " ).append( XmlHandler.addTagValue( "DragType", getTypeCode() ) );
      xml.append( "  " ).append(
        XmlHandler
          .addTagValue( "Data", new String( Base64.encodeBase64( data.getBytes( Const.XML_ENCODING ) ) ) ) );
      xml.append( XmlHandler.closeTag( XML_TAG ) ).append( Const.CR );

      return xml.toString();
    } catch ( UnsupportedEncodingException e ) {
      throw new RuntimeException( "Unable to encode String in encoding [" + Const.XML_ENCODING + "]", e );
    }
  }

  /**
   * Construct a Drag and drop container from an XML String
   *
   * @param xml The XML string to convert from
   */
  public DragAndDropContainer( String xml ) throws HopXmlException {
    try {
      Document doc = XmlHandler.loadXmlString( xml );
      Node dnd = XmlHandler.getSubNode( doc, XML_TAG );

      id = XmlHandler.getTagValue( dnd, "ID" );
      type = getType( XmlHandler.getTagValue( dnd, "DragType" ) );
      data =
        new String( Base64.decodeBase64( XmlHandler.getTagValue( dnd, "Data" ).getBytes() ), Const.XML_ENCODING );
    } catch ( Exception e ) {
      throw new HopXmlException( "Unexpected error parsing Drag & Drop XML fragment: " + xml, e );
    }
  }

}
