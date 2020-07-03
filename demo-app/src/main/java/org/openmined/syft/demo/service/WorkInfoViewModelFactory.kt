package org.openmined.syft.demo.service

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class WorkInfoViewModelFactory(
    private val activity: AppCompatActivity
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkInfoViewModel::class.java))
            return WorkInfoViewModel(activity) as T
        throw IllegalArgumentException("unknown view model class")
    }
}