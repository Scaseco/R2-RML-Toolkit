package org.aksw.r2rmlx.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;
import org.aksw.rmltk.model.r2rml.TermMap;

public interface TermMapX
    extends TermMap
{
    @Iri(R2RMLXStrings.languageColumn)
    String getLangColumn();
    TermMapX setLangColumn(String langColumn);

    @Iri(R2RMLXStrings.constraint)
    Set<Constraint> getConstraints();


    default Constraint addNewConstraint() {
        Constraint result = getModel().createResource().as(Constraint.class);
        getConstraints().add(result);
        return result;
    }
}
