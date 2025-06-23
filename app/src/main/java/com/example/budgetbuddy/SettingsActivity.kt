package com.example.budgetbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbuddy.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
            // Implements password change logic or open a new activity
        }

        binding.btnClearData.setOnClickListener {
            Toast.makeText(this, "Clear Data clicked", Toast.LENGTH_SHORT).show()
            // Implements data clearing logic
        }

        binding.btnAbout.setOnClickListener {
            //startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}