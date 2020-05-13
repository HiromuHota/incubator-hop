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
package org.apache.hop.databases.dbase;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.row.value.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DbaseDatabaseMetaTest {
  private DbaseDatabaseMeta nativeMeta, odbcMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new DbaseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    odbcMeta = new DbaseDatabaseMeta();
    odbcMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_ODBC },
      nativeMeta.getAccessTypeList() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "jdbc:odbc:FOO", odbcMeta.getURL( "IGNORED", "IGNORED", "FOO" ) );
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertEquals( "\"BAR\"", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertFalse( nativeMeta.supportsTransactions() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsViews() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.supportsSetMaxRows() );
  }

  @Test
  public void testSqlStatements() {
    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
      nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
      nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "insert into FOO(FOOVERSION) values (1)",
      nativeMeta.getSqlInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATETIME",
      nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATETIME",
      nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );

    assertEquals( "CHAR(1)",
      nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "DECIMAL(10)",
      nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "DECIMAL(8)",
      nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 8, 0 ), "", "FOO", false, false, false ) );

    assertEquals( "DECIMAL(19)",
      nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 19, 0 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(22)",
      nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 22, 0 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL",
      nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "", "", false, false, false ) ); // Pretty sure this is a bug ...

    assertEquals( "VARCHAR()",
      nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) ); // Pretty sure this is a bug ...
    assertEquals( "VARCHAR(15)",
      nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
      nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 0, 0 ), "", "", false, false, false ) );
    // assertEquals( "VARBINARY(50)",
    //     nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 50, 0 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
      nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
      nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
