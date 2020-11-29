/**
 *    Child Voltage Sensor Advanced
 *
 *    Copyright 2018 T Kamp (kampto)
 *  Base code originated from @ogiewon ST_Anything child device and highly modified/changed with more features.  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change Revision History:
 *    Date:       Who:           What:
 *    2020-11-29  kampto         Added Max Value attribute
 *    2020-09-23  kampto         Converted from ST to Hubitat format, with eneble/disable debugging, removed color map  
 *    2019-07-01  kampto         Added decimal poistion and multipler
 *    2018-05-12  kampto         Last update 24hr selectable feature 
 *    2017-10-09  kampto         Origination and deviation
 *
 */
metadata {
	definition (name: "Child Voltage Sensor Advanced", namespace: "kampto", author: "T Kamp", importUrl: "https://github.com/kampto/Hubitat/blob/main/Drivers/Child-Voltage-Sensor-Advanced") {
		capability "Voltage Measurement"
		capability "Sensor"
        
    	attribute "lastUpdated", "String"    
        attribute "maxValue", "number"    
	}
        
    preferences {
        input name: "logEnable", type: "bool", title: "<b>Enable debug logging?</b>", description: "Will Auto Disable in 30min", defaultValue: true
		input name: "multiplier", type: "enum", title: "<b>Adjust Decimal Position</b>", description: "Default = x1", defaultValue: "x1", required: false, multiple: false, options:[["0.001":"x0.001"], ["0.01":"x0.01"], ["0.1":"x0.1"],["1":"x1"], ["10":"x10"], ["100":"x100"], ["1000":"x1000"]], displayDuringSetup: false
        input name: "numDecimalPlaces", type: "enum", title: "<b>Number of decimal places</b>", description: "Default = 0", defaultValue: "0", required: false, multiple: false, options:[["0":"0"], ["1":"1"], ["2":"2"], ["3":"3"]], displayDuringSetup: false
        input name: "lastUpdateEnable", type: "bool", title: "<b>Enable Last Update Attribute?</b>", defaultValue: true
        input name: "clockformat", type: "bool", title: "<b>Use 24 hour clock?</b>", description: "Used in Last Update if Enabled", defaultValue: true
        input name: "maxValueBool", type: "bool", title: "<b>Enable Max VALUE Attributte?</b>", defaultValue: true
        input name: "maxValueResetBool", type: "bool", title: "<b>Reset Max VALUE at Midnite?</b>", defaultValue: true
        input name: "inputMaxValue", type: "number", title: "<b>Starting Max VALUE</b>", description: "Default = 0, No need to change, will Auto change if Max VALUE Enabled", range: "*...*", defaultValue: 0, required: false, displayDuringSetup: false
    }
}   
def logsOff(){
    log.warn "debug logging auto disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def parse(String description) {
    if (logEnable) log.info "Raw capability parse (${description})"
	def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    if (name && value) {
        
//// Incase Settings werent configured on Device        
        if (numDecimalPlaces == null) {device.updateSetting("numDecimalPlaces", [value: "0", type: "enum"]) }
        if (multiplier == null) {device.updateSetting("multiplier", [value: "1", type: "enum"]) }
        if (inputMaxValue == null) {device.updateSetting("inputMaxValue", [value: 0, type: "number"]) }
        //if (logEnable) log.debug "Test Previous Max Input is ${inputMaxValue}"
        
//// #Decimals and Decimal position Conversion Calc                         
        float tmpMultiplier = multiplier as float 
        float tmpInputMaxValue = inputMaxValue as float  
        float tmpValue = Float.parseFloat(value)
        tmpValue = tmpValue * tmpMultiplier
        tmpValue = tmpValue.round(numDecimalPlaces.toInteger())
        sendEvent(name: name, value: tmpValue, unit: "V")
        //if (logEnable) log.debug "Test converted Value is now ${tmpValue}"
        
//// Max VALUE Conversion Calc and Reset
        if (maxValueResetBool) {
        if (new Date().format("HH", location.timeZone) == 00 && (new Date().format("mm", location.timeZone)) < 59  ) {
            device.updateSetting("inputMaxValue", [value: 0, type: "number"]) }    // Reset Max Value betweem midnite and 1am
            }
                
        if (tmpInputMaxValue < tmpValue) {
          device.updateSetting("inputMaxValue", [value: tmpValue, type: "number"]) 
          if (maxValueBool) {sendEvent(name: "maxValue", value: tmpValue, unit: " max")  // Send new Max Value if enabled
          if (logEnable) log.info "New Max Voltage is ${tmpValue}"
          //if (logEnable) log.debug "Test New Max input is now ${inputMaxValue}"
          }
        }
        
//// Last Update                  
        def timeString = clockformat ? "HH:mm" : "h:mm: a" // 24Hr : 12Hr
        def nowDay = new Date().format("MMM dd", location.timeZone)
        def nowTime = new Date().format("${timeString}", location.timeZone) 
        if (lastUpdateEnable) {sendEvent(name: "lastUpdated", value: nowDay + " " + nowTime, displayed: false)} // kampto,  Update lastUpdated date and time
        }
    
    else {log.error "Missing either name or value.  Cannot parse!"}
}

def installed() {
    updated()
}
