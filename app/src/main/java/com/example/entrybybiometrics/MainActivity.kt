package com.example.entrybybiometrics

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.entrybybiometrics.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallBack: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication success!")
                    startActivity(Intent(this@MainActivity, SecretActivity::class.java))
                }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkBiometricSupport()

        binding.btn.setOnClickListener {
            val biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Title or prompt")
                .setSubtitle("Authentication is required")
                .setDescription("This app uses fingerprint to keep your data secure")
                .setNegativeButton("Cancel", this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                    notifyUser("Authentication cancelled")
                }).build()
            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallBack)
        }

    }

    private fun getCancellationSignal() : CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was cancelled by the user")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            notifyUser("Fingerprint, authentication has not been enabled in settings")
            return false
        }

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint, authentication, permission is not enabled")
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}