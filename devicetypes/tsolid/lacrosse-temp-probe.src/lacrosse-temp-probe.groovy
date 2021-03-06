/**
 *  Lacrosse Temp Probe Virtual Device Handler
 *
 *  Copyright 2020 tsolid
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

metadata {
	definition (name: "Lacrosse Temp Probe", namespace: "tsolid", author: "Tom Sxxxx") {
		capability "refresh"
        capability "polling"
        capability "sensor"
        capability "capability.temperatureMeasurement"
        capability "capability.relativeHumidityMeasurement"
        capability "Battery"
	}
    
    preferences {
    input name: "deviceId", type: "text", title: "DeviceId", description: "Enter Lacrosse DeviceId", required: true,
          displayDuringSetup: true
    }

	tiles(scale: 2) {

	standardTile("LT_Probe", "device.LT_Probe",  width: 6, height: 3,  canChangeIcon: false ) {
            state "default", icon: "https://cdn.shopify.com/s/files/1/0035/7443/1790/files/Main_copy_2x_f85b234b-d84f-43ea-91b3-92cc0f5ebaaf_190x.png", backgroundColor: "#999999"      } 

	 standardTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: false) {
            state "default", label: '${currentValue}º',unit:'${currentValue}', icon: "st.Weather.weather2", backgroundColor:"#999999"}  
        
	standardTile("humidity", "device.humidity", width: 2, height: 2, canChangeIcon: false) {
            state "default", label: '${currentValue}%', icon: "st.Weather.weather12", backgroundColor:"#999999"      }

	valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}

	standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
 		state "default", action:"refresh", icon:"st.secondary.refresh"
 		} 
	
	main("temperature")
	details(["LT_Probe","temperature","humidity","battery","refresh" ])
 	}
}

def installed() {
    runEvery10Minutes(forcepoll)

}

def updated() {

	log.debug "Executing 'updated'"
	refresh()
    runEvery10Minutes(forcepoll)
}

def poll(){
	log.debug "Executing 'poll'"
	refresh()
}

// parse events into attributes
def parse(String description) {

	log.debug "Executing 'parse'"
    state.lastUpdate=""
	state.lowtempalert=false
	refresh()    
	runEvery10Minutes(forcepoll)
    
}

def forcepoll()
{
	refresh()
}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"

	def mymap = makeJSONTempRequest() 
	log.debug mymap

	log.debug "response relative_humidity: ${mymap['humidity']}"
	log.debug "response temp_f: ${mymap['temp']}"
	log.debug "response lowbattery: ${mymap['lowbattery']}"
	log.debug "response updatetime: ${mymap['updatetime']}"
    
    if(state.lastUpdate=="" || state.lastUpdate < mymap['updatetime']) {
        sendEvent(name: "humidity", value:  mymap['humidity'])
        sendEvent(name: "temperature", value: mymap['temp'], unit: temperatureScale)
		if(mymap['lowbattery'] == "0") 
    	    sendEvent(name: "battery", value: 100, unit: "%")
        else
	        sendEvent(name: "battery", value: 1, unit: "%", descriptionText: "${device.displayName} has a low battery", isStateChange: true)
        sendEvent(name: "updatetime", value:  mymap['updatetime'])
        state.lastUpdate = mymap['updatetime']
    } 
    else 
    {
    	log.debug "not updated!"
    }

	if (getDataValue("LTPlowtempalert")) {
		if (getDataValue("LTPlowtempalert").toFloat() >= mymap['temperature'].toFloat())
		{
			if ( state.lowtempalert == false) {
                		sendEvent(name:"Alert", value: "Probe Low Temperature Alert!", displayed:true)
				state.lowtempalert=true 
			}
        	}
	        else
	            state.lowtempalert=false
	}

}

def makeJSONTempRequest() {
    def params = [
        uri:  'http://decent-destiny-704.appspot.com',
        path: '/laxservices/device_info.php',
        contentType: 'application/json',
        query: [deviceid: deviceId, limit: '5', timezone: '3', metric: '0', cachebreaker: new Date().getTime()]
    ]

	def result = [temp: "", humidity: "", lowbattery: "", updatetime: ""]

    try {
        httpGet(params) {resp ->
            //log.debug "resp data: ${resp.data}"
		result["lowbattery"] = resp.data.device0.obs[0].lowbattery
		result["temp"] = resp.data.device0.obs[0].probe_temp
		result["humidity"] = resp.data.device0.obs[0].humidity
		result["updatetime"] = resp.data.device0.obs[0].utctime
        }
    } catch (e) {
        log.error "error: $e"
    }

    return result
}
