package org.openmined.syft.demo.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.log_area
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.openmined.syft.datasource.ModuleDataSource
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityMainBinding
import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.domain.ModelRepository
import org.openmined.syft.networking.clients.HttpClient
import org.openmined.syft.networking.clients.SocketClient
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "MainActivity"

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)
        binding.lifecycleOwner = this
        binding.viewModel = initiateViewModel("10.0.2.2:5000")

//        (binding.viewModel as FederatedCycleViewModel).cycleState.observe(this, Observer {
//            Log.d(TAG, "Received IValue from training")
//            log_area.append(it.toString())
//        })
    }

    private fun initiateViewModel(baseUrl: String): FederatedCycleViewModel {
        val networkingSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.io()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }
        val computeSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }

        val localModuleDataSource = LocalMNISTModuleDataSource(resources, filesDir)
        val moduleRepository = MNISTModuleRepository(localModuleDataSource)

        val localConfiguration = LocalConfiguration(filesDir.absolutePath, filesDir.absolutePath)
        val moduleDataSource = ModuleDataSource(localConfiguration)
        val modelRepository = ModelRepository(moduleDataSource)
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        val trainer = MNISTTrainer()
        return MainViewModelFactory(
            dataRepository,
            trainer,
            modelRepository,
            baseUrl,
            "auth",
            networkingSchedulers,
            computeSchedulers,
            localConfiguration
        ).create(FederatedCycleViewModel::class.java)
    }
}
