# SmartThings_Lacrosse

Virtual Device Handler for Samsung Smartthings for use with [Lacrosse Alerts Mobile API](http://lacrossealertsmobile.com/)

## Pre-requisities

* Samsung Smartthings Hub
* Lacrosse Temperature Probe
  * Only Model TX60 tested
* Lacross Alerts Mobile Account, Gateway and Device working

## Setup

* Import in your IDE the devicehandler and publish for yourself
* Create new device, change deviceId in preferences

## Identify your DeviceId

* When logged into Lacrosse Alerts Mobile the deviceId should be visible in the URL when you visit the device specific information page:
  * ex: http://lacrossealertsmobile.com/v1.2/#device-XXXX-deviceid-XXXX
  * where XXXX-deviceid-XXXX is your Id

## Features

* Sensors
  * Probe Temperature 
  * Device Humidity
* Polls every 10 minutes

## Todo

* Add Device Temperature
