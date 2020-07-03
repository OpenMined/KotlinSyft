package org.openmined.syft.demo.service

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.openmined.syft.demo.R

class WorkInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_info)
        setSupportActionBar(findViewById(R.id.toolbar))

    }
}