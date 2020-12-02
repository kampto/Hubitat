/* Child Voltage Sensor Advanced
*  
*  Copyright 2017 T. Kamp (kampto)
*  -Starting point from 2017 @ogiewon Smartthings ST_Anything child device DH. Applied major changes/feature additions overtime to fit different applications.  
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
*            http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
*  OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*
*    NOTES:  Parsed log/sent float values produce a value with 1 decimal, And if values after the decimals are all Zero's, it will only use one "0" decimal.
*            So the Multiplier and Number of Decimals settings will not achieve certain conditions. It will work for cases like converting smaller unit large 
*            numbers to larger unit numbers or vise vera. EX: 12489mV to 12.49V. If availible you can modify decimal on the Hub dash or Frontend apps (sharptools, etc..)
*    Smartthings: This DH will not compile on Smarthings IDE as of 2020, Its built for Hubitat. Smarthing IDE seems to have changed/broken some legacy Groovy code. 
*    SharpTools and other frontends: If you switch to this DH with the extra attributes you will need to go (Hubitat) Apps/sharptools/next/done to get them to take. 
*
*  Change Revision History:
*    Date:       Who:           What:
*    2020-11-29  kampto         Added Max/Min Value attribute, resetable, notes, unit conversion
*    2020-09-23  kampto         Converted from ST to Hubitat format, with eneble/disable debugging, removed color map and tiles 
*    2019-07-01  kampto         Added decimal poistion and multipler
*    2018-10-16  kampto         Advanced color mapping (legacy ST app)   
*    2018-05-12  kampto         Last update 24hr selectable feature 
*    2017-10-09  kampto         Origination and deviation for value formatting, Modified from ST_Anything child DH
*/
metadata {
	definition (name: "Child Voltage Sensor Advanced", namespace: "kampto", author: "T. Kamp", 
        importUrl: "https://github.com/kampto/Hubitat/blob/main/Drivers/Child_Voltage_Sensor_Advanced.groovy") {
		    capability "Voltage Measurement"
		    capability "Sensor"
        
    	    attribute "lastUpdated", "String"    
            attribute "maxValue", "number"  
            attribute "minValue", "number"    
	        }
        
    preferences {
        input name: "logEnable", type: "bool", title: "<b>Enable debug logging?</b>", description: "Will Auto Disable in 30min", defaultValue: true
        input name: "units", type: "enum", title: "<b>Voltage Units</b>", description: "Default = V", defaultValue: "2", required: false, multiple: false, options:[["1":"mV"], ["2":"V"], ["3":"KV"]], displayDuringSetup: false
		input name: "multiplier", type: "enum", title: "<b>Number Multiplier</b>", description: "Default = x1", defaultValue: "1", required: false, multiple: false, options:[["0.001":"x0.001"], ["0.01":"x0.01"], ["0.1":"x0.1"],["1":"x1"], ["10":"x10"], ["100":"x100"], ["1000":"x1000"]], displayDuringSetup: false
        input name: "numDecimalPlaces", type: "enum", title: "<b>Number of Decimals Places</b>", description: "Default = 1", defaultValue: "1", required: false, multiple: false, options:[["0":"0"], ["1":"1"], ["2":"2"], ["3":"3"]], displayDuringSetup: false
        input name: "lastUpdateEnable", type: "bool", title: "<b>Enable Last Update Attribute?</b>", defaultValue: true
        input name: "clockformat", type: "bool", title: "<b>Use 24 hour clock?</b>", description: "Used in Last Update if Enabled", defaultValue: true
        input name: "max_minEnable", type: "bool", title: "<b>Enable Max/Min VALUE Attributtes?</b>", defaultValue: true
        input name: "max_minResetEnable", type: "bool", title: "<b>Reset Max/Min VALUE's at Midnite?</b>", defaultValue: true
        input name: "inputMaxValue", type: "number", title: "<b>Starting Max VALUE</b>", description: "Default = 0, Don't change, will Auto populate with new Max VALUE", range: "*...*", defaultValue: 0, required: false, displayDuringSetup: false
        input name: "inputMinValue", type: "number", title: "<b>Starting Min VALUE</b>", description: "Default = 50000, Don't change, will Auto populate with new Min VALUE", range: "*...*", defaultValue: 50000, required: false, displayDuringSetup: false
    }
}   

def parse(String description) {
    if (logEnable) log.info "Raw capability parse (${description})"
	def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    def dispUnit
    if (name && value) {

//// Set units
        if (units == "1") {dispUnit = "mV"}
         else if (units == "3") {dispUnit = "KV"}
         else {dispUnit = "V"}
    
//// Send Value with #Decimals and Decimal position Conversion Calc                        
        float tmpMultiplier = multiplier as float 
        float tmpValue = Float.parseFloat(value)
         tmpValue = tmpValue * tmpMultiplier
         tmpValue = tmpValue.round(numDecimalPlaces.toInteger())
        sendEvent(name: name, value: tmpValue, unit: dispUnit)
        if (logEnable) log.debug "Sent Value = ${tmpValue} " + dispUnit
        
//// Send Max & Min VALUE Conversion Calc and Reset, if Enabled
        float tmpInputMinValue = inputMinValue as float
        float tmpInputMaxValue = inputMaxValue as float 
        float hour = new Date().format("HH", location.timeZone) as float 
        float minute = new Date().format("mm", location.timeZone) as float
        if (max_minResetEnable) {
         if (hour == 00 && minute < 31) {   // 30min window to reset
            device.updateSetting("inputMinValue", [value: 50000, type: "number"]) 
            device.updateSetting("inputMaxValue", [value: 0, type: "number"]) }    // Reset Max Value after midnite
            }
        if (max_minEnable) {
         if (tmpInputMaxValue < tmpValue) {device.updateSetting("inputMaxValue", [value: tmpValue, type: "number"]) 
           sendEvent(name: "maxValue", value: tmpValue, unit: " max")   // Send new Max Value if enabled
           if (logEnable) log.info "New Max Value is ${tmpValue} " + dispUnit
           }
         if (tmpValue < tmpInputMinValue) {device.updateSetting("inputMinValue", [value: tmpValue, type: "number"]) 
           sendEvent(name: "minValue", value: tmpValue, unit: " min")   // Send new Min Value if enabled
           if (logEnable) log.info "New Min Value is ${tmpValue} " + dispUnit
           }   
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
