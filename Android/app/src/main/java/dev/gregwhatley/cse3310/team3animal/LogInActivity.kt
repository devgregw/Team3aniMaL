package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        setTitle(R.string.app_full_name)
        log_in.setOnClickListener {
            // Firebase throws an exception if email_address or password is empty, so show a message.
            if (email_address.text.isEmpty() or password.text.isEmpty()) {
                Snackbar.make(root,"An email address and password is required to log in.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Open up a loading dialog while Firebase authenticates.
            val dialog = ProgressDialog(this).apply {
                setMessage("Logging in...")
                show()
            }

            // Dismiss the keyboard
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken ?: View(this).windowToken, 0)

            // Start authenticating
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_address.text.toString(), password.text.toString()).addOnSuccessListener {
                // Successful, open the main activity
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                // Failure, show a message and clear the password
                dialog.dismiss()
                Snackbar.make(root,"Incorrect email address or password.", Snackbar.LENGTH_LONG).show()
                password.setText("")
            }
        }
    }
}
