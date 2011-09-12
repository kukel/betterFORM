/*
 * Copyright (c) 2011. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.xml.xforms.xpath.saxon.function;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionVisitor;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import de.betterform.xml.ns.BetterFormNamespaceMap;
import de.betterform.xml.ns.NamespaceConstants;
import de.betterform.xml.xforms.Container;
import de.betterform.xml.xforms.exception.XFormsException;
import de.betterform.xml.xforms.ui.Repeat;
import de.betterform.xml.xpath.impl.saxon.XPathCache;

import java.util.List;

public class Index extends XFormsFunction
{
    /**
     * Pre-evaluate a function at compile time. Functions that do not allow
     * pre-evaluation, or that need access to context information, can override this method.
     * @param visitor an expression visitor
     * @return the result of the early evaluation, or the original expression, or potentially
     * a simplified expression
     */

    public Expression preEvaluate(ExpressionVisitor visitor) throws XPathException {
	return this;
    }

	/**
	 * Evaluate in a general context
	 */
	public Item evaluateItem(XPathContext xpathContext) throws XPathException
	{
		final String idref = argument[0].evaluateAsString(xpathContext).toString();
		
		XPathFunctionContext functionContext = getFunctionContext(xpathContext);
		

		if (functionContext != null)
		{
			Container container = functionContext.getXFormsElement().getContainerObject();
			if (container != null)
			{
				Object object = container.lookup(idref);
		        if (object != null && object instanceof Repeat) {
		            // get current index
		            return Int64Value.makeIntegerValue(((Repeat) object).getIndex());
		        }
		        
		     // check repeat element
		        List nodeset;
			try {
//			    nodeset = XPathCache.getInstance().evaluate(container.getRootNodeInfo(), "//" + NamespaceConstants.XFORMS_PREFIX + ":repeat[@id='" + idref + "'] | //*[@id='" + idref + "'][@" + NamespaceConstants.XFORMS_PREFIX + ":repeat-nodeset or @" + NamespaceConstants.XFORMS_PREFIX + ":repeat-bind]", BetterFormNamespaceMap.kNAMESPACE_MAP, functionContext);
			    nodeset = XPathCache.getInstance().evaluate(container.getHostContext("").getNodeset(),1, "//" + NamespaceConstants.XFORMS_PREFIX + ":repeat[@id='" + idref + "'] | //*[@id='" + idref + "'][@" + NamespaceConstants.XFORMS_PREFIX + ":repeat-nodeset or @" + NamespaceConstants.XFORMS_PREFIX + ":repeat-bind]", BetterFormNamespaceMap.kNAMESPACE_MAP, functionContext);
			} catch (XFormsException e) {
			    throw (XPathException)e.getCause();
			}
		        
		        return (nodeset.size() == 1)?Int64Value.makeIntegerValue(1):DoubleValue.NaN;
			}
		}
		// XXX dispatch compute exception
		return null;
	}
}
