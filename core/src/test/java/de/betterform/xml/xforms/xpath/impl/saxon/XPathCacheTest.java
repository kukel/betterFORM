package de.betterform.xml.xforms.xpath.impl.saxon;

import java.util.List;

import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.om.NodeInfo;
import de.betterform.xml.xforms.BetterFormTestCase;
import de.betterform.xml.xforms.XFormsElement;
import de.betterform.xml.xforms.XFormsProcessorImpl;
import de.betterform.xml.xforms.exception.XFormsException;
import de.betterform.xml.xforms.xpath.saxon.function.XPathFunctionContext;
import de.betterform.xml.xpath.impl.saxon.BetterFormXPathContext;
import de.betterform.xml.xpath.impl.saxon.XPathCache;

public class XPathCacheTest extends BetterFormTestCase {

	private BetterFormXPathContext relativeContext;

	public void testEvaluateAsBooleanBetterFormXPathContextString()
			throws XFormsException {

		String xpathString = ". != instance('originalInstance')/repeated/item[count(current()/parent::*/preceding-sibling::*) + 1]/amount";
		
		defaultFunctionContext =  new XPathFunctionContext(((XFormsProcessorImpl)processor).getContainer().lookup("bind-5"));

		List nodes = evaluateInDefaultContext("/data/repeated/item[1]/amount");

		relativeContext = new BetterFormXPathContext(nodes, 1, kPREFIX_MAPPING,
				defaultFunctionContext);

		assertEquals(Boolean.TRUE, XPathCache.getInstance().evaluateAsBoolean(relativeContext, "boolean("+xpathString+")"));


	}

	@Override
	protected String getTestCaseURI() {
		return "XPathCacheTest.xhtml";
	}

	@Override
	protected XPathFunctionContext getDefaultFunctionContext() {
		// TODO Auto-generated method stub
		
		return null;
	}

}
