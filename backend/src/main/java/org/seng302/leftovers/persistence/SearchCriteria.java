package org.seng302.leftovers.persistence;

/**
 * Defines a criteria for building predicates
 */
public class SearchCriteria {

    public enum Pred {
        GREATER_THAN,
        LESS_THAN,
        COLON,
        EQUAL
    }

    private String column;

    private Pred operation;

    private Object value;

    private boolean isOrPredicate = true;

    public SearchCriteria(String column, String operation, Object value) {
        setKey(column);
        setOperation(operation);
        setValue(value);
    }

    /**
     * Search criteria predicate helper
     * @param column The column to compare
     * @param operation The compare operation
     * @param value The value to compare against
     * @param isOrPredicate Determines if predicate will be AND / OR
     */
    public SearchCriteria(String column, String operation, Object value, boolean isOrPredicate) {
        setKey(column);
        setOperation(operation);
        setValue(value);
        setOrPredicate(isOrPredicate);
    }


    public void setKey(String column){
        this.column=column;
    }

    public String getKey() {
        return this.column;
    }

    public void setOperation(String operation) {
        if (">".equals(operation)) {
            this.operation = Pred.GREATER_THAN;
        } else if ("<".equals(operation)) {
            this.operation = Pred.LESS_THAN;
        } else if (":".equals(operation)) {
            this.operation = Pred.COLON;
        } else if ("=".equals(operation)) {
            this.operation = Pred.EQUAL;
        } else {
            this.operation = null;
        }
    }

    public Pred getOperation() {
        return this.operation;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isOrPredicate(){
        return this.isOrPredicate;
    }

    public void setOrPredicate(boolean orPredicate){
        this.isOrPredicate = orPredicate;
    }
}