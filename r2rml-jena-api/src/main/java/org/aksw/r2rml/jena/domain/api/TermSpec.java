package org.aksw.r2rml.jena.domain.api;

import org.aksw.r2rml.jena.vocab.RR;

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
public interface TermSpec
    extends MappingComponent
{
    /**
     * R2RML specifies that the condition for an entity to qualify as an "ref object map" is
     * "Having an rr:parentTriplesMap property"
     *
     * @return
     */
    default boolean qualifiesAsRefObjectMap() {
        return hasProperty(RR.parentTriplesMap);
    }

    /**
     * Obtain a RefObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsRefObjectMap()} to yield true.
     *
     * @return
     */
    default RefObjectMap asRefObjectMap() {
        return as(RefObjectMap.class);
    }

    /**
     * R2RML specifies that the condition for an entity to qualify as a "term map" is
     * "Having exactly one of rr:constant, rr:column, rr:template"
     *
     * @return
     */
    default boolean qualifiesAsTermMap() {
        return hasProperty(RR.constant) || hasProperty(RR.column) || hasProperty(RR.template);
    }

    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    default TermMap asTermMap() {
        return as(TermMap.class);
    }
}
