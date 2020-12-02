/* Child Contact Sensor Advanced
*  
*  Copyright 2017 T. Kamp (kampto)
*  -Starting point from 2017 @ogiewon Smartthings ST_Anything child device DH. Applied major changes/feature additions overtime to fit different applications.  
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
*            http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
*  OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*
*    NOTES:  This DH allows you to select what text is sent besides the generic "Open", "Closed". EX: you can select "Empty" or "Full" or in reverse.
*    Smartthings: This DH will may not compile on Smarthings IDE as of 2020, Its now built for Hubitat.  
*    SharpTools and other frontends: If you switch to this DH with the extra attributes you may need to go (Hubitat) Apps/sharptools/next/done to get them to take. 
*
*  Change Revision History:
*    Date:       Who:           What:
*    2020-09-23  kampto         Converted from ST to Hubitat format, with eneble/disable debugging, removed color map and tiles 
*    2018-06-12  kampto         Last update 24hr selectable feature
*    2018-04-16  kampto         Add several contact open/closed text selections and color mapping  
*    2017-10-09  kampto         Origination and deviation for value formatting
*/
metadata {
	definition (name: "Child Contact Sensor Advanced", namespace: "kampto", author: "T. Kamp", 
        importUrl: "https://github.com/kampto/Hubitat/blob/main/Drivers/Child_Contact_Sensor_Advanced.groovy") {
		    capability "Contact Sensor"
		    capability "Sensor"
        
    	  attribute "lastUpdated", "String"    
        }
    
    preferences {
        input name: "logEnable", type: "bool", title: "<b>Enable debug logging?</b>", description: "Will Auto Disable in 30min", defaultValue: true
		    input name: "lastUpdateEnable", type: "bool", title: "<b>Enable Last Update Attribute?</b>", defaultValue: true
        input name: "clockformat", type: "bool", title: "<b>Use 24 hour clock?</b>", description: "Used in Last Update if Enabled", defaultValue: true
//// Closed State Inputs			         
        input name: "displayLabel1", type: "enum", title: "<b>Change Text for Contact 'Closed'</b>", description: "Default = Closed", defaultValue: "Closed", required: false, 
            options:[
            ["Closed":"Closed"], // Default
            ["Open":"Open"], 
            ["Full":"Full"],
            ["Empty":"Empty"],
            ["High":"High"],    
            ["Low":"Low"],    
            ["On":"On"],
            ["Off":"Off"],
            ["Running":"Running"],
            ["Armed":"Armed"],
            ["DisArmed":"DisArmed"],    
            ["AC On":"AC On"],  
            ["AC Off":"AC Off"],
            ["Enabled":"Enabled"],    
            ["Disabled":"Disabled"], 
            ["Local":"Local"],  
            ["Remote":"Remote"], 
            ], displayDuringSetup: false
            
//// Open State Inputs            
        input name: "displayLabel2", type: "enum", title: "<b>Change Text for Contact 'Open'</b>", description: "Default = Open", defaultValue: "Open", required: false, 
            options:[
            ["Closed":"Closed"], 
            ["Open":"Open"], // Default
            ["Full":"Full"],
            ["Empty":"Empty"],
            ["High":"High"],    
            ["Low":"Low"],    
            ["On":"On"],
            ["Off":"Off"],
            ["Running":"Running"],
            ["Armed":"Armed"],
            ["DisArmed":"DisArmed"],    
            ["AC On":"AC On"],  
            ["AC Off":"AC Off"],
            ["Enabled":"Enabled"],    
            ["Disabled":"Disabled"], 
            ["Local":"Local"],  
            ["Remote":"Remote"], 
            ], displayDuringSetup: false
        }
}   

def parse(String description) {
    if (logEnable) log.info "Raw capability parse (${description})"
	  def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    if (name && value) {
        
//// Send Contact Update        
        if (value.equals("open")) {sendEvent(name: name, value: displayLabel2)
          if (logEnable) log.debug "Sent Open Value as = ${displayLabel2}" 
          }
        else if (value.equals("closed")) {sendEvent(name: name, value: displayLabel1)
          if (logEnable) log.debug "Sent Closed Value as = ${displayLabel1}" 
          }
                
//// Send Last Update Time, if Enabled                  
        def timeString = clockformat ? "HH:mm" : "h:mm: a" // 24Hr : 12Hr
        def nowDay = new Date().format("MMM dd", location.timeZone)
        def nowTime = new Date().format("${timeString}", location.timeZone) 
        if (lastUpdateEnable) {sendEvent(name: "lastUpdated", value: nowDay + " " + nowTime, displayed: false)}
    }
    
    else {log.error "Missing either name or value.  Cannot parse!"}
}

def logsOff(){
    log.warn "debug logging auto disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def installed() {
    updated()
}

def updated() {
    if (logEnable) runIn(1800,logsOff)
}
