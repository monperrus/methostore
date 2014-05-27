package methostore;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


/** 
 * Provides a persistent, queryable key-value based datastore.
 * 
 * Datastore instances are obtained with concrete implementations:
 * For instance: 
 * <pre> Datastore ds = LuceneDatastore.service(); </pre>
 * 
 * The main challenges for concrete implementations are:<br/>
 * 	- to provide an indexing of properties and fast answers to {@link #searchEntities(Map<String,String> m)}<br/>
 *  - to provide a powerful and intuitive query language for {@link #searchEntities(String query)}<br/>
 *  - to provide a distributed datastore for the internet scale<br/>
 * 
 * @see Datastore
 * @author Martin Monperrus
 *
 */
public interface Datastore {
	/** creates a new Entity */
	public Entity createEntity();

	/** puts or updates an Entity in the datastore. */
	public Datastore put(Entity e);

	/** deletes an Entity */
	public Datastore delete(Entity e);

	/** returns an entity directly with its ID 
	 * Should be more efficient than searchEntities 
	 * @throws NoSuchElementException if none is found
	 * */
	public Entity get(String id);
	
	/** returns  all entities matching all fields/values of the map */
	public List<Entity> searchEntities(Map<String, String> query);
	
	/** returns  all entities matching given a query in a query language 
	 * The query language is implementation-dependent
	 * */
	public List<Entity> searchEntities(String query);

	/** returns  all entities of the datastore */
	public List<Entity> getAllEntities();
	
	/** is an handy shortcut for create and put */
	public Datastore createAndSaveEntity(Map<String, String> m);

	/** gets the first entity matching the query */
  public Entity searchEntity(String query);
}
