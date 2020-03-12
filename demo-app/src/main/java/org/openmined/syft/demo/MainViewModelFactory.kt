package org.openmined.syft.demo

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.threading.ProcessSchedulers
import java.io.File

class MainViewModelFactory(
    private val schedulerProvider: ProcessSchedulers,
    private val resources: Resources,
    private val filesDir: File
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(schedulerProvider, resources, filesDir) as T
    }
}
