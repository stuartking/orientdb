package com.orientechnologies.orient.graph.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-at-orientechnologies.com)
 */

public class OGraphBatchInsertTest extends TestCase {

  @Test
  public void test1() {
    String dbUrl = "memory:test1";
    OGraphBatchInsertBasic batch = new OGraphBatchInsertBasic(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L);
    batch.createEdge(0L, 2L);
    batch.end();
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbUrl);
    db.open("admin", "admin");
    List<?> result = db.query(new OSQLSynchQuery<Object>("select from V"));

    assertEquals(3, result.size());

    db.close();
  }

  @Test
  public void test2() {
    String dbUrl = "memory:test2";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    batch.createEdge(0L, 2L, null);
    batch.end();
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbUrl);
    db.open("admin", "admin");
    List<?> result = db.query(new OSQLSynchQuery<Object>("select from V"));
    for (Object o : result) {
      System.out.println(o);
    }
    assertEquals(3, result.size());
    db.close();
  }

  @Test
  public void test3() {
    String dbUrl = "memory:test3";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    batch.createEdge(1L, 2L, null);
    batch.createEdge(2L, 0L, null);
    batch.createVertex(3L);
    batch.end();
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbUrl);
    db.open("admin", "admin");
    List<?> result = db.query(new OSQLSynchQuery<Object>("select from V"));
    for (Object o : result) {
      System.out.println(o);
    }
    assertEquals(4, result.size());
    db.close();
  }

  @Test
  public void test4() {
    String dbUrl = "memory:test4";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    batch.createEdge(1L, 2L, null);
    batch.createEdge(2L, 0L, null);
    batch.createVertex(3L);
    Map<String, Object> vertexProps = new HashMap<String, Object>();
    vertexProps.put("foo", "foo");
    vertexProps.put("bar", 3);
    batch.setVertexProperties(0L, vertexProps);
    batch.end();
    ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbUrl);
    db.open("admin", "admin");
    List<?> result = db.query(new OSQLSynchQuery<Object>("select from V"));
    boolean found0 = false;
    for (Object o : result) {
      ODocument doc = ((ODocument) o);
      if (new Long(0).equals(doc.field("uid"))) {
        found0 = true;
        assertEquals("foo", doc.field("foo"));
        assertEquals(3, doc.field("bar"));
      } else {
        assertNotSame("foo", doc.field("foo"));
        assertNotSame(3, doc.field("bar"));

      }
    }
    assertTrue(found0);
    assertEquals(4, result.size());
    db.close();
  }

  @Test
  public void testFail1() {
    String dbUrl = "memory:testFail1";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    Map<String, Object> vertexProps = new HashMap<String, Object>();
    vertexProps.put("bar", 3);
    batch.setVertexProperties(0L, vertexProps);

    try {
      batch.createVertex(3L);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testFail2() {
    String dbUrl = "memory:testFail2";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    Map<String, Object> vertexProps = new HashMap<String, Object>();
    vertexProps.put("bar", 3);
    batch.setVertexProperties(0L, vertexProps);

    try {
      batch.createVertex(3L);
      fail();
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testTraverse() {
    String dbUrl = "memory:testFail2";
    OGraphBatchInsert batch = new OGraphBatchInsert(dbUrl, "admin", "admin");
    batch.begin();

    batch.createEdge(0L, 1L, null);
    batch.createEdge(1L, 2L, null);
    batch.createEdge(2L, 3L, null);
    Map<String, Object> vertexProps = new HashMap<String, Object>();
    vertexProps.put("foo", "bar");
    batch.setVertexProperties(3L, vertexProps);

    batch.end();

    OrientGraph g = new OrientGraph(dbUrl, "admin", "admin");

    Iterable<Vertex> result = g.command(
        new OSQLSynchQuery<Vertex>("select expand(out().in().out().out().in().out()) from V where uid = ?")).execute(1L);

    for (Vertex v : result) {
      assertEquals("bar", v.getProperty("foo"));
    }

  }

}
