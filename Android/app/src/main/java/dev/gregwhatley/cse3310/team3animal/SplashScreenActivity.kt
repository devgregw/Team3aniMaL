package dev.gregwhatley.cse3310.team3animal

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1024
    }

    private val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun arePermissionsGranted(): Boolean {
        // Check if each permissions is granted already
        return permissions.map { p -> ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED }.all { true }
    }

    private fun requestPermissions() {
        // Request all permissions
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val allTrue = grantResults.map { r -> r == PackageManager.PERMISSION_GRANTED }.all { true }
        if (requestCode == PERMISSIONS_REQUEST_CODE && allTrue)
            // Permissions granted - continue.
            checkAuth()
        else if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // TODO: handle rejected permissions
        }
    }

    private fun checkAuth() {
        // Wait for Firebase to load the current authentication state.
        FirebaseAuth.getInstance().addAuthStateListener {
            if (it.currentUser == null) {
                // Not logged in - show the log in activity.
                startActivity(Intent(this, LogInActivity::class.java))
                finish()
            } else {
                // Already logged in - show the main activity.
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        setTitle(R.string.app_full_name)
        // Check for necessary permissions.
        if (arePermissionsGranted())
            // No new permissions needed - continue.
            checkAuth()
        // Request permissions otherwise.
        else requestPermissions()
    }
}
