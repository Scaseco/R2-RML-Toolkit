package org.aksw.rmltk.model.backbone.r2rml;

import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.aksw.rmltk.model.backbone.common.IMappingComponent;

/**
 * Interface for RDF-based logical tables.
 *
 * As this denotes the same information as in the more basic
 * {@link org.aksw.ILogicalTableR2rml.domain.api.LogicalTable}, deriving from it should be
 * safe.
 *
 *
 *
 * @author raven Apr 1, 2018
 *
 */
public interface ILogicalTableR2rml
    extends IMappingComponent, IAbstractSource
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
    IBaseTableOrView asBaseTableOrView();

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
    IR2rmlView asR2rmlView();
}
