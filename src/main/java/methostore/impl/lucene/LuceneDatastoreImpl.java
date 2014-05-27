package methostore.impl.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import methostore.Datastore;
import methostore.Entity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/** singleton */ 
// this class must remain package visible
public class LuceneDatastoreImpl implements Datastore {

  // Saturday, November 19 2011
  // switched to a default WhitespaceAnalyzer
  public Analyzer _analyzer = new WhitespaceAnalyzer(Version.LUCENE_34);
	private IndexWriter _indexWriter;
	Directory _luceneDir = null;
	
	// the constructor must remain package-visible
	// clients must use the constructor
	LuceneDatastoreImpl(Directory datastoreDirectory) {
		try {
		this._luceneDir = datastoreDirectory;
		this._indexWriter = new IndexWriter(_luceneDir, new IndexWriterConfig(Version.LUCENE_34, this._analyzer));
		}
		catch (Exception e) {
			throw new MethostoreException(e);
		}
	}
	

  /** sets the analyzer for both indexing (new entities) and searching */
  public LuceneDatastoreImpl setAnalyzer(Analyzer a) {
    this._analyzer = a;
    return this;
  }
  

	/**
	 * encapsulates the creation of an Entity with a special kind (defined in
	 * {@see Constants})
	 */
	@Override
	public Entity createEntity() {
		Entity e = new LuceneEntity(_analyzer);
		return e;
	}

	@Override
	public LuceneDatastoreImpl put(Entity e) {
	  if (e==null) {
	    throw new IllegalArgumentException();
	  }
		try {
		  this.putFast(e);
			this._indexWriter.commit();
			return this;
		} catch (IOException ex) {
			throw new MethostoreException(ex);
		} catch (RuntimeException err) {
			System.err.println(new Exception().getStackTrace()[0].toString()+"\n"+e.toString());// new Exception is just used for location
			throw err;
		}
	}

	  public LuceneDatastoreImpl putFast(Entity e) {
	    try {
	      String id = e.getId();
	      
	      // bug in Lucene 3.0.1
	      // **hack**: we have to retokenize the id (because of KeywordAnalyzer)
	      //((LuceneEntity) e).retokenizeId();      
	      
	      this._indexWriter.updateDocument(new Term(LuceneEntity.LUCENE_UUID, id),((LuceneEntity) e).getDocument());
	      
	      // after a put, we have to set again all tokenizer
	      //((LuceneEntity) e).getDocument().getFields().get(0).
	      for (Fieldable f: ((LuceneEntity) e).getDocument().getFields()) {
	        TokenStream t = f.tokenStreamValue();
	        if (t!=null) {
	          if (t instanceof Tokenizer) {
	            ((Tokenizer)t).reset(new StringReader(f.stringValue()));	          
	          } else {
	            // well, this may work, but I'm not sure
	            // throw new MethostoreException("I don't know how to handle this case, sorry: "+t.getClass().getName());
	          }
	        }
	      }
	      
	      return this;
	    } catch (IOException ex) {
	      throw new MethostoreException(ex);
	    }
	  }

	@Override
	public Entity get(String id) {
		try {
			IndexReader _index = IndexReader.open(this._indexWriter, false);
			TermDocs td = _index.termDocs(new Term(LuceneEntity.LUCENE_UUID, id));			
			if (!td.next()) { throw new NullPointerException(); }
			int docIndex = td.doc();
			Entity e = new LuceneEntity(_index.document(docIndex));					
			_index.close();
			return e;
		} catch (Exception e) {
			throw new MethostoreException(e);
		}
		//return null;
	}


  /** gets a list of JSONObject bas on the parameters in the request */
	@Override
  public List<Entity> searchEntities(Map<String, String> m) {
		try {
			BooleanQuery q = new BooleanQuery();
			for (String val : m.keySet()) {
				q.add(new BooleanClause(new TermQuery(new Term(val,
						m.get(val))), Occur.MUST));
			}
			return this.searchEntities(q);
		} catch (Exception e) {
			throw new MethostoreException(e);
		}
	}

