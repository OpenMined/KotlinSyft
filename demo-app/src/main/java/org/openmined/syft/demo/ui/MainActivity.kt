package org.openmined.syft.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityMainBinding
import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.LocalConfiguration
import org.openmined.syft.threading.ProcessSchedulers

private const val TAG = "MainActivity"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)
        binding.lifecycleOwner = this
        binding.viewModel = initiateViewModel("10.0.2.2:5000")
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

        val localConfiguration = LocalConfiguration(
            filesDir.absolutePath,
            filesDir.absolutePath,
            filesDir.absolutePath)
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        return MainViewModelFactory(
            baseUrl,
            "auth",
            dataRepository,
            networkingSchedulers,
            computeSchedulers,
            localConfiguration
        ).create(FederatedCycleViewModel::class.java)
    }
}
