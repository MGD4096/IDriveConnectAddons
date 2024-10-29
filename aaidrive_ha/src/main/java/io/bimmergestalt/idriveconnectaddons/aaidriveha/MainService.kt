package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import io.bimmergestalt.idriveconnectaddons.aaidriveha.ha.HaHttpClient
import io.bimmergestalt.idriveconnectaddons.lib.CDSLiveData
import io.bimmergestalt.idriveconnectkit.CDSProperty
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.net.URL

/**
 * Needs a service intent to be discoverable in the Addons UI
 */
class MainService: Service() {
	companion object {
		const val TAG = "AAIDriveHomeAssistant"
	}



	private val listOfProperties = listOf(
		CDSProperty.VEHICLE_VIN,
		CDSProperty.NAVIGATION_GPSPOSITION,
		CDSProperty.SENSORS_FUEL,
		CDSProperty.SENSORS_DOORS,
		CDSProperty.SENSORS_TRUNK,
		CDSProperty.SENSORS_LID,
		CDSProperty.CONTROLS_SUNROOF,
		CDSProperty.CONTROLS_WINDOWDRIVERFRONT,
		CDSProperty.CONTROLS_WINDOWDRIVERREAR,
		CDSProperty.CONTROLS_WINDOWPASSENGERREAR,
		CDSProperty.CONTROLS_WINDOWPASSENGERFRONT,
		CDSProperty.DRIVING_ECORANGE,
		CDSProperty.DRIVING_ODOMETER,
		CDSProperty.DRIVING_MODE,
		CDSProperty.DRIVING_AVERAGECONSUMPTION,
		CDSProperty.DRIVING_AVERAGESPEED,
		CDSProperty.SENSORS_BATTERY,
		CDSProperty.VEHICLE_UNITS
	)
	private val sc by lazy { ServerConfig() }
	private val haClient by lazy { HaHttpClient(sc) }
	private val properties by lazy { listOfProperties.map { it -> Pair<String, CDSLiveData>(it.propertyName.replace(".", "\u200B."), CDSLiveData(this.contentResolver, it)) }}
	private val propertyObserver by lazy { PropertyObserver(haClient) }

	override fun onCreate() {
		super.onCreate()
		Log.i(TAG, "Starting service")
		MainModel.isConnected.value = true
		properties.forEach { it -> it.second.observeForever(propertyObserver)}
	}

	override fun onBind(p0: Intent?): IBinder? {
		Log.i(TAG, "Binding service")
		return null
	}

	override fun onUnbind(intent: Intent?): Boolean {
		Log.i(TAG, "Unbinding service")
		return super.onUnbind(intent)
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.i(TAG, "Destroying service")
		MainModel.isConnected.value = false
		properties.forEach { it -> it.second.removeObserver(propertyObserver)}
	}
}

class PropertyObserver(private val haClient: HaHttpClient): Observer<Map<String, Any>> {
	var VIN:String?=null;
	private val currentDate = Clock.System.now()

	private val limitedRefreshProperty = mutableMapOf(
		Pair<String,Instant>("odometer", currentDate),
		Pair<String,Instant>("range", currentDate),
		Pair<String,Instant>("tanklevel", currentDate),
		Pair<String,Instant>("ecoRange", currentDate),
		Pair<String,Instant>("averageSpeed1", currentDate),
		Pair<String,Instant>("averageSpeed2", currentDate),
		Pair<String,Instant>("averageConsumption1", currentDate),
		Pair<String,Instant>("averageConsumption2", currentDate),
		Pair<String,Instant>("latitude", currentDate),
		Pair<String,Instant>("longitude", currentDate),
	)

