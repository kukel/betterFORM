/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

dojo.provide("betterform.ui.common.ToolTipAlert");

dojo.require("dojo.NodeList-fx");

dojo.declare("betterform.ui.common.ToolTipAlert",
        betterform.ui.common.Alert,
{
    displayDuration:3000,
    hideSpeed:1000,

    _show:function(id, commonChild,action) {
        //console.debug("ToolTipAlert._show: [id:" + id , " commonChild: " + commonChild + "]");
        var commonChildNode = dojo.byId(id + '-' + commonChild);

        if(commonChild != undefined && commonChild == this.hint) {
            this._render(id, commonChild,"inline");
        }
        else if(commonChildNode != undefined && commonChild == this.alert) {
            // console.debug("ToolTipAlert._show: [id:" + id , " commonChildNode: " + commonChildNode + "]");

            var toolTipId = id+"-MasterToolTip-" +commonChild;
            var alertTooltip = dijit.byId(toolTipId);

            var valueNode = dojo.query('.xfValue', dojo.byId(id))[0];
            if(alertTooltip == undefined) {
                alertTooltip = new dijit._MasterTooltip({id:toolTipId});


                dojo.connect(alertTooltip, "onClick", this, dojo.hitch(this, function() {
                        alertTooltip.hide(valueNode);
                }));
            }


            var controlValue = dijit.byId(id+"-value");
            if (controlValue != undefined) {

            	var controlValueIsEmpty = (!(typeof controlValue.getControlValue === 'function') || controlValue.getControlValue() == undefined || controlValue.getControlValue() == '') && !(dojo.hasClass(controlValue.domNode, "xsdBoolean"));
            	if (controlValueIsEmpty) {
            		return;
            	} else {
            		// Something with datetime...
            		console.debug("Unexpected value?!" + controlValue.getControlValue());	
            	}
            }

            if (controlValue != undefined && controlValue != null) {
            	alertTooltip.show(commonChildNode.innerHTML, dojo.byId(id+"-value"));
                dojo.style(alertTooltip.domNode, "opacity", "1");
                dojo.style(alertTooltip.domNode, "cursor", "pointer");
                dojo.addClass(alertTooltip.domNode, "bfToolTipAlert");
            	//dojo.addClass(controlValue.domNode, "bfInvalidControl");
            } else {
            	console.warn("ToolTipAlert._show: NOT Showing: " + dojo.byId(id+"-label"));
            	// FIX ME!!! Needs to be done in hide as well... And moved to Alert.js maybe?
            	// And put at 'label' and make sure label is not to wide??? Or is that a design issue?
            	// IT IS
            	//alertTooltip.show(commonChildNode.innerHTML, dojo.byId(id+"-label"));
            	//dojo.addClass(dojo.byId(id), "bfInvalidControl");
            }

        if (action == "applyChanges" && (!controlValueIsEmpty || dojo.hasClass(controlValue.domNode, "xsdBoolean"))) {
            setTimeout(dojo.hitch(this,function() {this._fadeOutAndHide(id,commonChild)}),this.displayDuration);
          }

        }
    },


    _hide:function(id, commonChild,action) {
        // console.debug("ToolTipAlert._hide: [id:" + id , " commonChild: " + commonChild + "]");
        var commonChildNode = dojo.byId(id + '-' + commonChild);


        if (commonChildNode != undefined && commonChild == this.alert) {
            var controlValue = dojo.query('.xfValue', dojo.byId(id))[0];
            var alertDijit = dijit.byId(id+"-MasterToolTip-" +commonChild);
            if (alertDijit != undefined && controlValue != undefined) {
                alertDijit.hide(controlValue);
            }
            if(dojo.hasClass(controlValue,"bfInvalidControl")) {
                dojo.removeClass(controlValue,"bfInvalidControl");
            }

        } else if (commonChild != undefined && commonChild == this.hint) {
            this._render(id, commonChild,"none");
        }
    },

    _render:function(id, commonChild, show) {
        // console.debug("ToolTipAlert._render [id:'",id,"' commonChild:'", commonChild," ' show:'",show, "']");
        var mip = dojo.byId(id + "-" + commonChild);
        if (mip != undefined && mip.innerHTML != '') {
            dojo.style(mip, "display", show);
        } else {
            console.info(id + "-" + commonChild + " is not defined for Control " + id);
        }
    },



    _fadeOutAndHide:function(id,commonChild) {
        var alertTooltip = dijit.byId(id+"-MasterToolTip-" +commonChild);
        // No need to check if tooltip exists since this function is only called if (after a check before) it exists
        var valueNode = dojo.query('.xfValue', dojo.byId(id))[0];
        // console.debug("ToolTipAlert._fadeOutAndHide  [id: " + id + " - alertTooltip:" , alertTooltip ,"]");
        var speed = this.hideSpeed;
        dojo.fadeOut({
            node:alertTooltip.domNode,
            duration:speed,
            onEnd:function() {
                alertTooltip.hide(valueNode);
    	}
        }).play();
    }



});
