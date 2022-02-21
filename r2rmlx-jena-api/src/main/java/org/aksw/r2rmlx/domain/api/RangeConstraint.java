package org.aksw.r2rmlx.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;
import org.apache.jena.rdf.model.RDFNode;

@ResourceView
public interface RangeConstraint
    extends Constraint
{
    @Iri(R2RMLXStrings.min)
    RDFNode getMin();
    RangeConstraint setMin(RDFNode min);

    @Iri(R2RMLXStrings.max)
    RDFNode getMax();
    RangeConstraint setMax(RDFNode max);

    @Iri(R2RMLXStrings.minInclusive)
    Boolean isMinInclusive();
    RangeConstraint setMinInclusive(Boolean minInclusive);

    @Iri(R2RMLXStrings.maxInclusive)
    Boolean isMaxInclusive();
    RangeConstraint setMaxInclusive(Boolean maxInclusive);
}