	@Override
  public List<Entity> searchEntities(String q) {
		return searchEntities(q, "content");
	}

  public List<Entity> searchEntities(String q, String field) {
    try {
      return this.searchEntities(
          new QueryParser(Version.LUCENE_30, field, this._analyzer)
            .parse(q));
    } catch (Exception e) {
      throw new MethostoreException(e);
    }
  }

	/** Subclasses may override. */
	public int getMaxResults() {
	   if (System.getProperty("methostore.maxresults")!=null) {
	     return Integer.parseInt(System.getProperty("methostore.maxresults"));
	   }
	   return 500; 
	 }

	 public List<Term> terms() throws Exception {
     IndexReader ir =  IndexReader.open(this._indexWriter, false);
     TermEnum l = ir.terms();
     List<Term> res = new LinkedList<Term>();
     while ((l.next())!=false) {
       res.add(l.term());
     }
    return res;  
	  }

	 
	public List<Entity> searchEntities(Query q) {
		try {

			IndexReader ir =  IndexReader.open(this._indexWriter, false);
			IndexSearcher indexSearcher = new IndexSearcher(ir);
			TopDocs topdocs = indexSearcher.search(q, this.getMaxResults());
			
			List<Entity> l = new ArrayList<Entity>();

			for (ScoreDoc s : topdocs.scoreDocs) {
				LuceneEntity e = new FoundLuceneEntity(ir.document(s.doc), s);
				
				// for backward compatibility
				if (e.getDocument().get(LuceneEntity.LUCENE_UUID) == null) {
					e.setProperty(LuceneEntity.LUCENE_UUID, "___"+s.doc);
				}
				
				l.add(e);
			}
			
			//System.out.println(q.toString());
			//System.out.println("results: "+l.size());
			// Note that the underlying IndexReader is not closed, if IndexSearcher was constructed with IndexSearcher(IndexReader r).
			indexSearcher.close();
			ir.close();

			return l;
		} catch (Exception e) {
			throw new MethostoreException(e);
		}
	}

		
	public void close() {
		try {
			this._indexWriter.close();
		} catch (Exception e) {
			throw new MethostoreException(e);
		
		}	
	}

	@Override
	public List<Entity> getAllEntities() {
		try {
		IndexReader indexReader =  IndexReader.open(this._indexWriter, false);
		List<Entity> l = new ArrayList<Entity>();
		for (int i = 0; i<indexReader.maxDoc(); i++) {
			if (!indexReader.isDeleted(i)) {
					l.add(new LuceneEntity(indexReader.document(i)));
			}
		}
		indexReader.close();
		return l;
		} catch (Exception e) {
			throw new MethostoreException(e);
		}
	}

	/**
	 * for instance: curl -X DELETE -v "http://localhost:8889/d/json/?_key=2bdce347-d888-4b9c-9659-778178dc31ed"
	 */
	@Override
	public LuceneDatastoreImpl delete(Entity e) {
		try {
			String id = e.getId();
			this._indexWriter.deleteDocuments(new Term(LuceneEntity.LUCENE_UUID, id));
			this._indexWriter.commit();
			return this;
		} catch (Exception ex) {
			throw new MethostoreException(ex);
		}
	}

  @Override
  public Entity searchEntity(String query) {
    return this.searchEntities(query).get(0);
  }
  
  /**
   * add an Entity in the Datastore based on the request parameters.
   * Parameters are obtained with req.getParameter, hence supports both POST
   * and GET parameters
   * */
  @Override
  public LuceneDatastoreImpl createAndSaveEntity(Map<String, String> m) {
    Entity e = this.createEntity();
    for (String propertyName : m.keySet()) {
      String propertyValue = m.get(propertyName);
      e.setProperty(propertyName.toString(), propertyValue);
    }
    if (!m.containsKey("created")) { e.setProperty("created", new Date().toString()); }
    this.put(e);
    return this;
  }

  public void toDisk() throws   Exception {
    this._indexWriter.optimize();
    this._indexWriter.commit();    
  }


}