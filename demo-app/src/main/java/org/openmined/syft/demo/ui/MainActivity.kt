package org.openmined.syft.demo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.log_area
import kotlinx.android.synthetic.main.activity_main.start_btn
import org.openmined.syft.demo.R
import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.datasource.LocalMNISTModuleDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.demo.domain.MNISTModuleRepository
import org.openmined.syft.demo.domain.MNISTTrainer
import org.openmined.syft.threading.ProcessSchedulers

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        injectMe()
        viewModel.trainingState.observe(this, Observer {
            log_area.append(it.toString())
        })
        start_btn.setOnClickListener { viewModel.process().subscribe() }
    }

    private fun injectMe() {
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
        viewModel = MainViewModelFactory(
            computeSchedulers,
            moduleRepository,
            dataRepository,
            trainer
        ).create(MainViewModel::class.java)
    }
}
