package methostore.impl.lucene;

/** encapsulates checked exceptions as runtime*/
@SuppressWarnings("serial")
public class MethostoreException extends RuntimeException {

  public MethostoreException(Exception e) {
    super(e);
  }
}

