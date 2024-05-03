package org.aksw.r2rmlx.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rmlx.vocab.RRX;
import org.aksw.rmltk.model.r2rml.MappingComponent;

@ResourceView
public interface Constraint
    extends MappingComponent
{
    default boolean qualifiesAsPrefixConstraint() {
        return hasProperty(RRX.prefix);
    }

    default PrefixConstraint asPrefixConstraint() {
        return as(PrefixConstraint.class);
    }


    default boolean qualifiesAsRangeConstraint() {
        return hasProperty(RRX.min) || hasProperty(RRX.max);
    }

    default RangeConstraint asRangeConstraint() {
        return as(RangeConstraint.class);
    }
}
