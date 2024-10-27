package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData
import org.json.JSONObject


object DataBindingHelpers {
    /**
     * Format a CDSLiveData to a pretty-printed JSON string
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(view: TextView, liveData: CDSLiveData) {
        val current = liveData.value
        if (current != null) {
            if(current.containsKey("VIN")){
                view.text = current["VIN"] as String;
            }
            else if(current.containsKey("odometer")){
                view.text = (current["odometer"] as Int).toString();
            }
            else if(current.containsKey("mode")){
                val driveMode = current["mode"] as? Int ?: 0
                var Mode = "Unknown"
                if(driveMode == 2){
                    Mode = "Comfort"
                }
                else if(driveMode == 3){
                    Mode = "Basic"
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

                view.text = Mode;
            }
            else if(current.containsKey("lid")){
                val lidStatus = current["lid"] as? Int ?: 0
                view.text = GetDoorStatus(lidStatus);
            }
            else if(current.containsKey("driver")
                && current.containsKey("driverRear")
                && current.containsKey("passenger")
                && current.containsKey("passengerRear")){
                view.text = "Driver Door:" + GetDoorStatus(current["driver"] as? Int ?: 0)+"\nDriver Rear Door:" + GetDoorStatus(current["driverRear"] as? Int ?: 0)+"\nPassenger Rear Door:" + GetDoorStatus(current["passengerRear"] as? Int ?: 0)+"\nPassenger Door:" + GetDoorStatus(current["passenger"] as? Int ?: 0);
            }
            else if(current.containsKey("trunk")){
                val trunkStatus = current["trunk"] as? Int ?: 0
                view.text = GetDoorStatus(trunkStatus);
            }else if(current.containsKey("tanklevel") && current.containsKey("range")){
                view.text = "Range: "+current["range"].toString()+"\n Fuel Level: "+current["tanklevel"].toString();
            }else if(current.containsKey("latitude") && current.containsKey("longitude")){
                view.text = "Latitude: "+current["latitude"].toString()+"\n Longitude: "+current["longitude"].toString();
            }else if(current.containsKey("ecoRange")){
                view.text = current["ecoRange"].toString();
            }else if(current.containsKey("battery")){
                view.text = current["battery"].toString()+"%";
            }else if(current.containsKey("averageSpeed1") && current.containsKey("averageSpeed2")){
                view.text = current["averageSpeed1"].toString()+" km/h \n" + current["averageSpeed2"].toString()+" km/h";
            }
            else if(current.containsKey("averageConsumption1") && current.containsKey("averageConsumption2")){
                view.text = current["averageConsumption1"].toString()+" l/100km \n" + current["averageConsumption2"].toString()+" l/100km";
            }
            else{
                view.text = current.toString()
            }
        } else {
            view.text = ""
        }
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
}