	private val previousValue = mutableMapOf(
		Pair<String,Any?>("VIN", null),
		Pair<String,Any?>("odometer", null),
		Pair<String,Any?>("mode", null),
		Pair<String,Any?>("lid", null),
		Pair<String,Any?>("trunk", null),
		Pair<String,Any?>("range", null),
		Pair<String,Any?>("tanklevel", null),
		Pair<String,Any?>("ecoRange", null),
		Pair<String,Any?>("battery", null),
		Pair<String,Any?>("averageSpeed1", null),
		Pair<String,Any?>("averageSpeed2", null),
		Pair<String,Any?>("averageConsumption1", null),
		Pair<String,Any?>("averageConsumption2", null),
		Pair<String,Any?>("latitude", null),
		Pair<String,Any?>("longitude", null),
		Pair<String,Any?>("driver", null),
		Pair<String,Any?>("driverRear", null),
		Pair<String,Any?>("passengerRear", null),
		Pair<String,Any?>("passenger", null)
	)

	fun SameAsPreviousValue(key:String, value: Map<String, Any>, update:Boolean = true):Boolean{
		val flag = (previousValue[key] as String?).equals(value[key].toString());

		if(limitedRefreshProperty.containsKey(key) && limitedRefreshProperty[key]?.plus(1,
				DateTimeUnit.MINUTE)!! >Clock.System.now()){
			return true;
		}
		if(!flag && update){
			limitedRefreshProperty[key] = Clock.System.now();
			previousValue[key] = value[key].toString();
		}
		return flag;
	}

	fun ToEntityStateString(str:String):String{
		return "\""+str+"\""
	}

	fun GetDoorStatus(status:Int):String{
		if(status == 0){
			return "Closed"
		}
		if(status == 1){
			return "Open"
		}
		if(status == 2){
			return "Not Exist"
		}
		return "Unknown"
	}

