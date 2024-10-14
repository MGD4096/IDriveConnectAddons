package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.app.Application
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.AndroidViewModel
import io.bimmergestalt.idriveconnectkit.CDSProperty
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData
import io.bimmergestalt.idriveconnectaddons.lib.CDSLiveData
import me.tatarka.bindingcollectionadapter2.ItemBinding

class MainModel(app: Application): AndroidViewModel(app) {
    val listOfProperties = listOf(
        CDSProperty.NAVIGATION_GPSPOSITION.propertyName,
        CDSProperty.SENSORS_FUEL.propertyName,
        CDSProperty.SENSORS_DOORS.propertyName,
        CDSProperty.SENSORS_TRUNK.propertyName,
        CDSProperty.SENSORS_LID.propertyName,
        CDSProperty.CONTROLS_SUNROOF.propertyName,
        CDSProperty.CONTROLS_WINDOWDRIVERFRONT.propertyName,
        CDSProperty.CONTROLS_WINDOWDRIVERREAR.propertyName,
        CDSProperty.CONTROLS_WINDOWPASSENGERREAR.propertyName,
        CDSProperty.CONTROLS_WINDOWPASSENGERFRONT.propertyName,
        CDSProperty.VEHICLE_VIN.propertyName,
        CDSProperty.VEHICLE_TYPE.propertyName,
        CDSProperty.DRIVING_PARKINGBRAKE.propertyName,
        CDSProperty.DRIVING_ECORANGE.propertyName,
        CDSProperty.DRIVING_MODE.propertyName,
        CDSProperty.DRIVING_AVERAGECONSUMPTION.propertyName,
        CDSProperty.DRIVING_AVERAGESPEED.propertyName,
        CDSProperty.SENSORS_BATTERY.propertyName,
        CDSProperty.VEHICLE_UNITS.propertyName,
    )
    val datapoints = ObservableArrayList<Pair<String, CDSLiveData>>().apply {
        addAll(CDSProperty.values().filter {  listOfProperties.contains(it.propertyName)}.map {
            Pair(it.propertyName.replace(".", "\u200B."), CDSLiveData(app.applicationContext.contentResolver, it))
        })
    }

    val cdsItemBinding: ItemBinding<Pair<String, CDSLiveData>> = ItemBinding.of(BR.item, R.layout.item_cds)
}