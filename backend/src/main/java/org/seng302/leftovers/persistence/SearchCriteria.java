package org.seng302.leftovers.persistence;

/**
 * Defines a criteria for building predicates
 */
public class SearchCriteria {

    public enum Pred {
        GREATER_THAN,
        LESS_THAN,
        PARTIAL_MATCH,
        FULL_MATCH
    }

    private String column;

    private Pred operation;

    private Object value;

    private boolean isOrPredicate = true;

    /**
     * @param column Name of database column to match
     * @param operation The predicate to ues for matching
     * @param value Value to match in DB
     */
    public SearchCriteria(String column, Pred operation, Object value) {
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
    public SearchCriteria(String column, Pred operation, Object value, boolean isOrPredicate) {
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

    public void setOperation(Pred operation) { this.operation = operation; }

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