	override fun onChanged(value: Map<String, Any>) {
		if(value.containsKey("VIN") && VIN == null){
			VIN = value["VIN"].toString();
			if(VIN!="")
				MainModel.VIN.value = VIN
				callApi(VIN as String,"vehicle_vin", ToEntityStateString(VIN.toString()))
		}
		else if(VIN != null){
			if(value.containsKey("odometer") && !SameAsPreviousValue("odometer",value)){
				val odometer = value["odometer"] as? Int ?: 0
				callApi(VIN as String,"odometer", odometer.toString())
			}

			if(value.containsKey("mode") && !SameAsPreviousValue("mode",value)){
				val driveMode = value["mode"] as? Int ?: 0
				var Mode = "Unknown"
				if(driveMode == 2){
					Mode = "Comfort"
				}
				else if(driveMode == 3){
					Mode = "Comfort"
				}
				else if(driveMode == 4){
					Mode = "Sport"
				}
				else if(driveMode == 5){
					Mode = "Sport+"
				}
				else if(driveMode == 6){
					Mode = "Race"
				}
				else if(driveMode == 7){
					Mode = "EcoPro"
				}
				else if(driveMode == 8){
					Mode = "EcoPro+"
				}
				else if(driveMode == 9){
					Mode = "Comfort+"
				}

				callApi(VIN as String,"driving_mode", ToEntityStateString(Mode))
			}

			if(value.containsKey("lid") && !SameAsPreviousValue("lid",value)){
				val lidStatus = value["lid"] as? Int ?: 0
				callApi(VIN as String,"lid", ToEntityStateString(GetDoorStatus(lidStatus)))
			}

			if(value.containsKey("trunk") && !SameAsPreviousValue("trunk",value)){
				val trunkStatus = value["trunk"] as? Int ?: 0
				callApi(VIN as String,"trunk", ToEntityStateString(GetDoorStatus(trunkStatus)))
			}
			if(value.containsKey("range")
				&& value.containsKey("tanklevel")
				&& !SameAsPreviousValue("range",value, false)
				&& !SameAsPreviousValue("tanklevel",value, false)){
					if(!SameAsPreviousValue("range",value))
						callApi(VIN as String,"fuel_range", value["range"].toString())
					if(!SameAsPreviousValue("tanklevel",value))
						callApi(VIN as String,"fuel_level", value["tanklevel"].toString())
			}
			if(value.containsKey("ecoRange") && !SameAsPreviousValue("ecoRange",value)){
				callApi(VIN as String,"fuel_eco_range", value["ecoRange"].toString())
			}
			if(value.containsKey("battery") && !SameAsPreviousValue("battery",value)){
				callApi(VIN as String,"battery", value["battery"].toString())
			}
			if(value.containsKey("averageSpeed1")
				&& value.containsKey("averageSpeed2")
				&& !SameAsPreviousValue("averageSpeed1",value,false)
				&& !SameAsPreviousValue("averageSpeed2",value,false)){
					if(!SameAsPreviousValue("averageSpeed1",value))
						callApi(VIN as String,"average_speed_1", ToEntityStateString(value["averageSpeed1"].toString()+" km/h"))
					if(!SameAsPreviousValue("averageSpeed2",value))
						callApi(VIN as String,"average_speed_2", ToEntityStateString(value["averageSpeed2"].toString()+" km/h"))
			}
			if(value.containsKey("driver")
				&& value.containsKey("driverRear")
				&& value.containsKey("passenger")
				&& value.containsKey("passengerRear")
				&& !SameAsPreviousValue("driver",value,false)
				&& !SameAsPreviousValue("passenger",value,false)
				&& !SameAsPreviousValue("driverRear",value,false)
				&& !SameAsPreviousValue("passengerRear",value,false)){
					if(!SameAsPreviousValue("driver",value))
						callApi(VIN as String,"driver_door", ToEntityStateString(GetDoorStatus(value["driver"] as? Int?: 0 )))
					if(!SameAsPreviousValue("passengerRear",value))
						callApi(VIN as String,"passenger_rear_door", ToEntityStateString(GetDoorStatus(value["passengerRear"] as? Int?: 0)))
					if(!SameAsPreviousValue("passenger",value))
						callApi(VIN as String,"passenger_door", ToEntityStateString(GetDoorStatus(value["passenger"] as? Int?: 0 )))
					if(!SameAsPreviousValue("driverRear",value))
						callApi(VIN as String,"driver_rear_door", ToEntityStateString(GetDoorStatus(value["driverRear"] as? Int?: 0 )))
			}
			if(value.containsKey("averageConsumption1")
				&& value.containsKey("averageConsumption2")
				&& !SameAsPreviousValue("averageConsumption1",value,false)
				&& !SameAsPreviousValue("averageConsumption2",value,false)){
					if(!SameAsPreviousValue("averageConsumption1",value))
						callApi(VIN as String,"average_consumption_1", ToEntityStateString(value["averageConsumption1"].toString()+" l/100km"))
					if(!SameAsPreviousValue("averageConsumption2",value))
						callApi(VIN as String,"average_consumption_2", ToEntityStateString(value["averageConsumption2"].toString()+" l/100km"))
			}
			if(value.containsKey("latitude")
				&& value.containsKey("longitude")
				&& !SameAsPreviousValue("latitude",value)
				&& !SameAsPreviousValue("longitude",value)){
					val attr = """,
						"attributes":{
							"source_type":"gps",
							"latitude": """+ToEntityStateString(value["latitude"].toString())+""",
							"longitude": """+ToEntityStateString(value["longitude"].toString())+""",
							"gps_accuracy": 150,
							"altitude": 0,
							"course": 0,
							"vertical_accuracy": 0,
							"speed": 0
						}
					""".trimMargin()
					callApi(VIN as String,"vehicle_position",ToEntityStateString("Location"), attr)
			}
		}
	}
	fun callApi(VIN:String, EntityName:String, EntityValue:String, EntityAttributes: String=""){
		GlobalScope.launch(Dispatchers.Default) {
			if(haClient.serverConfig.isAuthorized){
				val endpointUrl = Uri.parse(haClient.serverConfig.serverName)
				val uriBuilder = endpointUrl.buildUpon()
				uriBuilder.encodedPath("/api/states/sensor."+VIN+"_"+EntityName)
				haClient.send_post(
					URL(uriBuilder.toString()),
					"""{
							"state": """ + EntityValue + EntityAttributes +"""
							}
						"""
				)
			}
		}
	}
}