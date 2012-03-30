dojo.provide("betterform.ui.common.GrowlAlert");

dojo.declare("betterform.ui.common.GrowlAlert",
        null,
{

    position: undefined,
    stack: undefined, 


    // Before Dojo begins templatizing the HTML, we setup the container
    constructor: function(params) {
        // Combine passed parameters with default params before creating template
        dojo.mixin(this, params);
    },

    handleValid:function(id,action){

        var alertDijit = undefined;
        alertDijit = dijit.byId(id + "-Growl-alert");

        if (alertDijit != undefined) {
            alertDijit.close();
        }

        var control = undefined;
	if (action == "hideContainerAlert") {
		control = dijit.byId(id); // Might be a repeat or group
	} else {
        	control = dijit.byId(id+"-value");
	}

        if(dojo.hasClass(control.domNode,"bfInvalidControl")){
            dojo.removeClass(control.domNode, "bfInvalidControl");
        }

    },

    handleInvalid:function(id,action){

        var alertGrowl = dijit.byId(id+"-Growl-alert");
        var alert = dojo.byId(id + '-alert');
        var label = dojo.byId(id + '-label');
        var control = dijit.byId(id);
        //console.debug("betterform.ui.common.GrowlAlert.invalid [id:" + id , " action: " + action + " position: " + this.position + " stack: " + this.stack +"]");
        if (alertGrowl == undefined && alert != undefined) {
	    if (label!=undefined) {
                alertGrowl = new betterform.ui.common._Growl({id:id+"-Growl-alert", control:control, controlLabel:label.innerHTML, message:alert.innerHTML, position:this.position, stack:this.stack});
            } else {
                alertGrowl = new betterform.ui.common._Growl({id:id+"-Growl-alert", controlLabel:"", message:alert.innerHTML, position:this.position, stack:this.stack});
            }
        }


        if (alert != undefined) {
            if(action == "xfDisabled") {
                alertGrowl.close();
            }
var controlValue = '';
if (typeof control.getControlValue === 'function') {
	controlValue = control.getControlValue();
}

            //if(action=="onFocus" || ((action=="onBlur" || action=="submitError") && controlValue != '')){
            if(action=="onFocus" || (action=="onBlur" && controlValue != '')){
                alertGrowl.show();
            }
            else  if(action=="applyChanges" && (controlValue != '' || dojo.hasClass(control.domNode,"xsdBoolean"))){

                alertGrowl.show();
                dojo.addClass(control.domNode, "bfInvalidControl");
            }
            else  if(action=="showContainerAlert"){
                alertGrowl.show();
                dojo.addClass(control.domNode, "bfInvalidControl");
            }
            else {
            //    alertGrowl.close();
            }
        }
    }

});
