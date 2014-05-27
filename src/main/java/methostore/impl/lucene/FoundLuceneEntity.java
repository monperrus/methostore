package methostore.impl.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class FoundLuceneEntity extends LuceneEntity {
  public ScoreDoc _score;

  public FoundLuceneEntity(Document document, ScoreDoc score) {
    super(document);
    this._score=score;
  }
}
