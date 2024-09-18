package tv.vizbee.screendemo.ui.account

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import tv.vizbee.screendemo.databinding.ActivityAccountBinding
import tv.vizbee.screendemo.ui.signin.SignInActivity

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var viewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.signOutButton.setOnClickListener {
            viewModel.signOut()
        }
    }

    private fun observeViewModel() {
        viewModel.accountState.observe(this) { state ->
            when (state) {
                is AccountState.SignedIn -> {
                    binding.accountName.text = state.email
                    binding.signInButton.isVisible = false
                    binding.signOutButton.isVisible = true
                }
                is AccountState.SignedOut -> {
                    binding.accountName.text = "Not signed in"
                    binding.signInButton.isVisible = true
                    binding.signOutButton.isVisible = false
                }
            }
        }

        viewModel.signOutResult.observe(this) { result ->
            when (result) {
                is SignOutResult.Success -> {
                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SignOutResult.Error -> {
                    Toast.makeText(this, "Sign out failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}