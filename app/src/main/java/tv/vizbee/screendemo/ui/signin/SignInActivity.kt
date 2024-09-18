package tv.vizbee.screendemo.ui.signin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import tv.vizbee.screendemo.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        setupObservers()
        viewModel.requestCode()
    }

    override fun onStart() {
        super.onStart()
        viewModel.startPolling()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPolling()
    }

    private fun setupObservers() {
        viewModel.regCode.observe(this) { regCode ->
            binding.signInRegCode.text = regCode
        }

        viewModel.signInState.observe(this) { state ->
            when (state) {
                is SignInState.Loading -> {
                    // Show loading indicator if needed
                }
                is SignInState.Success -> {
                    // Handle successful sign-in
                    Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SignInState.Error -> {
                    // Handle error
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}