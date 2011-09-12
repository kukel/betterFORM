/*
 * Copyright (c) 2011. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

dojo.provide("betterform.ui.timeline.TimeLine");


dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare(
        "betterform.ui.timeline.TimeLine",
        betterform.ui.ControlValue,
{   timeLine:null,
    t1:null,
    resizeTimerID:null,
    templateString: dojo.cache("betterform", "ui/templates/TimeLine.html"),
    instanceId:null,
    modelId:null,
    adjustTimestamp:false,

    buildRendering:function() {
        console.debug("betterform.ui.timeline.TimeLine.buildRendering srcNode:", this.srcNodeRef);
        this.inherited(arguments);


        // prepare getInstanceDocument() call in postCreate()
        this.instanceId = dojo.attr(this.srcNodeRef, "instanceId");
        this.modelId = dojo.attr(this.srcNodeRef, "modelId");
    },

    postMixInProperties:function() {
        this.applyProperties(dijit.byId(this.xfControlId), this.srcNodeRef);
    },

    postCreate:function() {
        this.inherited(arguments);
        console.debug("TimeLine.postcreate");
        this._createInitialTimeline();
    },

    _createInitialTimeline:function() {
        dijit.byId(this.modelId).getInstanceDocument(this.instanceId,dojo.hitch(this,this._updateTimeLine));
    },

    adjustTimelineToDate:function(currentDate) {
        var tmpDate = null;
        if (currentDate != undefined) {
               tmpDate = Timeline.DateTime.parseIso8601DateTime(currentDate);
        }
        //console.debug("TimeLine.adjustTimelineToDate: ", tmpDate);
        this._adjustAndCreateTimeline(tmpDate);
    },

    _updateTimeLine:function(data) {
        //console.debug("TimeLine._updateTimeLine update event data: ", data);
        // console.dirxml(data);
        this.eventSource = new Timeline.DefaultEventSource();

        var eventList = dojo.query("data event", data);
        event = eventList[eventList.length-1];
        var startDate;
        if(event == undefined) {
            // console.debug("no events defined, switching to now");
            var tmpStart = dojo.query("*[value]", data)[0];
            // console.debug("Start", tmpStart);
            startDate = dojo.attr(tmpStart,"value");;

        }else {
            // console.debug("found event: ", event);
            startDate= dojo.attr(event,"start");
        }
        
        // console.debug("start Date: ", startDate, " this.date: ", this.date, " parsedDate:", Timeline.DateTime.parseIso8601DateTime(startDate));
        this.date = startDate;
        // this.date = Timeline.DateTime.parseIso8601DateTime(startDate);

        // console.debug("Timeline._updateTimeline: Adjust Timeline to: ",startDate, " [",Timeline.DateTime.parseIso8601DateTime(startDate)," ]")
        this._adjustAndCreateTimeline(this.date);
        // console.debug("TimeLine.postCreate created Timeline: ", this.timeLine);
        this.eventSource.loadXML(data,"");
    },

    _adjustAndCreateTimeline:function(currentDate) {
        // console.debug("TimeLine._adjustAndCreateTimeline: ", currentDate);
        // band info properties
        this.timeZone = 0;
        var bandInfoDay = {
                overview:       false,
                intervalUnit:   Timeline.DateTime.DAY,
                eventSource:    this.eventSource,
                date:           currentDate,
                width:          "50%",
                showEventText:  true,
                intervalPixels: 200,
                timeZone:       this.timeZone
            };
        var bandInfoMonth =  {
                overview:       true,
                intervalUnit:   Timeline.DateTime.MONTH,
                eventSource:    this.eventSource,
                date:           currentDate,
                width:          "25%",
                showEventText:  true,
                intervalPixels: 100,
                timeZone:       this.timeZone
            };

        var bandInfoYear = {
                overview:       true,
                intervalUnit:   Timeline.DateTime.YEAR,
                eventSource:    this.eventSource,
                date:           currentDate,
                width:          "25%",
                showEventText:  true,
                intervalPixels: 50,
                timeZone:       this.timeZone
            };

        // console.debug("create BandInfos");
        var bandInfos = [
            Timeline.createBandInfo(bandInfoDay),
            Timeline.createBandInfo(bandInfoMonth),
            Timeline.createBandInfo(bandInfoYear)
        ];


        bandInfos[1].syncWith = 0;
        bandInfos[1].highlight = true;
        // bandInfos[1].eventPainter.setLayout(bandInfos[0].eventPainter.getLayout());
        bandInfos[2].syncWith = 1;
        bandInfos[2].highlight = true;
        // bandInfos[2].eventPainter.setLayout(bandInfos[1].eventPainter.getLayout());

        // console.debug("create aimTimestampValue");
        var aimTimestampValue;
        if(dijit.byId("timestampDijit-value") != undefined) {
            aimTimestampValue = Timeline.DateTime.parseIso8601DateTime(dijit.byId("timestampDijit-value").getControlValue());
        }else {
            console.warn("No Timestamp Found, adjusted timestamp to date  " + currentDate);
            aimTimestampValue =  currentDate;
        }

        if(aimTimestampValue == undefined) {
            aimTimestampValue = this.date;
        }
        // console.debug("aimTimestampValue: ", aimTimestampValue, " this.date: ",this.date);
        for (var i = 0; i < bandInfos.length; i++) {
            bandInfos[i].decorators = [
                new Timeline.PointHighlightDecorator({
                    date:       aimTimestampValue,
                    color:      "#FFC080",
                    opacity:    50,
                    //theme:      theme,
                    cssClass: 'p-highlight1'
                })
            ];
        }
        this.timeLine = Timeline.create(this.timelineNode, bandInfos);

    },
     getControlValue:function() {
        return this.date;    
     },

    _handleSetControlValue:function(value) {
        // console.debug("betterform.ui.timeline.Timeline._handleSetControlValue: value" + value);
        dijit.byId(this.modelId).getInstanceDocument(this.instanceId,dojo.hitch(this,this._updateTimeLine))
    },

    _onResize:function() {
        if (this.resizeTimerID == null) {
            this.resizeTimerID = window.setTimeout(function() {
                this.resizeTimerID = null;
                this.timeLine.layout();
            }, 500);
        }
    }
});


