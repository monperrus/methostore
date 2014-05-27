package methostore.impl.lucene;

import java.io.File;

import methostore.Entity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class LuceneDatastoreFactory {

  /** LuceneDatastoreFactory is not meant to be used as an object */
  private LuceneDatastoreFactory() {}
  
  public static Analyzer _analyzer = new WhitespaceAnalyzer(Version.LUCENE_34);
 
  /** returns a datastore stored in RAM */
  public static LuceneDatastoreImpl createDatastore() {
    LuceneDatastoreImpl luceneDatastoreImpl = new LuceneDatastoreImpl(new RAMDirectory());
    return luceneDatastoreImpl;
  }

  /** returns a datastore stored on disk */
   public static LuceneDatastoreImpl createDatastore(String datastoreDirectory) {
    try {
      return new LuceneDatastoreImpl(new SimpleFSDirectory(new File(datastoreDirectory)));
    } catch (Exception e) {
      throw new MethostoreException(e);
    }
  }

  /** returns an entity */
  public static Entity createEntity() {
    return new LuceneEntity().setAnalyzer(_analyzer);
  }

}
