/* Child Ultrasonic Tank Level Sensor
*  
*  Copyright 2017 T. Kamp (kampto)
*   
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
*            http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
*  OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*
*    NOTES:  This is for cylindrical vertical tanks with unltrasonic senor on the top. Enter your units or unit conversions first. EX: if you want inches/gallons but sensor reports in cm then select "Convert Sensor Centimeters to Inches". 
*	 Required is the 3 tank dimensions. If availible you can modify decimals on the Hub dash or Frontend apps (sharptools, etc..)
*    Smartthings: This DH might compile on Smarthings IDE as of 2020, Its built for Hubitat. You can try but no garuantee's 
*    SharpTools and other frontends: If you switch to this DH with the extra attributes you will need to go (Hubitat) Apps/sharptools/next/done to get them to take. 
*
*	Change Revision History:
*	Date:       Who:           What:
*	2020-12-04  kampto         Added Max/Min Value attribute, resetable, notes, unit conversion
*	2020-09-23  kampto         Converted from ST to Hubitat format, with eneble/disable debugging, removed color map and tiles 
*	2018-10-16  kampto         Advanced color mapping (legacy ST app)	
*	2018-07-17  kampto         Added tank volume percentage
*	2018-05-12  kampto         Last update 24hr selectable feature 
*	2017-10-10  kampto         Origination and deviation for value formatting, Modified from ST_Anything child DH
*/
metadata {
	definition (name: "Child Ultrasonic Tank Level Sensor", namespace: "kampto", author: "T. Kamp", 
		importUrl: "https://github.com/kampto/Hubitat/blob/main/Drivers/Child_Ultrasonic_Tank_Level_Sensor.groovy") {
			capability "Sensor"
        
			attribute "lastUpdated", "String" 
			attribute "tankMaxVolume", "number"
			attribute "tankMeasuredVolume", "number"
			attribute "maxValue", "number"  
			attribute "minValue", "number"
			}
    
    preferences {
		input name: "units", type: "enum", title: "<b>Units</b>", description: "Default = Convert sensor cm to inch", defaultValue: "3", required: false, multiple: false, options:[["1":"Inches"], ["2":"Centimeters"], ["3":"Convert Sensor Centimeters to Inches"], ["4":"Convert Sensor Inches to Centimeters"]], displayDuringSetup: false
		input name: "logEnable", type: "bool", title: "<b>Enable debug logging?</b>", description: "Will Auto Disable in 30min", defaultValue: true
		input name: "height", type: "number", title: "<b>Tank Height</b>", description: "Enter height of tank full level (cm or inches)", required: true, defaultValue: true
		input name: "diameter", type: "number", title: "<b>Tank Diameter</b>", description: "Enter diameter of tank (cm or inches)", required: true, defaultValue: true
		input name: "airgap", type: "number", title: "<b>AirGap</b>", description: "Enter Sensor Height Above Full Level (cm or inches)", required: true, defaultValue: true
		input name: "lastUpdateEnable", type: "bool", title: "<b>Enable Last Update Attribute?</b>", defaultValue: true
		input name: "clockformat", type: "bool", title: "<b>Use 24 hour clock?</b>", description: "Used in Last Update if Enabled", defaultValue: true
		input name: "max_minEnable", type: "bool", title: "<b>Enable Max/Min VALUE Attributtes?</b>", defaultValue: true
		input name: "max_minResetEnable", type: "bool", title: "<b>Reset Max/Min VALUE's at Midnite?</b>", defaultValue: true
		input name: "inputMaxValue", type: "number", title: "<b>Starting Max VALUE</b>", description: "Default = -50, Don't change, will Auto populate with new Max VALUE", range: "*...*", defaultValue: -50, required: false, displayDuringSetup: false
		input name: "inputMinValue", type: "number", title: "<b>Starting Min VALUE</b>", description: "Default = 50000, Don't change, will Auto populate with new Min VALUE", range: "*...*", defaultValue: 50000, required: false, displayDuringSetup: false
		input name: "skipZeroValueEnable", type: "bool", title: "<b>Dont Send Zero Values?</b>", description: "If device resets with Zero Value dont send", defaultValue: false
	}
}   

