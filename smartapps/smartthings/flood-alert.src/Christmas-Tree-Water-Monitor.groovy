/**
 *  Copyright 2015 SmartThings
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
 *  Christmas Tree Water Monitor
 *
 *  Author: SmartThings Adapted by Palzer
 */
definition(
    name: "Christmas Tree Water Monitor",
    namespace: "smartthings",
    author: "Palzer",
    description: "Get a push notification or text message when the Christmas tree is dry.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png"
)

preferences {
	section("When there's no water detected...") {
		input "alarm", "capability.waterSensor", title: "Where?"
	}
	section("Send a notification to...") {
		input("recipients", "contact", title: "Recipients", description: "Send notifications to") {
			input "phone", "phone", title: "Phone number?", required: false
		}
	}
}

def installed() {
	subscribe(alarm, "water.dry", waterDryHandler)
}

def updated() {
	unsubscribe()
	subscribe(alarm, "water.dry", waterDryHandler)
}

def waterDryHandler(evt) {
	def deltaSeconds = 60

	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = alarm.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "dry" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent within the last $deltaSeconds seconds"
	} else {
		def msg = "${alarm.displayName} is dry!"
		log.debug "$alarm is dry, texting phone number"

		if (location.contactBookEnabled) {
			sendNotificationToContacts(msg, recipients)
		}
		else {
			sendPush(msg)
			if (phone) {
				sendSms(phone, msg)
			}
		}
	}
}

