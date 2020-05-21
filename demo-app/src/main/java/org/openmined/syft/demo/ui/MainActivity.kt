package org.openmined.syft.demo.ui

import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.chart
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.openmined.syft.demo.BuildConfig
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityMainBinding
import org.openmined.syft.demo.datasource.LocalMNISTDataDataSource
import org.openmined.syft.demo.domain.MNISTDataRepository
import org.openmined.syft.domain.SyftConfiguration

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
        binding.viewModel = initiateViewModel(BuildConfig.SYFT_BASE_URL)

        (binding.viewModel as FederatedCycleViewModel).processState.observe(
            this,
            Observer { onProcessStateChanged(it) }
        )

        (binding.viewModel as FederatedCycleViewModel).processData.observe(
            this,
            Observer { onProcessData(it) }
        )

        (binding.viewModel as FederatedCycleViewModel).steps.observe(
            this,
            Observer { binding.step.text = it })
    }

    private fun onProcessData(it: ProcessData?) {
        processData(it ?: ProcessData(emptyList()))
    }

    private fun onProcessStateChanged(processState: ProcessState?) {
        when (processState) {
            ProcessState.Hidden -> progressBar.visibility = ProgressBar.GONE
            ProcessState.Loading -> progressBar.visibility = ProgressBar.VISIBLE
        }
    }

    private fun processData(processState: ProcessData) {
        // TODO do with fold
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

    private fun initiateViewModel(baseUrl: String): FederatedCycleViewModel {

        val config = SyftConfiguration.builder(this, baseUrl).build()
        val localMNISTDataDataSource = LocalMNISTDataDataSource(resources)
        val dataRepository = MNISTDataRepository(localMNISTDataDataSource)
        return MainViewModelFactory(
            "auth",
            config,
            dataRepository
        ).create(FederatedCycleViewModel::class.java)
    }
}
