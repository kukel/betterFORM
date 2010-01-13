/* Copyright 2008 - Joern Turner, Lars Windauer */

package de.betterform.xml.xpath.impl.saxon;

import java.util.List;
import java.util.Map;

import de.betterform.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * @author Nick Van den Bleeken
 * @version $Id$
 */
public class BetterFormXPathContext {
    private final List nodeset;
    private final int position;
    private final Map prefixMapping;
    private final XPathFunctionContext xpathFunctionContext;
    
    /**
     * @param nodeset
     * @param position
     * @param prefixMapping
     * @param xpathFunctionContext
     */
    public BetterFormXPathContext(List nodeset, int position, Map prefixMapping, XPathFunctionContext xpathFunctionContext) {
	this.nodeset = nodeset;
	this.position = position;
	this.prefixMapping = prefixMapping;
	this.xpathFunctionContext = xpathFunctionContext;
    }

    /**
     * @return the nodeset
     */
    public List getNodeset() {
        return nodeset;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return the prefixMapping
     */
    public Map getPrefixMapping() {
        return prefixMapping;
    }

    /**
     * @return the xpathFunctionContext
     */
    public XPathFunctionContext getXPathFunctionContext() {
        return xpathFunctionContext;
    }
    
    
}