def parse(String description) {
	if (logEnable) log.info "Raw capability parse (${description})"
	def parts = description.split(" ")
	def name  = parts.length>0?parts[0].trim():null
	def value = parts.length>1?parts[1].trim():null
	def dispUnit
	if (name && value) {
		if (skipZeroValueEnable && value == 0) {return} // dont proceed or send if value is zero
       	
		float tmpValue = Float.parseFloat(value)
		float tmpHeight = height as float
		float tmpDiameter = diameter as float
		float tmpAirgap = airgap as float
			
//// Units Conversion
	if (units == "2" || units == "4" ) {dispUnit = "L"} // centimeters input used
		else {dispUnit = "gal"} // inches input used
	if (units == "3") {tmpValue = tmpValue / 2.54} // Convert sensor raw data and input to inches
	if (units == "4") {tmpValue = tmpValue * 2.54} // Convert sensor raw data to centimeters

////Tank total Capacity Calculation and Send
	float maxVolume = 3.14159 * (tmpDiameter/2) * (tmpDiameter/2) * tmpHeight // Max Volume of Tank
	if (units == "3") {maxVolume = maxVolume / 231} // convert cubic in to Gallons
	if (units == "4") {maxVolume = maxVolume / 1000} // convert cubic cm to Liters
	maxVolume = maxVolume as int  // Remove decimals
	sendEvent(name: "tankMaxVolume", value: maxVolume, unit: dispUnit) // This is the total capacity when full
	if (logEnable) log.info "tankMaxVolume = ${maxVolume} " + dispUnit
		
//// Remaining measured volume in Tank
	float measuredVolume = ((3.14159 * (tmpDiameter/2) * (tmpDiameter/2) * tmpHeight) -  (3.14159 * (tmpDiameter/2) * (tmpDiameter/2) * (tmpValue - tmpAirgap)))
	if (units == "3") {measuredVolume = measuredVolume / 231} // convert cubic in to Gallons
	if (units == "4") {measuredVolume = measuredVolume / 1000} // convert cubic cm to Liters
	measuredVolume = measuredVolume as int // Remove decimals
	sendEvent(name: "tankMeasuredVolume", value: measuredVolume, unit: dispUnit) // Use this for how much is left in the tank
		if (logEnable) log.info "tankMeasuredVolume = ${measuredVolume} " + dispUnit
    
//// Tank Percent full (Primary Attributte)   
	tmpValue = 100 - ((tmpValue-tmpAirgap)/height * 100 )  // Get the percent full
	tmpValue = tmpValue.round(1)
	sendEvent(name: name, value: tmpValue, unit: "%")
		if (logEnable) log.debug "Sent Primary Value = ${tmpValue}%"	
		
//// Send Max & Min VALUE Conversion Calc and Reset, if Enabled
	if (max_minEnable) {
		if (max_minResetEnable) {
			float tmpHour = new Date().format("HH", location.timeZone) as float 
			float tmpMinute = new Date().format("mm", location.timeZone) as float	
			 
			if (tmpHour == 0 && tmpMinute < 31) {   // 30min window to reset    
           		sendEvent(name: "maxValue", value: tmpValue, unit: " max")   // Send new Max Value if enabled
           		device.updateSetting("inputMaxValue", [value: tmpValue, type: "number"])  
				if (logEnable) {log.info "New Reset Max Value is ${tmpValue}%"}
             
           		sendEvent(name: "minValue", value: tmpValue, unit: " min")   // Send new Min Value if enabled
           		device.updateSetting("inputMinValue", [value: tmpValue, type: "number"]) 
				if (logEnable) {log.info "New Reset Min Value is ${tmpValue}%"}
           	}
		 }          
        
		float tmpInputMinValue = inputMinValue as float
		float tmpInputMaxValue = inputMaxValue as float 
					
		if (tmpInputMaxValue < tmpValue) { 
			sendEvent(name: "maxValue", value: tmpValue, unit: " max")   // Send new Max Value if enabled
			if (logEnable) {log.info "New Max Value is ${tmpValue}%"}
			device.updateSetting("inputMaxValue", [value: tmpValue, type: "number"])  
			}
			
		if (tmpValue < tmpInputMinValue) {
			sendEvent(name: "minValue", value: tmpValue, unit: " min")   // Send new Min Value if enabled
			if (logEnable) {log.info "New Min Value is ${tmpValue}%"}
			device.updateSetting("inputMinValue", [value: tmpValue, type: "number"]) 
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
