package org.aksw.rmltk.model.backbone.rml;

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
public interface ITermSpecRml
    extends ITermSpec
{
}
