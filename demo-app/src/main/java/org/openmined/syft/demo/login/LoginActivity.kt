package org.openmined.syft.demo.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.openmined.syft.demo.R
import org.openmined.syft.demo.databinding.ActivityLoginBinding
import org.openmined.syft.demo.federated.ui.MnistActivity

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        loginViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(LoginViewModel::class.java)


        binding.button.setOnClickListener {
            val baseUrl = binding.url.text.toString()
            val valid = loginViewModel.checkUrl(baseUrl)
            if (valid) {
                val intent = Intent(this, MnistActivity::class.java)
                intent.putExtra("baseURL", baseUrl)
                intent.putExtra("authToken", "auth")
                startActivity(intent)
            } else {
                binding.error.text = getString(R.string.error_url)
                binding.error.visibility = TextView.VISIBLE
            }
        }

        binding.url.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    binding.button.performClick()
                    true
                }
                else -> false
            }
        }
    }
}