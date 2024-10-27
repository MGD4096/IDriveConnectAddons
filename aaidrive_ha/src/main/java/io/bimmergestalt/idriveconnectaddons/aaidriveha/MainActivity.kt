package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableList
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfig
import io.bimmergestalt.idriveconnectaddons.aaidriveha.databinding.ActivityMainBinding
import io.bimmergestalt.idriveconnectaddons.aaidriveha.ha.HaHttpClient
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
	val mainModel by viewModels<MainModel>()
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityMainBinding.inflate(layoutInflater)
		binding.lifecycleOwner = this
		binding.viewModel = mainModel
		setContentView(binding.root)

		if (ContextCompat.checkSelfPermission(this, "bimmergestalt.permission.CDS_personal") != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf("bimmergestalt.permission.CDS_personal"), 0)
		}
	}
}