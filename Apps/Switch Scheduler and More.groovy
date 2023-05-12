/* Switch Scheduler and More
*  
*	2023 T. K. (kampto)
*	NOTES: Generate a schedule to Automate Lights, Outlets, Switches, Relays, Sprinklers,. 
*   
*    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
*    http://www.apache.org/licenses/LICENSE-2.0  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*
*	Change Revision History:  
*   Ver		Date:		Who:		What:
*   1.0.0	2023-05-09	kampto    	First Build from scratch, BETA Release.
*/

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def titleVersion() { state.name = "Switch Scheduler (Lights, Outlets, Switches, Relays, Sprinklers,..) ";    state.version = "1.0.0" }
definition (
	name: "Switch Scheduler and More", namespace: "kampto", author: "T. K.",
	description: "Automate Switches, Relays, Outlets Sprinklers",
	category: "General",
   	iconUrl: "",
	iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/kampto/Hubitat/main/Apps/Switch-Sprinkler_Scheduler",
    //documentationLink: "https://community.hubitat.com/t/beta-device-active-time-tracker-app-device-on-timer-and-on-counter-with-variables-access/102896"
	)
preferences { page(name: "mainPage") }

////////////////////////////////////////////////////////////  Main Page Inputs/Set-Up /////////////////////////////////////////////////////////////////
def mainPage() {
    if (app.getInstallationState() != "COMPLETE") {hide=false} else {hide=true}  //// ver1.2.0
    if (state.DeVices == null) state.DeVices = [:]
    if (state.DeVicesList == null) state.DeVicesList = []
                        
  dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
  displayTitle() 
   section (getFormat("header","Initial Set-Up:"),hideable: true, hidden: hide){  
        label title: "<b>1. Name this child App</b>", required: true, submitOnChange: true, width: 3
        input "DeVices", "capability.switch", title: "<b>2. Select Devices to Track Switch On Time</b>", required: true, multiple: true, submitOnChange: true, width: 5    
                
     DeVices.each {dev ->
	    if(!state.DeVices["$dev.id"]) {
             state.DeVices["$dev.id"] = [start: dev.currentSwitch == "on" ? now() : 0, total: 0, sun: false, mon: false, tue: false, wed: false, thu: false, fri: false, sat: false, startTime: "00000000000000000000000000000000000", durTime: 0, cron: "", days: "", zone: 0, counts: 0]  
             state.DeVicesList += dev.id   
             }
	 }
   }
  section {                         
     if(DeVices) {
         if(DeVices.id.sort() != state.DeVicesList.sort()) { 
		    state.DeVicesList = DeVices.id
			Map newState = [:]
			DeVices.each{dev ->  newState["$dev.id"] = state.DeVices["$dev.id"]}   //////////////////
            state.DeVices = newState
			}
    //updated()
    paragraph displayTable()
	  ///////////////////////// Input Start Time 
      if(state.newStartTime) {
        input name: "newStartTime", type: "time", title:"<b>Enter Start/On Time, Applies to all checked days for Switch. &nbsp <small>Hit Updater</small>", defaultValue: "", required: false,  submitOnChange:true, width: 5, newLineAfter: true, style: 'margin-left:10px'
          if(newStartTime) {
             state.DeVices[state.newStartTime].startTime = newStartTime
             state.remove("newStartTime")
		     app.removeSetting("newStartTime")
             paragraph "<script>{changeSubmit(this)}</script>"
		     }
		} 
       else if(state.remStartTime) {
		  state.DeVices[state.remStartTime].startTime = ""
          state.remove("remStartTime")  
		  paragraph "<script>{changeSubmit(this)}</script>"
		  }
       endif
       ///////////////////////// Input Run Duration  
       if(state.newDurTime) {
        input name: "newDurTime", type: "number", title:"<b>Enter Run/On Duration in Minutes, Applies to all checked days for Switch. &nbsp <small>Hit Enter</small>", defaultValue: "", required: false,  submitOnChange:true, width: 8, style: 'margin-left:10px'
          if(newDurTime) {
             state.DeVices[state.newDurTime].durTime = newDurTime
             state.remove("newDurTime")
		     app.removeSetting("newDurTime")
             paragraph "<script>{changeSubmit(this)}</script>"
		     }
		  } 
       else if(state.remDurTime) {
		     state.DeVices[state.remDurTime].durTime = ""
             state.remove("remDurTime")  
			 paragraph "<script>{changeSubmit(this)}</script>"
		     } 
       endif
        
       input "refresh", "button", title: "REFRESH Table Times, Counts, & States", width: 5 
       input "allOff", "button", title: "TURN OFF all switches", width: 4 
      }
  }
////////////////////////////////////////////////////////////  Advanced Inputs //////////////////////////////////////////////////////////////////      
   section(getFormat("header","Advanced Options:"),hideable: true, hidden: false) {
        input name: "pauseBool", type: "bool", title: getFormat("important2","<b>Pause all upcoming schedules?</b>"), defaultValue:false, submitOnChange:true, style: 'margin-left:10px' 
        input "remoteSwitch", "capability.switch", title: "<b>Select a Switch to Remotely turn Off all switches and Pause/Resume all schedules (Optional)</b><br><small>Real or Virtual Switch</small>", required: false, multiple: false, submitOnChange: true  
        input name: "formatBool", type: "bool", title: getFormat("important2","<b>Enable Alternative UI formatting, darks screen mode?</b>"), defaultValue:false, submitOnChange:true, style: 'margin-left:10px'
        input name: "logEnableBool", type: "bool", title: getFormat("important2","<b>Enable Logging of App based Resets and Refreshes?</b><br><small>Shuts off in 1hr</small>"), defaultValue:true, submitOnChange:true, style: 'margin-left:10px'
        input "updateButton", "button", title: "Update/Store Schedules without hitting Done exiting App"   
    }
/////////////////////////////////////////////////////////////  Notes Section  /////////////////////////////////////////////////////////////////
   section(getFormat("header","Usage Notes:"), hideable: true, hidden: hide) {   //// ver1.1.5
      paragraph getFormat("lessImportant","<ul>"+    
      "<li>Use for any switch capability; switches, outlets, relays, lights, sprinklers, etc.. Add as many as you want on table.</li>"+
      "<li>For each switch select day(s) check boxes, enter Start time and Run Duration. Start times are in 24 hour format. Run Time in Minutes only.</li>"+
	  "<li>Table will not auto refresh, you must hit refresh button.</li>"+
      "<li>Total On Time and Counts will track all switch activity, from app and outside app.</li>"+ 
      "<li>Select a virtual switch to remotely turn off all switches and pause/resume schedules (optional). Use case EX: Rain delay for spinklers, dashboard access.</li>"+
      "<li>To automate sprinkers you must provide valve voltage (24VAC) thru a switch, outlet(with 24VAC walwart), or relay. </li>"+                    
      "<li>If you change schedules and update any switches while already in a On/Run mode things might get messed up and require manual Off.</li>"+                    
      "<li>You must hit DONE at page bottom to save App.</li>"+                    
      "</ul>")
      }
  }
}
//////////////////////////////////////////////////////////////  Main Table ////////////////////////////////////////////////////////////////////
String displayTable() {
    if(state.resetTotal) {  //// Reset Cumulative time and counts per device Button       
		def dev = DeVices.find{"$it.id" == state.resetTotal}
        state.DeVices[state.resetTotal].start = now()
        state.DeVices[state.resetTotal].total = 0
        state.DeVices[state.resetTotal].counts = 0
        state.remove("resetTotal")
	    }
    /////////////////////// Sunday - Saturday Check Boxes
    if(state.sunCheckedBox) { def dev = DeVices.find{"$it.id" == state.sunCheckedBox}
        state.DeVices[state.sunCheckedBox].sun = true
        state.remove("sunCheckedBox") }
	else if(state.sunUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.sunUnCheckedBox}  
	    state.DeVices[state.sunUnCheckedBox].sun = false
        state.remove("sunUnCheckedBox") }
    endif
    if(state.monCheckedBox) {def dev = DeVices.find{"$it.id" == state.monCheckedBox}  
	    state.DeVices[state.monCheckedBox].mon = true
        state.remove("monCheckedBox") }
	else if(state.monUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.monUnCheckedBox}  
	    state.DeVices[state.monUnCheckedBox].mon = false
        state.remove("monUnCheckedBox") }
    endif
    if(state.tueCheckedBox) {def dev = DeVices.find{"$it.id" == state.tueCheckedBox}  
	    state.DeVices[state.tueCheckedBox].tue = true
        state.remove("tueCheckedBox") }
	else if(state.tueUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.tueUnCheckedBox}  
	    state.DeVices[state.tueUnCheckedBox].tue = false
        state.remove("tueUnCheckedBox") }
    endif
    if(state.wedCheckedBox) {def dev = DeVices.find{"$it.id" == state.wedCheckedBox}  
	    state.DeVices[state.wedCheckedBox].wed = true
        state.remove("wedCheckedBox") }
	else if(state.wedUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.wedUnCheckedBox}  
	    state.DeVices[state.wedUnCheckedBox].wed = false
        state.remove("wedUnCheckedBox") }
    endif
    if(state.thuCheckedBox) {def dev = DeVices.find{"$it.id" == state.thuCheckedBox}  
	    state.DeVices[state.thuCheckedBox].thu = true
        state.remove("thuCheckedBox") }
	else if(state.thuUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.thuUnCheckedBox}  
	    state.DeVices[state.thuUnCheckedBox].thu = false
        state.remove("thuUnCheckedBox") }
    endif
    if(state.friCheckedBox) {def dev = DeVices.find{"$it.id" == state.friCheckedBox}  
	    state.DeVices[state.friCheckedBox].fri = true
        state.remove("friCheckedBox") }
	else if(state.friUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.friUnCheckedBox}  
	    state.DeVices[state.friUnCheckedBox].fri = false
        state.remove("friUnCheckedBox") }
    endif
    if(state.satCheckedBox) {def dev = DeVices.find{"$it.id" == state.satCheckedBox}  
	    state.DeVices[state.satCheckedBox].sat = true
        state.remove("satCheckedBox") }
	else if(state.satUnCheckedBox) {def dev = DeVices.find{"$it.id" == state.satUnCheckedBox}  
	    state.DeVices[state.satUnCheckedBox].sat = false
        state.remove("satUnCheckedBox") }
    endif
    /////////////////////////////// Table Build
	String str = "<script src='https://code.iconify.design/iconify-icon/1.0.0/iconify-icon.min.js'></script>"   //////// font-weight: bold !important; word-wrap: break-word !important; white-space: normal!important
        str += "<style>.mdl-data-table tbody tr:hover{background-color:inherit} .tstat-col td, .tstat-col th {font-size:15px !important; padding:2px 4px;text-align:center} + .tstat-col td {font-size:13px  }" +
        "</style><div style='overflow-x:auto'><table class='mdl-data-table tstat-col' style=';border:3px solid black'>" +
        "<thead><tr style='border-bottom:2px solid black'><th>zone</th>" +	
        "<th>Device</th>" +
        "<th>State</th>" +     
		"<th style='width: 80px !important'>Start<br>Time</th>" +
        "<th style='width: 80px !important'>Run Time<br>Minutes</th>" +
        "<th>Sun</th>" + "<th>Mon</th>" + "<th>Tue</th>" + "<th>Wed</th>" + "<th>Thur</th>" + "<th>Fri</th>" + "<th>Sat</th>" +
        "<th>On<br>Counts" +    
        "<th>Total<br>On Time</th>"+
        "<th>Reset<br>On Time</th></tr></thead>"
        
    int zone = 0
    DeVices.sort{it.displayName.toLowerCase()}.each {dev ->
        zone += 1 
        String thisStartTime = state.DeVices["$dev.id"].startTime.substring(11, state.DeVices["$dev.id"].startTime.length() - 12)    
        String thisDurTime = state.DeVices["$dev.id"].durTime
        String thisCron = state.DeVices["$dev.id"].cron
        String thisZone = state.DeVices["$dev.id"].zone = zone
        
        ////////////////////////// Active/On Time Calc
        int counts = state.DeVices["$dev.id"].counts   
        int total = state.DeVices["$dev.id"].total / 1000    
        int hours = total / 3600
        total = total % 3600
	    int mins = total / 60
	    int secs = total % 60
	    String time = "$hours:${mins < 10 ? "0" : ""}$mins:${secs < 10 ? "0" : ""}$secs"
        if (logEnableBool) {log.info "App: ${app.label} - Page Refresh, *${dev}*, StartTime-${thisStartTime}, Duration-${thisDurTime}, Counts-${counts}, Cron-${thisCron}"}
                
        String devLink = "<a href='/device/edit/$dev.id' target='_blank' title='Open Device Page for $dev'>$dev"
		String resetTotal = buttonLink("z$dev.id", "<iconify-icon icon='bx:reset'></iconify-icon>", "black", "23px") 
        String startTime = thisStartTime ? buttonLink("o$dev.id", thisStartTime, "purple") : buttonLink("p$dev.id", "Select", "green") 
        String durTime = thisDurTime ? buttonLink("q$dev.id", thisDurTime, "purple") : buttonLink("u$dev.id", "Select", "green") 
        String sunCheckBoxT = (state.DeVices["$dev.id"].sun) ? buttonLink("a$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("b$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")  
        String monCheckBoxT = (state.DeVices["$dev.id"].mon) ? buttonLink("c$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("d$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        String tueCheckBoxT = (state.DeVices["$dev.id"].tue) ? buttonLink("e$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("f$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        String wedCheckBoxT = (state.DeVices["$dev.id"].wed) ? buttonLink("g$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("h$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        String thuCheckBoxT = (state.DeVices["$dev.id"].thu) ? buttonLink("i$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("j$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        String friCheckBoxT = (state.DeVices["$dev.id"].fri) ? buttonLink("k$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("l$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        String satCheckBoxT = (state.DeVices["$dev.id"].sat) ? buttonLink("m$dev.id", "<iconify-icon icon='material-symbols:check-box'></iconify-icon>", "green", "23px") : buttonLink("n$dev.id", "<iconify-icon icon='material-symbols:check-box-outline-blank'></iconify-icon>", "black", "23px")
        
        str += "<tr style='color:black'><td>$thisZone</td>" + 
        "<td>$devLink</td>" +
        "<td style='font-weight:bold; color:${dev.currentSwitch == "on" ? "green" : "red"}'title='State $dev'>$dev.currentSwitch </td>" +    
		"<td style='font-weight:bold' title='${thisStartTime ? "Click to Change Start Time" : "Select"}'>$startTime</td>" +
        "<td style='font-weight:bold' title='${thisDurTime ? "Click to Change Run Duration" : "Select"}'>$durTime</td>" + 
        "<td title='Check Box to select Day'>$sunCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$monCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$tueCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$wedCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$thuCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$friCheckBoxT</td>" +  
        "<td title='Check Box to select Day'>$satCheckBoxT</td>" +
        "<td style='font-weight:bold' title='Total On Counts' >${state.DeVices["$dev.id"].counts}</td>" +
        "<td style='font-weight:bold; color:${dev.currentSwitch == "on" ? "green" : "red"}'>$time</td>" +       
        "<td style='border-right:3px solid black' title='Reset Total On Time for $dev' style='padding:0px 0px'>$resetTotal</td></tr>" 
        }
   	str += "</table></div>"
    str
}
String buttonLink(String btnName, String linkText, color = "#1A77C9", font = "13px") {
   "<div class='form-group'><input type='hidden' name='${btnName}.type' value='button'></div><div><div class='submitOnChange' onclick='buttonClick(this)' style='color:$color;cursor:pointer;font-size:$font'>$linkText</div></div><input type='hidden' name='settings[$btnName]' value=''>"
}
//////////////////////////////////////////////////////// Schedules and Subscribes //////////////////////////////////////////////////////////
void initialize() {
     subscribe(DeVices, "switch.on", onTimeHandler)
	 subscribe(DeVices, "switch.off", offTimeHandler)
     subscribe(remoteSwitch, "switch.on", remoteOffHandler)
     subscribe(remoteSwitch, "switch.off", remoteOffHandler)
    
     DeVices.sort{it.displayName.toLowerCase()}.each {dev ->
         zone = state.DeVices["$dev.id"].zone
         if (state.DeVices["$dev.id"].cron && state.DeVices["$dev.id"].durTime != 0) {
            schedule("${state.DeVices["$dev.id"].cron}", switchOnHandler, [data:zone, overwrite:false]) 
            if (logEnableBool) {log.debug "${app.label} -SCHEDULED....Device ${dev}.......duration = ${state.DeVices["$dev.id"].durTime}......cronString = ${state.DeVices["$dev.id"].cron}"}
         }
     }
}   
/////////////////////////////////////////////////////////////// Handlers //////////////////////////////////////////////////////////////////
def onTimeHandler(evt) {
    state.DeVices[evt.device.id].start = now()
    state.DeVices[evt.device.id].counts += 1  
    //log.debug "${app.label} - ON HANDLER, Device ${evt.device.id}.....start= ${state.DeVices[evt.device.id].start}" ////////////////////
}
def offTimeHandler(evt) {
    state.DeVices[evt.device.id].total += now() - state.DeVices[evt.device.id].start
    //log.debug "${app.label} - OFF HANDLER, Device ${evt.device.id}.....total= ${state.DeVices[evt.device.id].total}" ////////////////////
}
void switchOnHandler(data) { 
    if (pauseBool) {return}
    DeVices.each { dev -> 
        if (data.value == state.DeVices["$dev.id"].zone ) {
            zone = state.DeVices["$dev.id"].zone
            if (logEnableBool) {log.debug "${app.label} -SWITCH-ONhandler...Device = ${dev}...duration = ${state.DeVices["$dev.id"].durTime}"}
            dev.on()
            runIn(60 * state.DeVices["$dev.id"].durTime, switchOffHandler, [data:zone, overwrite:false])
        }
     }
}
void switchOffHandler(data) {
    DeVices.each { dev -> 
        if (data.value == state.DeVices["$dev.id"].zone ) {
           if (logEnableBool) {log.debug "${app.label} -SWITCH-OFFhandler...Device = ${dev}...zone = ${data.value}"}  
           dev.off()
        }
     }
}
def allOffHandler(evt) { DeVices.off() }

def remoteOffHandler(evt) {
    if (logEnableBool) {log.debug "App: ${app.label} - Remote Off, Pause/Resume Handler....switch ${evt.device} = ${evt.device.currentSwitch}"}
    if (evt.device.currentSwitch == "on") {DeVices.off();  app?.updateSetting("pauseBool",[value:"true",type:"bool"]) }
    else app?.updateSetting("pauseBool", [value:"false",type:"bool"])
}
void appButtonHandler(btn) {
    if (btn == "refresh") refreshHandler()   
    else if (btn == "allOff") allOffHandler()  
    else if (btn == "updateButton") updated()  
    else if (btn.startsWith("a")) state.sunUnCheckedBox = btn.minus("a") 
    else if (btn.startsWith("b")) state.sunCheckedBox = btn.minus("b")
    else if (btn.startsWith("c")) state.monUnCheckedBox = btn.minus("c") 
    else if (btn.startsWith("d")) state.monCheckedBox = btn.minus("d")
    else if (btn.startsWith("e")) state.tueUnCheckedBox = btn.minus("e") 
    else if (btn.startsWith("f")) state.tueCheckedBox = btn.minus("f")
    else if (btn.startsWith("g")) state.wedUnCheckedBox = btn.minus("g") 
    else if (btn.startsWith("h")) state.wedCheckedBox = btn.minus("h")
    else if (btn.startsWith("i")) state.thuUnCheckedBox = btn.minus("i") 
    else if (btn.startsWith("j")) state.thuCheckedBox = btn.minus("j")
    else if (btn.startsWith("k")) state.friUnCheckedBox = btn.minus("k") 
    else if (btn.startsWith("l")) state.friCheckedBox = btn.minus("l")
    else if (btn.startsWith("m")) state.satUnCheckedBox = btn.minus("m") 
    else if (btn.startsWith("n")) state.satCheckedBox = btn.minus("n")
    else if (btn.startsWith("o")) state.newStartTime = btn.minus("o") 
    else if (btn.startsWith("p")) state.remStartTime = btn.minus("p")  
    else if (btn.startsWith("q")) state.newDurTime = btn.minus("q") 
    else if (btn.startsWith("u")) state.remDurTime = btn.minus("u")
    else if (btn.startsWith("z")) state.resetTotal = btn.minus("z")  
    endif    
}
/////////////////////////////////////////////////////////////// Functions //////////////////////////////////////////////////////////////////
def buildCron() {
  state.DeVices.each {k, v ->
     def dev = DeVices.find{"$it.id" == k}
        if(state.DeVices[k].startTime) {
            String formattedTime = state.DeVices[k].startTime.substring(11, state.DeVices[k].startTime.length() - 12)
            String hours = formattedTime.substring(0, formattedTime.length() - 3) // Chop off the last 3 in string
            String minutes = formattedTime.substring(3) // Chop off the first 3 in string
              
            String days = ""
            if (state.DeVices[k].sun) {days = "SUN,"}
            if (state.DeVices[k].mon) {days += "MON,"}
            if (state.DeVices[k].tue) {days += "TUE,"}
            if (state.DeVices[k].wed) {days += "WED,"}
            if (state.DeVices[k].thu) {days += "THU,"}
            if (state.DeVices[k].fri) {days += "FRI,"}
            if (state.DeVices[k].sat) {days += "SAT,"}
            if (days != "") {
                days = days.substring(0, days.length() - 1) // Chop off last ","
                state.DeVices[k].days = days
                state.DeVices[k].cron = "0 ${minutes} ${hours} ? * ${days} *" 
                if (logEnableBool) {log.debug "App: ${app.label} - Device ${dev}, BUILD-CRON = ${state.DeVices[k].cron}"} 
               }       
         }
   }
}
def refreshHandler() { ///////////// Update Times if Active/On
    state.DeVices.each {k, v ->
        def dev = DeVices.find{"$it.id" == k}
        if (dev.currentSwitch == "on") {
          state.DeVices[k].total += now() - state.DeVices[k].start
		  state.DeVices[k].start = now()
          if (logEnableBool) {log.info "${app.label} -REFRESH Handler, Device ${k}.... state.DeVices[k].total= ${state.DeVices[k].total}"}
          }
    }
}
//////////////////////////////////////////////////////////////////  Other Stuff //////////////////////////////////////////////////////////////////
def updated() {
    unsubscribe()
    unschedule()
    buildCron()
    initialize()
    if(logEnableBool) runIn(3600, logsOff)  // Disable all Logging after time elapsed
}
def installed() {}
def logsOff() {
    log.info "${app.label} - All App logging auto disabled"
    app?.updateSetting("logEnableBool",[value:"false",type:"bool"])
}
def getFormat(type, myText="") {		
    if(type == "title") return "<h3 style='color:#0000ff;font-weight: bold'>${myText}</h3>"
    if (formatBool) {if(type == "header") {return "<div style='color:#660000;font-weight: bold'>${myText}</div>"}  //// ver1.2.0
                     if(type == "important2") return "<div style='color:#5a8200'>${myText}</div>"
    }
    else {if(type == "header") return "<div style='color:#000000;font-weight: bold'>${myText}</div>"
          if(type == "important2") return "<div style='color:#000000'>${myText}</div>"
    }
    //if(type == "red") return "<div style='color:#660000'>${myText}</div>"
	if(type == "importantBold") return "<div style='color:#32a4be;font-weight: bold'>${myText}</div>"
	if(type == "important") return "<div style='color:#32a4be'>${myText}</div>"
	//if(type == "important2Bold") return "<div style='color:#5a8200;font-weight: bold'>${myText}</div>"
	if(type == "lessImportant") return "<div style='color:green'>${myText}</div>"
}
def displayTitle() {
    titleVersion()
    section (getFormat("title",  "App: ${state.name} - ${"ver " + state.version}")) {}
}
