package methostore.impl.lucene;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import methostore.Entity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.util.Version;

public class LuceneEntity implements Entity {

  public static final String LUCENE_UUID = "_key";
  private final Document _doc;

  LuceneEntity() {
    String id = java.util.UUID.randomUUID().toString();
    this._doc = new Document();
    this.setKey(id);
  }

  LuceneEntity(Analyzer analyzer) {
    this();
    this._analyzer=  analyzer;
  }

  
  /** sets the key of the entity */
  private void setKey(String key) {
	this._setProperty(LUCENE_UUID, key, new KeywordAnalyzer());      
  }
  


  public LuceneEntity(Document document) {
    this._doc = document;
  }

  private static Analyzer _analyzer = new WhitespaceAnalyzer(Version.LUCENE_34);

  public Entity setAnalyzer(Analyzer a) {
    _analyzer = a;
    return this;
  }

  @Override
  public Entity setPropertyAsKeyword(String name, String value) {
	return this._setProperty(name, value, new KeywordAnalyzer());	     
  }

  @Override
  public Entity setProperty(String name, String value) {
      return this._setProperty(name, value, _analyzer);
  }
  
  public Entity _setProperty(String name, String value, Analyzer analyzer) {

    Field f = new Field(name, value, Store.YES, Index.ANALYZED, Field.TermVector.YES);
    f.setTokenStream(analyzer.tokenStream(name, new StringReader(value)));

    // contrary to Lucene,  we don't allow to have several properties with the same name
    // calling this method is safe is the property does not exist
    // API doc: "	If there is no field with the specified name, the document remains unchanged."
    this._doc.removeField(name);

    this._doc.add(f);
    
    // we also a meta field to keep track of the analyzer used
    String meta = "__meta_"+name;
    this._doc.removeField(meta);
    this._doc.add( new Field(meta, analyzer.getClass().getName(), Store.YES, Index.NOT_ANALYZED));
    return this;
  }

  public Document getDocument() {
    return this._doc;
  }

  public Map<String, String> getProperties() {
    Map<String, String> result = new HashMap<String, String>();
    for (Fieldable f : this._doc.getFields()) {
      result.put(f.name(), f.stringValue());
    }
    return result;
  }

  @Override
  public String getProperty(String prop) {
    Fieldable f = this._doc.getFieldable(prop);
    if (f == null) {
      throw new NoSuchElementException(prop + " in " + this.toString());
    }
    return f.stringValue();
  }

  @Override
  public String getId() {
    return this.getProperty(LUCENE_UUID);
  }

  /*public JSONObject toJSON() {
        try {
            JSONObject jsonObj;
            jsonObj = new JSONObject();
            for (String prop : this.getProperties().keySet()) {
                Object val = this.getProperty(prop);
  			if (val!=null)	{jsonObj.put(prop, val.toString());}
                    
            }
            return jsonObj;
  	} catch (Exception e) {
  		throw new MethostoreException(e);
  	}                         
  }*/

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (Map.Entry<String, String> e : this.getProperties().entrySet()) {
      buffer.append(e.getKey() + ": " + e.getValue() + "\n");
    }
    return buffer.toString();
  }

  @Override
  public Entity setPropertyAsLong(String name, long value) {
    NumericField f = new NumericField(name, Store.YES, true);
    f.setLongValue(value);
    this._doc.add(f);
    return this;
  }

  @Override
  public Entity setPropertyAsDouble(String name, double value) {
    NumericField f = new NumericField(name, Store.YES, true);
    f.setDoubleValue(value);
    this._doc.add(f);
    return this;
  }

  @Override
  public long getLong(String prop) {
    return ((NumericField) this._doc.getFieldable(prop)).getNumericValue().longValue();
  }

  @Override
  public double getDouble(String prop) {
    return ((NumericField) this._doc.getFieldable(prop)).getNumericValue().doubleValue();
  }

//  public void retokenizeId() {
//    String id = this.getId();
//    this._doc.removeField(LUCENE_UUID);
//    Field f = new Field(LUCENE_UUID, id, Store.YES, Index.ANALYZED);
//    f.setTokenStream(new KeywordTokenizer(new StringReader(id)));
//    this._doc.add(f);
//  }

  @Override
  public long getPropertyAsLong(String propertyName) {
    return this.getLong(propertyName);
  }

  @Override
  public double getPropertyAsDouble(String propertyName) {
    return this.getDouble(propertyName);
  }

  @Override
  public Iterator<String> iterator() {
    List<String> result = new ArrayList<String>();
    for (Fieldable f : this._doc.getFields()) {
      result.add(f.name());
    }
    return result.iterator();
  }

  @Override
  public boolean hasProperty(String name) {
    try {
      this.getProperty(name);
    } catch (NoSuchElementException e) {
      return false;
    }
    return true;
  }

}
