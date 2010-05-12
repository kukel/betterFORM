xquery version "1.0";
declare option exist:serialize "method=xhtml media-type=text/xml";

request:set-attribute("betterform.filter.parseResponseBody", "true"),
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:xf="http://www.w3.org/2002/xforms"
      xmlns:ev="http://www.w3.org/2001/xml-events"
      xml:lang="en">
    <head>
        <title>betterFORM Demo XForms: Address, Registration, FeatureExplorer</title>

        <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojox/grid/resources/Grid.css"/>
        <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojox/grid/resources/tundraGrid.css"/>
        <link rel="stylesheet" type="text/css" href="/exist/resources/styles/bf.css"/>
        <link rel="stylesheet" type="text/css" href="/exist/resources/styles/demo.css"/>
        <link rel="stylesheet" type="text/css"
              href="/exist/rest/db/betterform/apps/timetracker/resources/InlineRoundBordersAlert.css"/>
        <link rel="stylesheet" type="text/css"
              href="/exist/rest/db/betterform/apps/timetracker/resources/timetracker.css"/>

       
        <script type="text/javascript">
            <!--
            dojo.require("dojo.parser");
            dojo.require("dijit.dijit");
            dojo.require("dijit.Declaration");
            dojo.require("dijit.Toolbar");
            dojo.require("dijit.ToolbarSeparator");
            dojo.require("dijit.Dialog");
            dojo.require("dijit.TitlePane");
            dojo.require("betterform.ui.container.Group");
            dojo.require('dijit.layout.ContentPane');
            dojo.require("dijit.form.Button");

            var xfReadySubscribers;

            function embed(targetTrigger,targetMount){
                console.debug("embed",targetTrigger,targetMount);
                if(targetMount == "embedDialog"){
                    dijit.byId("taskDialog").show();
                }

                var targetMount =  dojo.byId(targetMount);

                fluxProcessor.dispatchEvent(targetTrigger);

                if(xfReadySubscribers != undefined) {
                    dojo.unsubscribe(xfReadySubscribers);
                    xfReadySubscribers = null;
                }

                xfReadySubscribers = dojo.subscribe("/xf/ready", function(data) {
                    dojo.style(targetMount, "opacity", 0);
                    dojo.fadeIn({
                        node: targetMount,
                        duration:100
                    }).play();
                });
                dojo.fadeOut({
                    node: targetMount,
                    duration:100,
                    onBegin: function() {
                        fluxProcessor.dispatchEvent(targetTrigger);
                    }
                }).play();

            }

            var editSubcriber = dojo.subscribe("/task/edit", function(data){
                fluxProcessor.setControlValue("currentTask",data);
                embed('editTask','embedDialog');

            });

            var deleteSubscriber = dojo.subscribe("/task/delete", function(data){
                var check = confirm("Really delete this entry??");
                if (check == true){
                    fluxProcessor.setControlValue("currentTask",data);
                    fluxProcessor.dispatchEvent("deleteTask");
                }
            });

            var refreshSubcriber = dojo.subscribe("/task/refresh", function(){
                fluxProcessor.dispatchEvent("overviewTrigger");
            });
            // -->
        </script>


    </head>
    <body id="timetracker" class="tundra InlineRoundBordersAlert">

        <div class="page">

            <!-- ***** hidden triggers ***** -->
            <!-- ***** hidden triggers ***** -->
            <!-- ***** hidden triggers ***** -->
            <div style="display:none;">
                <xf:model id="model-1">
                    <xf:instance>
                        <data xmlns="">
                            <project/>
                            <default-duration>30</default-duration>
                            <from>2000-01-01</from>
                            <to>2000-01-02</to>
                            <billable>false</billable>
                            <currentTask/>
                        </data>
                    </xf:instance>
                    <xf:bind nodeset="default-duration" type="xf:integer"/>
                    <xf:bind nodeset="from" type="xf:date"/>
                    <xf:bind nodeset="to" type="xf:date" />
                    <xf:bind nodeset="billable" type="xf:boolean"/>

                    <xf:submission id="s-query-tasks"
                                    resource="/exist/rest/db/betterform/apps/timetracker/views/list-items.xql"
                                    method="get"
                                    replace="embedHTML"
                                    targetid="embedInline"
                                    ref="instance()"
                                    validate="false">
                        <xf:action ev:event="xforms-submit-error">
                            <xf:message>Submission failed</xf:message>
                        </xf:action>
                    </xf:submission>

                    <xf:instance id="i-project" src="/exist/rest/db/betterform/apps/timetracker/data/project.xml" />

                    <xf:submission id="s-delete-task"
                                    method="delete"
                                    replace="none"
                                    validate="false">
                        <xf:resource value="concat('/exist/rest/db/betterform/apps/timetracker/data/task/',currentTask,'.xml')"/>
                        <xf:header>
                            <xf:name>username</xf:name>
                            <xf:value>admin</xf:value>
                        </xf:header>
                        <xf:header>
                            <xf:name>password</xf:name>
                            <xf:value>betterform</xf:value>
                        </xf:header>
                        <xf:header>
                            <xf:name>realm</xf:name>
                            <xf:value>exist</xf:value>
                        </xf:header>
                        
                        <xf:action ev:event="xforms-submit-done">
                            <script type="text/javascript">
                                fluxProcessor.dispatchEvent("overviewTrigger");
                            </script>
                            <xf:message level="ephemeral">Entry has been removed</xf:message>
                        </xf:action>
                    </xf:submission>



                    <xf:action ev:event="xforms-ready">
                        <xf:setvalue ref="to" value="substring(local-date(), 1, 10)"/>
                        <xf:recalculate/>
                        <xf:setvalue ref="from" value="days-to-date(number(days-from-date(instance()/to) - instance()/default-duration))"/>
                    </xf:action>

                     <!-- ***************************
                    Commented out but might still be useful as reference - shows REST-style access

                    <xf:instance id="i-query">
                        <data xmlns="">
                            <_query>//task</_query>
                            <_howmany/>
                            <_xsl>/db/betterform/apps/timetracker/views/list-items.xsl</_xsl>
                        </data>
                    </xf:instance>

                    <xf:submission id="s-query-tasks-rest"
                                    resource="/exist/rest/db/betterform/apps/timetracker/data/task"
                                    method="get"
                                    replace="embedHTML"
                                    targetid="embedInline"
                                    ref="instance('i-query')"
                                    validate="false">
                        <xf:action ev:event="xforms-submit-done">
                            <xf:refresh/>
                        </xf:action>
                    </xf:submission>
                    ****************************** -->
                </xf:model>

                <xf:trigger id="overviewTrigger">
                    <xf:label>Overview</xf:label>
                    <xf:send submission="s-query-tasks"/>
                </xf:trigger>

                <xf:trigger id="addTask">
                    <xf:label>new</xf:label>
                    <xf:action>
                        <xf:load show="embed" targetid="embedDialog">
                            <xf:resource
                                    value="'/exist/rest/db/betterform/apps/timetracker/edit/edit-item.xql#xforms'"/>
                        </xf:load>
                    </xf:action>
                </xf:trigger>

                <xf:trigger id="editTask">
                    <xf:label>new</xf:label>
                    <xf:action>
                        <xf:load show="embed" targetid="embedDialog">
                            <xf:resource
                                    value="concat('/exist/rest/db/betterform/apps/timetracker/edit/edit-item.xql#xforms?timestamp=',instance()/currentTask)"/>
                        </xf:load>
                    </xf:action>
                </xf:trigger>

                <xf:trigger id="deleteTask">
                    <xf:label>delete</xf:label>
                    <xf:send submission="s-delete-task"/>
                </xf:trigger>


                <xf:input id="currentTask" ref="instance()/currentTask">
                    <xf:label>This is just a dummy used by JS</xf:label>
                </xf:input>
            </div>


            <!-- ######################### Content here ################################## -->
            <!-- ######################### Content here ################################## -->
            <!-- ######################### Content here ################################## -->
            <!-- ######################### Content here ################################## -->
            <!-- ######################### Content here ################################## -->
            <div id="content">

                <div id="toolbar" dojoType="dijit.Toolbar">
                    <div id="overviewBtn" dojoType="dijit.form.DropDownButton" showLabel="true"
                         onclick="fluxProcessor.dispatchEvent('overviewTrigger');">
                        <span>Filter</span>
                        <div id="filterPopup" dojoType="dijit.TooltipDialog">
                            <table id="searchBar">
                                <tr>
                                    <td>
                                        <xf:input ref="from" incremental="true">
                                            <xf:label>from</xf:label>
                                            <xf:action ev:event="xforms-value-changed">
                                                <xf:dispatch name="DOMActivate" targetid="overviewTrigger"/>
                                            </xf:action>
                                        </xf:input>
                                    </td>
                                    <td>
                                        <xf:input ref="to" incremental="true">
                                            <xf:label>to</xf:label>
                                            <xf:action ev:event="xforms-value-changed">
                                                <xf:dispatch  name="DOMActivate" targetid="overviewTrigger"/>
                                            </xf:action>
                                        </xf:input>
                                    </td>
                                    <td>
                                        <xf:select1 ref="project" appearance="minimal">
                                            <xf:label>Project</xf:label>
                                            <xf:itemset nodeset="instance('i-project')/*">
                                                <xf:label ref="."/>
                                                <xf:value ref="@id"/>
                                            </xf:itemset>
                                        </xf:select1>
                                    </td>
                                    <td>
                                        <xf:input ref="billable">
                                            <xf:label>Billable</xf:label>
                                        </xf:input>
                                    </td>
                                    <td>
                                        <xf:trigger id="closeFilter">
                                            <xf:label/>
                                            <script type="text/javascript">
                                                dijit.byId("filterPopup").onCancel();
                                            </script>
                                        </xf:trigger>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div id="addBtn" dojoType="dijit.form.Button" showLabel="true"
                         onclick="embed('addTask','embedDialog');">
                        <span>New Task</span>
                    </div>
                    <div id="searchBtn" dojoType="dijit.form.Button" showLabel="true" onclick="alert('todo');">
                        <span>Search</span>
                    </div>
                    <div id="settingsBtn" dojoType="dijit.form.Button" showLabel="true" onclick="alert('todo');">
                        <span>Settings</span>
                    </div>
                </div>

                <img id="shadowTop" src="/exist/rest/db/betterform/apps/timetracker/resources/images/shad_top.jpg" alt=""/>

                <div id="fromTo">
                    <xf:output value="concat(from,' - ',to)" id="durationLabel">
                        <xf:label/>
                    </xf:output>
                </div>

                <div id="taskDialog" dojotype="dijit.Dialog" style="width:610px;height:480px;" title="Task">
                    <div id="embedDialog"></div>
                </div>

                <div id="embedInline"></div>

                <!-- ######################### Content end ################################## -->
                <!-- ######################### Content end ################################## -->
                <!-- ######################### Content end ################################## -->
                <!-- ######################### Content end ################################## -->
                <!-- ######################### Content end ################################## -->
            </div>
        </div>
        <!-- ######################### Content end ################################## -->
        <!-- ######################### Content end ################################## -->
        <!-- ######################### Content end ################################## -->
        <!-- ######################### Content end ################################## -->
        <!-- ######################### Content end ################################## -->

    </body>
</html>
