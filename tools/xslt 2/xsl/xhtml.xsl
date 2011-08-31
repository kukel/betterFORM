<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (c) 2010. betterForm Project - http://www.betterform.de
  ~ Licensed under the terms of BSD License
  -->

<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xf="http://www.w3.org/2002/xforms"
    xmlns:bf="http://betterform.sourceforge.net/xforms"
    exclude-result-prefixes="xf bf xsl"
    xpath-default-namespace="http://www.w3.org/1999/xhtml">

    <xsl:import href="common.xsl"/>
    <xsl:include href="html-form-controls.xsl"/>
    <xsl:include href="ui.xsl"/>

    <!-- ####################################################################################################### -->
    <!-- This stylesheet transcodes a XTHML2/XForms input document to HTML 4.0.                                  -->
    <!-- It serves as a reference for customized stylesheets which may import it to overwrite specific templates -->
    <!-- or completely replace it.                                                                               -->
    <!-- This is the most basic transformator for HTML browser clients and assumes support for HTML 4 tagset     -->
    <!-- but does NOT rely on javascript.                                                                        -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <!-- ############################################ PARAMS ################################################### -->
    <!--
    contextroot - the name of the webapp context (default: 'betterform'
    -->
    <xsl:param name="contextroot" select="''"/>

    <!--
    sessionKey - the XForms session identifier used by the processor
    -->
    <xsl:param name="sessionKey" select="''"/>

    <!--
    baseURI - the baseURI of the document to be transformed by this stylesheet
    -->
    <xsl:param name="baseURI" select="''"/>

    <!-- ### this url will be used to build the form action attribute ### -->
    <xsl:param name="action-url" select="'http://localhost:8080/betterform/XFormsServlet'"/>

    <xsl:param name="form-id" select="'betterform'"/>
    <xsl:param name="form-name" select="//title"/>
    <xsl:param name="debug-enabled" select="'true'"/>

    <!-- ### specifies the parameter prefix for repeat selectors ### -->
    <xsl:param name="selector-prefix" select="'s_'"/>


    <!-- will be set by config and passed from WebProcessor -->
    <xsl:param name="resourcesPath" select="'/bfResources/'"/>

    <!-- locale Parameter -->
    <xsl:param name="locale" select="'en'"/>

     <!-- ############################################ VARIABLES ################################################ -->
    <!-- path to core CSS file -->
    <xsl:variable name="CSSPath" select="concat($resourcesPath,'styles/')"/>


    <xsl:variable name="data-prefix" select="'d_'"/>
    <xsl:variable name="trigger-prefix" select="'t_'"/>
    <xsl:variable name="remove-upload-prefix" select="'ru_'"/>


    <!-- ### checks, whether this form uses uploads. Used to set form enctype attribute ### -->
    <xsl:variable name="uses-upload" select="boolean(//*/xf:upload)"/>

    <!-- ### checks, whether this form makes use of date types and needs datecontrol support ### -->
    <!-- this is only an interims solution until Schema type and base type handling has been clarified -->
    <xsl:variable name="uses-dates">
        <xsl:choose>
            <xsl:when test="boolean(//bf:data/bf:type='date')">true()</xsl:when>
            <xsl:when test="boolean(//bf:data/bf:type='dateTime')">true()</xsl:when>
            <xsl:when test="boolean(substring-after(//bf:data/bf:type,':') ='date')">true()</xsl:when>
            <xsl:when test="boolean(substring-after(//bf:data/bf:type,':') ='dateTime')">true()</xsl:when>
            <xsl:otherwise>false()</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

	<!-- ### checks, whether this form makes use of <textarea xf:mediatype='text/html'/> ### -->
	<xsl:variable name="uses-html-textarea" select="boolean(//xf:textarea[@mediatype='text/html'])"/>

    <!-- ### the CSS stylesheet to use ### -->
    <xsl:variable name="default-css" select="concat($contextroot,$CSSPath,'xforms-basic.css')"/>
    <xsl:variable name="betterform-css"  select="concat($contextroot,$CSSPath,'betterform-basic.css')"/>

    <xsl:variable name="default-hint-appearance" select="'bubble'"/>

    <xsl:output method="xhtml" version="1.0" encoding="UTF-8" indent="no"/>
    <!-- ### transcodes the XHMTL namespaced elements to HTML ### -->
    <!--<xsl:namespace-alias stylesheet-prefix="xhtml" result-prefix="#default"/>-->

    <xsl:strip-space elements="*"/>

    <!-- ####################################################################################################### -->
    <!-- ##################################### TEMPLATES ####################################################### -->
    <!-- ####################################################################################################### -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="html">
        <html>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="head">
        <head>
            <title>
                <xsl:value-of select="$form-name"/>
            </title>
            <!-- copy all meta tags except 'contenttype' -->
            <xsl:call-template name="getMeta" />

            <!-- copy base if present -->
            <xsl:if test="base">
                <base>
                    <xsl:attribute name="href">
                        <xsl:value-of select="base/@href"/>
                    </xsl:attribute>
                </base>
            </xsl:if>

            <!-- include betterForm default stylesheet -->
            <!-- todo: pull these together and change to LessCSS -->
            <link rel="stylesheet" type="text/css" href="{$default-css}"/>
            <link rel="stylesheet" type="text/css" href="{$betterform-css}"/>

            <!-- copy user-defined stylesheets and inline styles -->
            <xsl:call-template name="getLinkAndStyle"/>
        </head>
    </xsl:template>

    <xsl:template match="body">
        <body>
            <xsl:copy-of select="@*"/>
<!--
            <div id="bfLoading">
                <img src="{concat($contextroot, $scriptPath, '../images/indicator.gif')}" class="disabled" id="indicator" alt="loading" />
            </div>
-->

            <xsl:call-template name="build-form-tag"/>

            <div id="bfCopyright">
                <xsl:text disable-output-escaping="yes">&amp;copy; 2011 betterForm Project</xsl:text>
            </div>
            <div id="messagePane"/>
        </body>
    </xsl:template>

    <xsl:template name="build-form-tag">
        <xsl:variable name="outermostNodeset"
                      select=".//xf:*[not(xf:model)][not(ancestor::xf:*)]"/>

        <!-- detect how many outermost XForms elements we have in the body -->
        <xsl:choose>
            <xsl:when test="count($outermostNodeset) = 1">
                <!-- match any body content and defer creation of form tag for XForms processing.
                This option allows to mix HTML forms with XForms markup. -->
                <!-- todo: issue to revisit: this obviously does not work in case there's only one xforms control in the document. In that case the necessary form tag is not written. -->
                <!-- hack solution: add an output that you style invisible to the form to make it work again. -->
                <xsl:apply-templates mode="inline"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- in case there are multiple outermost xforms elements we are forced to create
          the form tag for the XForms processing.-->
                <xsl:call-template name="createForm"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xf:group[not(ancestor::xf:*)][1] | xf:repeat[not(ancestor::xf:*)][1] | xf:switch[not(ancestor::xf:*)][1]" mode="inline">
        <xsl:element name="form">
            <xsl:attribute name="name">
                <xsl:value-of select="$form-id"/>
            </xsl:attribute>

            <xsl:attribute name="action">
                    <xsl:value-of select="concat($action-url,'?sessionKey=',$sessionKey)"/>
            </xsl:attribute>

            <xsl:attribute name="method">POST</xsl:attribute>
            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
            <xsl:if test="$uses-upload">
                <xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
            </xsl:if>
            <input type="submit" value="refresh page" class="bfRefreshButton"/>

            <xsl:apply-templates select="."/>
            <input type="submit" value="refresh page" class="bfRefreshButton"/>
        </xsl:element>
    </xsl:template>

    <!-- this template is called when there's no single outermost XForms element meaning there are
     several blocks of XForms markup scattered in the body of the host document. -->
    <xsl:template name="createForm">
        <xsl:element name="form">
            <xsl:attribute name="name">
                <xsl:value-of select="$form-id"/>
            </xsl:attribute>

            <xsl:attribute name="action">
                <xsl:value-of select="concat($action-url,'?sessionKey=',$sessionKey)"/>
            </xsl:attribute>
            <xsl:attribute name="method">POST</xsl:attribute>
            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
            <xsl:if test="$uses-upload">
                <xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
            </xsl:if>
            <input type="hidden" id="bfSessionKey" name="sessionKey" value="{$sessionKey}"/>

            <xsl:for-each select="*">
                <xsl:apply-templates select="."/>
            </xsl:for-each>

        </xsl:element>
    </xsl:template>

    <!-- ######################################################################################################## -->
    <!-- #####################################  CONTROLS ######################################################## -->
    <!-- ######################################################################################################## -->

    <xsl:template match="xf:input|xf:range|xf:secret|xf:select|xf:select1|xf:textarea|xf:upload">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="control-classes">
            <xsl:call-template name="assemble-control-classes"/>
        </xsl:variable>
        <xsl:variable name="label-classes">
            <xsl:call-template name="assemble-label-classes"/>
        </xsl:variable>

        <span id="{$id}" class="{$control-classes}">
			<xsl:if test="@style">
				<xsl:attribute name="style"><xsl:value-of select="@style"/></xsl:attribute>
			</xsl:if>
            <label for="{$id}-value" id="{$id}-label" class="{$label-classes}"><xsl:apply-templates select="xf:label"/></label>
            <xsl:call-template name="buildControl"/>
        </span>
    </xsl:template>

    <!-- cause outputs can be inline they should not use a block element wrapper -->
    <xsl:template match="xf:output">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="control-classes">
            <xsl:call-template name="assemble-control-classes"/>
        </xsl:variable>
        <xsl:variable name="label-classes">
            <xsl:call-template name="assemble-label-classes"/>
        </xsl:variable>

        <span id="{$id}" class="{$control-classes}">
			<label for="{$id}-value" id="{$id}-label" class="{$label-classes}"><xsl:apply-templates select="xf:label"/></label>
            <xsl:call-template name="buildControl"/>
        </span>
    </xsl:template>

    <xsl:template match="xf:output[string-length(xf:label)!=0]">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="control-classes">
            <xsl:call-template name="assemble-control-classes"/>
        </xsl:variable>
        <xsl:variable name="label-classes">
            <xsl:call-template name="assemble-label-classes"/>
        </xsl:variable>

        <span id="{$id}" class="{$control-classes}">
			<label for="{$id}-value" id="{$id}-label" class="{$label-classes}"><xsl:apply-templates select="xf:label"/></label>
            <xsl:call-template name="buildControl"/>
        </span>
    </xsl:template>

    <xsl:template match="xf:trigger|xf:submit">
        <xsl:variable name="control-classes">
            <xsl:call-template name="assemble-control-classes"/>
        </xsl:variable>

        <xsl:call-template name="trigger">
            <xsl:with-param name="classes" select="$control-classes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- ######################################################################################################## -->
    <!-- #####################################  CHILD ELEMENTS ################################################## -->
    <!-- ######################################################################################################## -->

    <!-- ### handle label ### -->
    <xsl:template match="xf:label">
        <!-- match all inline markup and content -->
        <xsl:apply-templates/>

        <!-- check for requiredness -->
        <xsl:if test="../bf:data/@bf:required='true'"><span class="required-symbol">*</span></xsl:if>
    </xsl:template>

    <!-- ### handle hint ### -->
    <xsl:template match="xf:hint">
        <div id="{@id}-hint" class="xfHint">
            <xsl:value-of select="normalize-space(.)"/>
            <xsl:apply-templates select="../xf:help"/>
        </div>
    </xsl:template>

    <!-- ### handle help ### -->
    <!-- ### only reacts on help elements with a 'src' attribute and interprets it as html href ### -->
    <xsl:template match="xf:help">

        <!-- help in repeats not supported yet due to a cloning problem with help elements -->
        <xsl:if test="string-length(.) != 0 and not(ancestor::xf:repeat)">
            <div id="{concat(@id,'-help')}" class="xfHelp">
                <xsl:element name="a">
                    <xsl:attribute name="href">#</xsl:attribute>
                    <xsl:attribute name="style">text-decoration:none;</xsl:attribute>
                    <xsl:attribute name="class">help-icon</xsl:attribute>
                    ?
                    <!--<img src="{concat($contextroot,'/bfResources/images/help.png')}" class="help-symbol" alt="?" border="0"/>-->
                    <span id="{../@id}-helptext" class="xfHelpText">
                        <xsl:apply-templates/>
                    </span>
                </xsl:element>
            </div>
        </xsl:if>

    </xsl:template>

    <!-- ### handle explicitely enabled alert ### -->
    <!--    <xsl:template match="xf:alert[../bf:data/@bf:valid='false']">-->
    <xsl:template match="xf:alert">
        <xsl:if test="../bf:data/@bf:valid='false'">
            <span id="{../@id}-alert" class="xfAlert">
                <xsl:value-of select="."/>
            </span>
        </xsl:if>
    </xsl:template>

    <!-- ####################################################################################################### -->
    <!-- #####################################  HELPER TEMPLATES '############################################## -->
    <!-- ####################################################################################################### -->

    <xsl:template name="buildControl">
        <xsl:choose>
            <xsl:when test="local-name()='input'">
                <xsl:call-template name="input"/>
                    <xsl:apply-templates select="xf:alert"/>
                    <xsl:apply-templates select="xf:hint"/>
                    <!--<xsl:apply-templates select="xf:help"/>-->
                    <!--<div class="bfRefreshBtn"></div>-->
            </xsl:when>
            <xsl:when test="local-name()='output'">
                <xsl:call-template name="output"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='range'">
                <xsl:call-template name="range"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='secret'">
                <xsl:call-template name="secret"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select'">
                <xsl:call-template name="select"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select1'">
                <xsl:call-template name="select1"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='submit'">
                <xsl:call-template name="submit"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='trigger'">
                <xsl:call-template name="trigger"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='textarea'">
                <xsl:call-template name="textarea"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='upload'">
                <xsl:call-template name="upload"/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='repeat'">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:when test="local-name()='group'">
                <xsl:apply-templates select="."/>
                <xsl:apply-templates select="xf:help"/>
                <xsl:apply-templates select="xf:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='switch'">
                <xsl:apply-templates select="."/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
