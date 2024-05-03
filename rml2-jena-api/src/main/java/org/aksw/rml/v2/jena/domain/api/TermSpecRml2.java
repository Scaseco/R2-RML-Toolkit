package org.aksw.rml.v2.jena.domain.api;

import org.aksw.rml2.vocab.jena.RML2;
import org.aksw.rmltk.model.backbone.common.ITermSpec;

/**
 * A common parent type for 'term-producing' constructs, namely TermMap and RefObjectMap.
 *
 * https://www.w3.org/TR/r2rml/#class-index states:
  * <i>As noted earlier, a single node in an R2RML mapping graph may represent multiple mapping components and thus be typed as several of these classes. However, the following classes are disjoint:</i>
  * <ul>
  *   <li><b>rr:TermMap and rr:RefObjectMap</b></li>
  *   <li>[...]</li>
  * </ul>
 */
public interface TermSpecRml2
    extends MappingComponentRml2, ITermSpec
{
    /**
     * R2RML specifies that the condition for an entity to qualify as an "ref object map" is
     * "Having an rr:parentTriplesMap property"
     *
     * @return
     */
    @Override
    default boolean qualifiesAsRefObjectMap() {
        return hasProperty(RML2.parentTriplesMap);
    }

    /**
     * Obtain a RefObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsRefObjectMap()} to yield true.
     *
     * @return
     */
    @Override
    default RefObjectMapRml2 asRefObjectMap() {
        return as(RefObjectMapRml2.class);
    }

    /**
     * R2RML specifies that the condition for an entity to qualify as a "term map" is
     * "Having exactly one of rr:constant, rr:column, rr:template"
     *
     * @return
     */
    @Override
    default boolean qualifiesAsTermMap() {
        return hasProperty(RML2.constant) || hasProperty(RML2.column) || hasProperty(RML2.template);
    }

    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    @Override
    default TermMapRml2 asTermMap() {
        return as(TermMapRml2.class);
    }
}
