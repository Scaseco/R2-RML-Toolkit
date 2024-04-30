package org.aksw.rml.jena.common;

/**
 * Interface for RDF-based logical tables.
 *
 * As this denotes the same information as in the more basic
 * {@link org.aksw.PlainLogicalTable.domain.api.LogicalTable}, deriving from it should be
 * safe.
 *
 *
 *
 * @author raven Apr 1, 2018
 *
 */
public interface LogicalTable
    extends MappingComponent
{
    /**
     * R2RML specifies that the minimum condition for an entity to qualify as an "base table or view" is
     * "Having an rr:tableName property"
     *
     * @return
     */
    boolean qualifiesAsBaseTableOrView();

    /**
     * Obtain a RefObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsRefObjectMap()} to yield true.
     *
     * @return
     */
    BaseTableOrView asBaseTableOrView();

    /**
     * R2RML specifies that the minimum condition for an entity to qualify as an "R2RML view" is
     * "Having an rr:sqlQuery property"
     *
     * @return
     */
    boolean qualifiesAsR2rmlView();

    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    R2rmlView asR2rmlView();
}
