package org.openmined.syft.demo.federated.ui

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
import org.openmined.syft.domain.SyftConfiguration

const val AUTH_TOKEN = "authToken"
const val BASE_URL = "baseUrl"
private const val TAG = "MnistActivity"

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MnistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMnistBinding
    private lateinit var viewModel: MnistViewModel
    private val logger = Logger.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mnist)
        binding.lifecycleOwner = this
        setSupportActionBar(toolbar)
        val baseUrl = intent.getStringExtra("baseURL")
        val authToken = intent.getStringExtra("authToken")

        viewModel = initiateViewModel(baseUrl, authToken)

        binding.viewModel = viewModel

        logger.processState.observe(
            this,
            Observer { onProcessStateChanged(it) }
        )

        logger.processData.observe(
            this,
            Observer { onProcessData(it) }
        )

        logger.steps.observe(
            this,
            Observer { binding.step.text = it })
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

    private fun initiateViewModel(baseUrl: String?, authToken: String?): MnistViewModel {
        if (baseUrl == null || authToken == null)
            throw IllegalArgumentException("Mnist trainer called without proper arguments")
        return ViewModelProvider(
            this, MnistViewModelFactory(
                application,
                baseUrl,
                authToken
            )
        ).get(MnistViewModel::class.java)
    }
}
