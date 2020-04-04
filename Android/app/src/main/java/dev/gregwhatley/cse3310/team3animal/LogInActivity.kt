package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        log_in.setOnClickListener {
            if (email_address.text.isEmpty() or password.text.isEmpty()) {
                Snackbar.make(root,"An email address and password is required to log in.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val dialog = ProgressDialog(this).apply {
                title = "Logging in..."
                show()
            }
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken ?: View(this).windowToken, 0)
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_address.text.toString(), password.text.toString()).addOnSuccessListener {
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                dialog.dismiss()
                Snackbar.make(root,"Incorrect email address or password.", Snackbar.LENGTH_LONG).show()
                password.setText("")
            }
        }
    }
}
