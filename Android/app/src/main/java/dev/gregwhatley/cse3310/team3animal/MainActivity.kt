package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var classifier: Classifier
    private lateinit var bottomSheetDialog: BottomSheetDialog

    @ExperimentalStdlibApi
    private fun showResult(map: SortedMap<Float, String>) {
        // Find layouts for labels and probabilities in the bottom sheet.
        val labels = bottomSheetDialog.findViewById<LinearLayout>(R.id.sheet_label_layout)!!
        val probabilities = bottomSheetDialog.findViewById<LinearLayout>(R.id.sheet_probability_layout)!!

        // Empty previous results.
        labels.removeAllViews()
        probabilities.removeAllViews()

        // Configure the bottom sheet before displaying it
        bottomSheetDialog.findViewById<androidx.appcompat.widget.Toolbar>(R.id.bottom_sheet_toolbar)!!.apply {
            title = "Result: ${map.values.last().toUpperCase(Locale.ROOT)}"
            menu.getItem(0).setOnMenuItemClickListener {
                bottomSheetDialog.dismiss()
                true
            }
        }

        // Get probabilities in descending order
        map.keys.reversed().forEach { probability ->
            // Add the corresponding label
            labels.addView(TextView(this@MainActivity).apply {
                setTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle)
                text = map[probability]!!.capitalize(Locale.ROOT)
            })

            // Add the probability formatted as a percent
            probabilities.addView(TextView(this@MainActivity).apply {
                setTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle)
                text = "${"%.2f".format(probability * 100)}%"
            })
        }
        bottomSheetDialog.show()
    }

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Classifier.CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // A photo was captured
            classifier.classifyCapturedPhoto {
                if (it == null) {
                    // No results returned, show an error message.
                    Snackbar.make(this@MainActivity.findViewById(android.R.id.content), "An error occurred while classifying this image.", Snackbar.LENGTH_SHORT).show()
                } else {
                    // Prepare the result sheet
                    showResult(it)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            // Initialize the options menu
            menu.add(Menu.NONE, 1, 0, "Log Out").setIcon(R.drawable.baseline_phonelink_lock_24)
                .setShowAsActionFlags(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM
                )
            menu.add(Menu.NONE, 2, 0, "Account").setIcon(R.drawable.baseline_account_circle_24)
                .setShowAsActionFlags(
                    MenuItem.SHOW_AS_ACTION_ALWAYS
                )
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            // Ask the user to confirm when logging out.
            AlertDialog.Builder(this).setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("LOG OUT") { _: DialogInterface, _: Int ->
                    // Confirmed, sign out and show log in activity
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, SplashScreenActivity::class.java))
                    finish()
                }.setNegativeButton("CANCEL", null).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Home"

        // Create an instance of the classifier abstraction.
        classifier = Classifier(this)

        // Get the bottom sheet ready.
        bottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(R.layout.classification_results)
            dismissWithAnimation = true
            setCanceledOnTouchOutside(true)
        }

        // Notify user of successful login.
        Snackbar.make(
            findViewById(android.R.id.content),
            "Logged in as ${if (FirebaseAuth.getInstance().currentUser!!.displayName?.isNotEmpty() == true) FirebaseAuth.getInstance().currentUser!!.displayName else FirebaseAuth.getInstance().currentUser!!.email}.",
            Snackbar.LENGTH_SHORT
        ).show()

        // When the capture button is tapped...
        capture_button.setOnClickListener {
            classifier.capturePhoto()
        }
    }
}
