package org.openmined.syft.demo.federated.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_mnist.chart
import kotlinx.android.synthetic.main.activity_mnist.progressBar
import kotlinx.android.synthetic.main.activity_mnist.toolbar
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityMnistBinding
import org.openmined.syft.demo.federated.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.federated.domain.MNISTDataRepository
import org.openmined.syft.demo.federated.service.WorkerRepository
import org.openmined.syft.demo.federated.ui.ContentState
import org.openmined.syft.demo.federated.ui.ProcessData
import org.openmined.syft.domain.SyftConfiguration

const val AUTH_TOKEN = "authToken"
const val BASE_URL = "baseUrl"
private const val TAG = "MnistActivity"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMnistBinding
    private lateinit var viewModel: MnistActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mnist)
        binding.lifecycleOwner = this
        setSupportActionBar(toolbar)

        this.viewModel = initiateViewModel(
            intent.getStringExtra("baseURL"),
            intent.getStringExtra("authToken")
        )
        binding.viewModel = this.viewModel

        viewModel.getRunningWorkInfo()?.observe(this, viewModel.getWorkInfoObserver())

        binding.buttonFirst.setOnClickListener { launchForegroundCycle() }
        binding.buttonSecond.setOnClickListener { launchBackgroundCycle() }

        viewModel.processState.observe(
            this,
            Observer { onProcessStateChanged(it) }
        )

        viewModel.processData.observe(
            this,
            Observer { onProcessData(it) }
        )

        viewModel.steps.observe(
            this,
            Observer { binding.step.text = it })
    }


    private fun launchBackgroundCycle() {
        viewModel.submitJob().observe(this, viewModel.getWorkInfoObserver())
    }

    private fun launchForegroundCycle() {
        val config = SyftConfiguration.builder(this, viewModel.baseUrl)
                .setCacheTimeout(0L)
                .build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        viewModel.launchForegroundTrainer(config, dataRepository)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.disposeTraining()
        finish()
    }

    private fun onProcessData(it: ProcessData?) {
        processData(
            it ?: ProcessData(
                emptyList()
            )
        )
    }

    private fun onProcessStateChanged(contentState: ContentState?) {
        when (contentState) {
            ContentState.Training -> {
                progressBar.visibility = ProgressBar.GONE
                binding.chartHolder.visibility = View.VISIBLE
            }
            ContentState.Loading -> {
                progressBar.visibility = ProgressBar.VISIBLE
                binding.chartHolder.visibility = View.GONE
            }
        }
    }

    private fun processData(processState: ProcessData) {
        val entries = mutableListOf<Entry>()
        processState.data.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }
        val dataSet = LineDataSet(entries, "accuracy")
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.setMaxVisibleValueCount(0)
        chart.setNoDataText("Waiting for data")
        chart.invalidate()
    }

    private fun initiateViewModel(baseUrl: String?, authToken: String?): MnistActivityViewModel {
        if (baseUrl == null || authToken == null)
            throw IllegalArgumentException("Mnist trainer called without proper arguments")
        return ViewModelProvider(
            this,
            MnistViewModelFactory(
                baseUrl,
                authToken,
                WorkerRepository(this)
            )
        ).get(MnistActivityViewModel::class.java)
    }
}
