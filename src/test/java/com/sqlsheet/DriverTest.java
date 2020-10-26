package com.sqlsheet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DriverTest {

  @BeforeClass
  public static void loadDriverClass() throws ClassNotFoundException {
    Class.forName("com.sqlsheet.XlsDriver");
  }

  @Test
  public void testXlsSheetCRUD() throws Exception {
    Connection conn = DriverManager.
               getConnection("jdbc:xls:file:" + DriverTest.class.getResource("/test.xls").getFile());

    Statement create = conn.createStatement();
    create.executeUpdate("CREATE TABLE \"TEST_INSERT\"(\"COL1\" INT, COL2 VARCHAR(255), COL3 DATE)");
    PreparedStatement write = conn.prepareStatement("INSERT INTO TEST_INSERT(COL1, \"COL2\", COL3) VALUES(?,?,?)");
    for (int i = 0; i < 3; i++) {
      write.setDouble(1, i);
      write.setString(2, "Row" + i);
      write.setDate(3, new java.sql.Date(new Date().getTime()));
      write.execute();
    }
    processBaseResultset(conn, "SELECT * FROM \"TEST_INSERT\"");

    Statement drop = conn.createStatement();
    drop.execute("DROP TABLE TEST_INSERT");

    drop.close();
    create.close();
    write.close();
    conn.close();
  }

  @Test
  public void testXlsSheetNameQuotes() throws Exception {
    Connection conn = DriverManager.
               getConnection("jdbc:xls:file:" + ClassLoader.getSystemResource("test.xls").getFile());
    processBaseResultset(conn, "SELECT * FROM \"2009\"");
  }

  @Test
  public void testXlsSheetNameNoQuotes() throws Exception {
    Connection conn = DriverManager.
               getConnection("jdbc:xls:file:" + ClassLoader.getSystemResource("test.xls").getFile());
    processBaseResultset(conn, "SELECT * FROM SHEET1");
  }

  @Test
  public void testXlsStreamSheetNameQuotes() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("test.xls").getFile() + "?readStreaming=true");
    processBaseStreamingResultset(conn, "SELECT * FROM \"2009\"");
  }

  @Test
  public void testXlsStreamSheetNameNoQuotes() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("test.xls").getFile() + "?readStreaming=true");
    processBaseStreamingResultset(conn, "SELECT * FROM SHEET1");
  }

  @Test
  public void testXlsxSheetNameQuotes() throws Exception {
    Connection conn = DriverManager.getConnection("jdbc:xls:file:" + ClassLoader.getSystemResource("test.xlsx").
                                                  getFile());
    processBaseResultset(conn, "SELECT * FROM \"2009\"");
  }

  @Test
  public void testXlsxSheetNameNoQuotes() throws Exception {
    Connection conn = DriverManager.getConnection("jdbc:xls:file:" + ClassLoader.getSystemResource("test.xlsx").
                                                  getFile());
    processBaseResultset(conn, "SELECT * FROM SHEET1");
  }

  @Test
  public void testXlsxStreamSheetNameQuotes() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("test.xlsx").getFile() + "?readStreaming=true");
    processBaseStreamingResultset(conn, "SELECT * FROM \"2009\"");
  }

  @Test
  public void testXlsxStreamSheetNameNoQuotes() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("test.xlsx").getFile() + "?readStreaming=true");
    processBaseStreamingResultset(conn, "SELECT * FROM SHEET1");
  }

  @Test
  public void testBugNo7() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("bug7.xlsx").getFile() + "?readStreaming=true");
    Statement stmt = conn.createStatement();
    ResultSet results = stmt.executeQuery("SELECT * FROM bug7");
    Assert.assertEquals(results.getMetaData().getColumnCount(), 13L);
    Assert.assertEquals(results.next(), true);
    Assert.assertEquals(results.next(), true);
    Assert.assertEquals(results.getString("Zone ID"), results.getString(1));

    conn = DriverManager
    .getConnection("jdbc:xls:file:" + ClassLoader.getSystemResource("bug7.xlsx").getFile() + "?readStreaming=no");
    stmt = conn.createStatement();
    results = stmt.executeQuery("SELECT * FROM bug7");
    Assert.assertEquals(results.getMetaData().getColumnCount(), 13L);
    Assert.assertEquals(results.next(), true);
    Assert.assertEquals(results.next(), true);
    Assert.assertEquals(results.getLong("Zone ID"), results.getLong(1));
  }

  @Test
  public void testBugNo6() throws Exception {
    Connection conn = DriverManager
               .getConnection(
                       "jdbc:xls:file:" + ClassLoader.getSystemResource("test.xlsx").getFile() + "?readStreaming=true");
    Statement stmt = conn.createStatement();
    ResultSet results = stmt.executeQuery("SELECT * FROM SHEET1");
    Assert.assertEquals(results.getMetaData().getColumnCount(), 3L);

    ResultSetMetaData resultSetMetaData = results.getMetaData();
    Assert.assertEquals("java.lang.Double", resultSetMetaData.getColumnTypeName(1));
    Assert.assertEquals("java.lang.String", resultSetMetaData.getColumnTypeName(2));
    Assert.assertEquals("java.util.Date", resultSetMetaData.getColumnTypeName(3));

  }

  private void processBaseResultset(Connection conn, String sql) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet results = stmt.executeQuery(sql);
    Assert.assertEquals(results.getMetaData().getColumnCount(), 3L);
    Long count = 0L;
    while (results.next()) {
      Assert.assertEquals(Double.class, results.getObject(1).getClass());
      Assert.assertEquals(String.class, results.getObject(2).getClass());
      Assert.assertEquals(java.sql.Date.class, results.getObject(3).getClass());
      count++;
    }
    Assert.assertEquals(count.longValue(), 3L);
    results.close();
    stmt.close();
    conn.close();
  }

  private void processBaseStreamingResultset(Connection conn, String sql) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet results = stmt.executeQuery(sql);
    Assert.assertEquals(results.getMetaData().getColumnCount(), 3L);
    Long count = 0L;
    while (results.next()) {
      Assert.assertEquals(Double.class, results.getObject(1).getClass());
      Assert.assertEquals(String.class, results.getObject(2).getClass());
      Assert.assertEquals(java.util.Date.class, results.getObject(3).getClass());
      count++;
    }
    Assert.assertEquals(count.longValue(), 3L);
    results.close();
    stmt.close();
    conn.close();
  }

}
