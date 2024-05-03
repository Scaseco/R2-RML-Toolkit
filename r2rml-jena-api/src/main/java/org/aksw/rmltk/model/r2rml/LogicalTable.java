package org.aksw.rmltk.model.r2rml;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.aksw.rmltk.model.backbone.r2rml.ILogicalTableR2rml;

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
@ResourceView
public interface LogicalTable
    extends ILogicalTableR2rml, MappingComponent
{
    /**
     * R2RML specifies that the minimum condition for an entity to qualify as an "base table or view" is
     * "Having an rr:tableName property"
     *
     * @return
     */
    default boolean qualifiesAsBaseTableOrView() {
        return hasProperty(RR.tableName);
    }

    /**
     * Obtain a RefObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsRefObjectMap()} to yield true.
     *
     * @return
     */
    default BaseTableOrView asBaseTableOrView() {
        return as(BaseTableOrView.class);
    }

    /**
     * R2RML specifies that the minimum condition for an entity to qualify as an "R2RML view" is
     * "Having an rr:sqlQuery property"
     *
     * @return
     */
    default boolean qualifiesAsR2rmlView() {
        return hasProperty(RR.sqlQuery);
    }

    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    default R2rmlView asR2rmlView() {
        return as(R2rmlView.class);
    }
}
