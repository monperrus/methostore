package methostore;

import methostore.impl.lucene.LuceneDatastoreImpl;


/** Entity instances are obtained with {@link LuceneDatastoreImpl.createEntity} */
public interface Entity extends Iterable<String>{

  // the core API
  /** Returns the ID of the entity. The ID format is specfiic to the implementation */
  public String getId();

  /** Sets the property "name" to the given value */
  public Entity setProperty(String name, String value);

  /** Returns the content of the property "name". Throws an exception if none exists. */
  public String getProperty(String propertyName);
 
  /** Returns true iff the entity has a property named "name" */
  public boolean hasProperty(String name);

  // the extended API (for handling richer data types)
  /** sets a property typed as long (allows numerical queries afterwards) */
  public Entity setPropertyAsLong(String name, long value);

  /** sets a property typed as long (allows numerical queries afterwards) */
	public Entity setPropertyAsDouble(String name, double value);

  /** sets a property typed as keyword (the property is a single term) */
  public Entity setPropertyAsKeyword(String name, String value);

	public long getPropertyAsLong(String propertyName);

	public double getPropertyAsDouble(String propertyName);
	
	// handy shortcuts
  public long getLong(String propertyName);
	public double getDouble(String propertyName);

}
