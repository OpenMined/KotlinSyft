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
import org.openmined.syft.demo.datasource.LocalMNISTModuleDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTModuleRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.threading.ProcessSchedulers

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)
        binding.viewModel = initiateViewModel("192.168.1.13:5000")

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
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        val trainer = MNISTTrainer()
        return MainViewModelFactory(
            baseUrl,
            "auth",
            networkingSchedulers,
            computeSchedulers
        ).create(FederatedCycleViewModel::class.java)
    }
}
