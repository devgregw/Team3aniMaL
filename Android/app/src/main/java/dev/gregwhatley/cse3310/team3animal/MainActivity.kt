package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.classification_results.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var classifier: Classifier
    private lateinit var bottomSheetDialog: BottomSheetDialog

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Classifier.CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            classifier.classifyCapturedPhoto {
                if (it == null) {
                    Snackbar.make(this@MainActivity.findViewById(android.R.id.content), "An error occurred while classifying this image.", Snackbar.LENGTH_SHORT).show()
                } else {
                    val labels = bottomSheetDialog.findViewById<LinearLayout>(R.id.sheet_label_layout)!!
                    val probabilities = bottomSheetDialog.findViewById<LinearLayout>(R.id.sheet_probability_layout)!!
                    labels.removeAllViews()
                    probabilities.removeAllViews()
                    bottomSheetDialog.findViewById<androidx.appcompat.widget.Toolbar>(R.id.bottom_sheet_toolbar)!!.apply {
                        title = "Result: ${it.values.last().toUpperCase(
                        Locale.ROOT
                    )}"
                        menu.getItem(0).setOnMenuItemClickListener {
                            bottomSheetDialog.dismiss()
                            true
                        }
                    }
                    it.keys.reversed().forEachIndexed { i, f ->
                        labels.addView(TextView(this@MainActivity).apply {
                            setTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle)
                            text = it[f]!!.capitalize(Locale.ROOT)
                        })
                        probabilities.addView(TextView(this@MainActivity).apply {
                            setTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle)
                            text = "%.2f".format(f * 100) + "%"
                        })
                    }
                    bottomSheetDialog.show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
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
            AlertDialog.Builder(this).setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("LOG OUT") { _: DialogInterface, _: Int ->
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
        classifier = Classifier(this)
        bottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(R.layout.classification_results)
            dismissWithAnimation = true
            setCanceledOnTouchOutside(true)
        }
        Snackbar.make(
            findViewById(android.R.id.content),
            "Logged in as ${if (FirebaseAuth.getInstance().currentUser!!.displayName?.isNotEmpty() == true) FirebaseAuth.getInstance().currentUser!!.displayName else FirebaseAuth.getInstance().currentUser!!.email}.",
            Snackbar.LENGTH_SHORT
        ).show()
        capture_button.setOnClickListener {
            classifier.capturePhoto()
        }
    }
}
