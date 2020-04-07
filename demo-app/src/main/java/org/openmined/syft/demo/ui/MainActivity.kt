package org.openmined.syft.demo.ui

import android.opengl.Visibility
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.progressBar
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

        (binding.viewModel as FederatedCycleViewModel).processState.observe(this, Observer { onProcessStateChanged(it) })
    }

    private fun onProcessStateChanged(processState: ProcessState?) {
        when (processState) {
            ProcessState.Hidden -> progressBar.visibility = ProgressBar.GONE
            ProcessState.Loading -> progressBar.visibility = ProgressBar.VISIBLE
        }
    }

    private fun initiateViewModel(baseUrl: String): FederatedCycleViewModel {
        val networkingSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.single()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }
        val computeSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.single()
            override val calleeThreadScheduler: Scheduler
                get() = AndroidSchedulers.mainThread()
        }

        val localConfiguration = LocalConfiguration(
            filesDir.absolutePath,
            filesDir.absolutePath,
            filesDir.absolutePath
        )
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
