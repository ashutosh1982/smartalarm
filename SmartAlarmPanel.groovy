/**
 *  SmartAlarm Control Panel.
 *
 *  This device handler implements virtual control panel for Smart Alarm app.
 *
 *  Version: 0.2 (01/27/2015)
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartalarm/blob/master/SmartAlarmPanel.groovy
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2015 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

metadata {
    definition (name:"SmartAlarm Control Panel", namespace:"statusbits", author:"geko@statusbits.com") {
        capability "Switch"
        capability "Alarm"
        capability "Refresh"

        // Custom attributes
        attribute "status", "string"
        attribute "display", "string"

        // Custom commands
        command "parse", ["string"]     // parse(<status>)
        command "armAway"               // arm in 'Away' mode
        command "armStay"               // arm in 'Stay' mode
        command "disarm"                // disarm
        command "panic"                 // panic alarm
    }

    tiles {
        standardTile("status", "device.status") {
            state "default",    label:'Not Ready',  backgroundColor:"#999999", icon:"st.security.alarm.off"
            state "disarmed",   label:'Disarmed',   backgroundColor:"#7FCC33", icon:"st.security.alarm.off"
            state "armedAway",  label:'Armed Away', backgroundColor:"#0099FF", icon:"st.security.alarm.on"
            state "armedStay",  label:'Armed Stay', backgroundColor:"#0099FF", icon:"st.security.alarm.partial"
            state "alarm",      label:'Alarm',      backgroundColor:"#FF3300", icon:"st.security.alarm.alarm"
        }

        valueTile("display", "device.display", width:3, height:1, decoration:"flat", inactiveLabel:false) {
            state "default", label:'${currentValue}'
        }

        standardTile("armAway", "device.status", decoration:"flat", inactiveLabel:false) {
            state "default", label:'Arm Away', icon:"st.security.alarm.on",action:"armAway"
        }

        standardTile("armStay", "device.status", decoration:"flat", inactiveLabel:false) {
            state "default", label:'Arm Stay', icon:"st.security.alarm.partial", action:"armStay"
        }

        standardTile("disarm", "device.status", decoration:"flat", inactiveLabel:false) {
            state "default", label:'Disarm', icon:"st.security.alarm.off", action:"disarm"
        }

        standardTile("panic", "device.status", decoration:"flat", inactiveLabel:false) {
            state "default", label:'Panic', icon:"st.security.alarm.alarm", action:"panic"
        }

        standardTile("refresh", "device.status", decoration:"flat", inactiveLabel:false) {
            state "default", icon:"st.secondary.refresh", action:"refresh.refresh"
        }

        main("status")

        details([
            "display",
            "disarm", "armAway", "armStay",
            "panic", "status", "refresh"
        ])
    }

    preferences {
        input("defaultMode", "enum", title:"Default Arming Mode", required:true,
            metadata:[values:["Away","Stay"]], defaultValue:"Away",
            displayDuringSetup:true)
    }

    simulator {
        status "Armed Away":    "status: armed, mode: away"
        status "Armed Stay":    "status: armed, mode: stay"
        status "Disarmed":      "status: disarmed"
        status "Alarm":         "status: alarm, reason: Entrance Door"
        status "Panic":         "status: alarm, reason: panic"
    }
}

def updated() {
    log.info "SmartAlarm Control Panel. Version 0.2. Copyright © 2015 Statusbits.com"
    LOG("updated with ${settings}")

    LOG("state: ${state}")
}

// Parse events
def parse(String message) {
    LOG("parse(${message})")

    if (message == "updated") {
        initialize()
        return null
    }

    def map = stringToMap(message)
    if (map?.status == null) {
        log.warn "Cannot parse '${message}'"
        return null
    }

    switch (map.status) {
        case "armed":
            statusArmed(map.mode)
            break

        case "disarmed":
            statusDisarmed()
            break

        case "alarm":
            statusAlarm(map.reason)
            break
    }

    return null
}

// "armAway" command handler
def armAway() {
    LOG("armAway()")
}

// "armStay" command handler
def armStay() {
    LOG("armStay()")
}

// "disarm" command handler
def disarm() {
    LOG("disarm()")
}

// "panic" command handler
def panic() {
    LOG("panic()")
}

// "alarm.siren" command handler
def siren() {
    LOG("siren()")
    panic()
}

// "alarm.strobe" command handler
def strobe() {
    LOG("strobe()")
    panic()
}

// "alarm.both" command handler
def both() {
    LOG("both()")
    panic()
}

// "alarm.off" and "switch.off" command handler
def off() {
    LOG("off()")

    if (device.currentValue("status") == "alarm") {
        disarm()
    }
}

// "switch.on" command handler
def on() {
    LOG("on()")
}

// "refresh.refresh" command handler
def refresh() {
    LOG("refresh()")
    STATE()
}

private def initialize() {
    LOG("initialize()")
}

// Armed status handler
private def statusArmed(mode) {
    LOG("statusArmed(${mode})")

    def event = [
        name:               "status",
        value:              "armed ${mode}",
        descriptionText:    "${device.displayName} ARMED ${mode}",
        isStateChange:      true
    ]

    LOG("sending event ${event}")
    sendEvent(event)
}

// Disarmed status handler
private def statusDisarmed() {
    LOG("statusDisarmed()")

    def event = [
        name:               "status",
        value:              "disarmed",
        descriptionText:    "${device.displayName} DISARMED",
        isStateChange:      true
    ]

    LOG("sending event ${event}")
    sendEvent(event)
}

// Alarm status handler
private def statusAlarm(reason) {
    LOG("statusAlarm(${reason})")

    def event = [
        name:               "status",
        value:              "alarm",
        descriptionText:    "${device.displayName} ALARM: ${reason}",
        isStateChange:      true
    ]

    LOG("sending event ${event}")
    sendEvent(event)
}

private def LOG(message) {
    log.debug message
}

private def STATE() {
    log.debug "state: ${state}"
    log.debug "state: ${device.currentValue("status")}"
    log.debug "display: ${device.currentValue("display")}"
}
