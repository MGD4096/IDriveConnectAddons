package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.app.Application
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.bimmergestalt.idriveconnectkit.CDSProperty
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData
import io.bimmergestalt.idriveconnectaddons.lib.CDSLiveData
import me.tatarka.bindingcollectionadapter2.ItemBinding

class MainModel(app: Application): AndroidViewModel(app) {

    companion object {
        val VIN = MutableLiveData("")
        val isConnected = MutableLiveData(false)
    }

    val listOfProperties = listOf(
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
        CDSProperty.VEHICLE_VIN,
        CDSProperty.DRIVING_ECORANGE,
        CDSProperty.DRIVING_ODOMETER,
        CDSProperty.DRIVING_MODE,
        CDSProperty.DRIVING_AVERAGECONSUMPTION,
        CDSProperty.DRIVING_AVERAGESPEED,
        CDSProperty.SENSORS_BATTERY,
        CDSProperty.VEHICLE_UNITS,
    )

    val datapoints = ObservableArrayList<Pair<String, CDSLiveData>>().apply {
        addAll(listOfProperties.map {
            Pair(it.propertyName.replace(".", "\u200B."), CDSLiveData(app.applicationContext.contentResolver, it))
        })
    }

    val cdsItemBinding: ItemBinding<Pair<String, CDSLiveData>> = ItemBinding.of(BR.item, R.layout.item_cds)

    fun getVin():String{
        return VIN.value?:"";
    }
    fun getConnectionStatus():String{
        if(isConnected.value==true)
            return "Yes";
        else
            return "No";
    }
}