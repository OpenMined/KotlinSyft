package org.openmined.syft.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.openmined.syft.networking.clients.MessageProcessor
import org.openmined.syft.threading.ProcessSchedulers
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        injectMe()
        viewModel.process().subscribe()
    }

    private fun injectMe() {
        val computeSchedulers = object : ProcessSchedulers {
            override val computeThreadScheduler: Scheduler
                get() = Schedulers.computation()
            override val calleeThreadScheduler: Scheduler
                get() = Schedulers.single()
        }

        viewModel = MainViewModelFactory(
            computeSchedulers,
            resources,
            filesDir
        ).create(MainViewModel::class.java)
    }
}
