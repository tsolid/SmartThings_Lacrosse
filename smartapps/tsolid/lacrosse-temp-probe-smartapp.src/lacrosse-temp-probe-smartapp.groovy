/**
 *  The Weather Company Web Smartapp
 *
 *  Copyright 2020 Tom S
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Lacrosse Temp Probe Smartapp",
    namespace: "tsolid",
    author: "Tom Sxxxxx",
    description: "SmartApp enabling using a Lacrosse Temp Probe via their API",
    category: "My Apps",
    iconUrl: "https://www.bbb.org/ProfileImages/2a72d4cc-a352-46a7-8aa6-f106df2ed96c.png",
    iconX2Url: "https://www.bbb.org/ProfileImages/2a72d4cc-a352-46a7-8aa6-f106df2ed96c.png",
    iconX3Url: "https://www.bbb.org/ProfileImages/2a72d4cc-a352-46a7-8aa6-f106df2ed96c.png")

preferences {

	section("Alert Settings") {
		input "LTPlowtempalert", "number", title: "Low temperature Alert", required: false
        }

	section("Switch On these on Low Temperature Alert:")
        {
        	input "ltplowton", "capability.switch", required: false, multiple: true
        }

	section("Switch Off these on Low Temperature Alert:")
        {
        	input "ltplowtoff", "capability.switch", required: false, multiple: true
        }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	state.deviceId="12345678AF"
	state.deviceName=""
	state.deviceRef= getAllChildDevices()?.find {it.device.deviceNetworkId == state.deviceId}
	log.debug "state.deviceRef installed with ${state.deviceRef}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe() 
	addDevices()
	//initialize()

}

def initialize() {
	log.debug "initialize"
    
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

def addDevices() {
	    
    if (childDevices)
    {
        def Ref= getAllChildDevices()?.find {
            it.device.deviceNetworkId == state.deviceId}
        log.debug "Devices installed before removal ${Ref}"
 
       // Make sure the settings are applied by removing the previous occurence
        removeChildDevices(getChildDevices())

        Ref= getAllChildDevices()?.find {
            it.device.deviceNetworkId == state.deviceId}
        log.debug "Devices installed after removal ${Ref}"
    }

    subscribe(addChildDevice("tsolid", "Lacrosse Temp Probe", state.deviceId, null, [
        "label": "Temp Probe Data",
        "data": [
            "TWClowtempalert": twclowtempalert,
            /*completedSetup: true*/]
    ]), "Alert", eventHandler)                           
}

def eventHandler(evt)
{
	Map options = [:]
	log.debug "LTP evt.name: ${evt.name}"
	log.debug "LTP evt.value: ${evt.value}"


	if(evt.name == "Alert") 
	{
	    	options.method = 'push'
	        sendNotification(evt.value, options) 
	}                   
}
