package methostore.tests;

import java.util.UUID;

import methostore.Datastore;
import methostore.Entity;
import methostore.impl.lucene.LuceneDatastoreFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MethostoreTest {
 
  private Datastore ds;
  
  @Before
  public void init() {
    ds = LuceneDatastoreFactory.createDatastore("/tmp/methostore"); 
  }
  
  @Test
  public void test00() {
    
    // getting the initial size of the datastore
    int initSize = ds.getAllEntities().size();
    
    // creating an entity
    Entity e = ds.createEntity();
    String id = e.getId();
    String nameVal = UUID.randomUUID().toString();
    String cityVal = UUID.randomUUID().toString().split("-")[0];
    e.setProperty("name", nameVal);
    e.setProperty("city", cityVal);
    long age = 29;
    double weight = 32.4;
    e.setPropertyAsLong("age", age);
    e.setPropertyAsDouble("weight", weight);
    ds.put(e);        
    int newSize = initSize+1;
    Assert.assertEquals(newSize, ds.getAllEntities().size());
        
    
    Entity e2 = ds.searchEntity("name:"+nameVal);
    Assert.assertEquals(id, e2.getId());
    Assert.assertEquals(nameVal, e2.getProperty("name"));
    Assert.assertEquals(age, e2.getLong("age"));
    Assert.assertEquals(weight, e2.getDouble("weight"),0.00001);

    
    // modifying the entity
    e2.setProperty("name", "joe");
    ds.put(e2);
    
    // e2 is not a new entity! so the size must be the same     
    Assert.assertEquals(newSize, ds.getAllEntities().size());
    Assert.assertEquals("joe", e2.getProperty("name"));
    Assert.assertEquals("joe", ds.searchEntity("city:"+cityVal).getProperty("name"));

    // adding a new entity in order not to call get(id) on the last inserted entity
    Entity etmp = ds.createEntity();
    etmp.setProperty("name", "bob");
    ds.put(etmp);
    newSize = initSize+2;

    
    // testing the get(key)
    Entity e3 = ds.get(id);
    Assert.assertEquals("joe", e3.getProperty("name"));
    Assert.assertEquals(age, e3.getLong("age"));
    
    {
    // testing delete  with the same object
    Entity eToBeDeleted = ds.createEntity();
    eToBeDeleted.setProperty("foo", "bar");
    ds.put(eToBeDeleted);
    Assert.assertEquals(newSize+1, ds.getAllEntities().size());
    ds.delete(eToBeDeleted);
    Assert.assertEquals(newSize, ds.getAllEntities().size());
    }
    
    {
    // testing delete with the object got from searching
    Entity eToBeDeleted2 = ds.createEntity();
    String id2 = eToBeDeleted2.getId();
    eToBeDeleted2.setProperty("foo", "bar");
    ds.put(eToBeDeleted2);
    Assert.assertEquals(newSize+1, ds.getAllEntities().size());
    Entity eToBeDeleted3 = ds.get(id2);
    Assert.assertEquals("bar", eToBeDeleted3.getProperty("foo"));    
    ds.delete(eToBeDeleted3);
    Assert.assertEquals(newSize, ds.getAllEntities().size());
    }

  }
}
