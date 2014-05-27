package methostore;

/** Represents a query */
public interface Query {
  /** adds a query item */
  Query addItem(String field, String value);
  /** filters entities that field.contains(value)*/
  Query addFilter(String field, String value);
}
