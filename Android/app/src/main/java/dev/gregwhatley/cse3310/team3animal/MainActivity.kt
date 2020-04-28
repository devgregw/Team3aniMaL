package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.classification_results.*
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var classifier: Classifier
    private lateinit var bottomSheetDialog: BottomSheetDialog

    companion object {
        const val MENU_ACCOUNT = 2
        const val MENU_MODEL_INFO = 3
    }

    @ExperimentalStdlibApi
    private fun showResult(map: SortedMap<Float, String>) {
        // Find layouts for labels and probabilities in the bottom sheet.
        val labels = bottomSheetDialog.sheet_label_layout
        val probabilities = bottomSheetDialog.sheet_probability_layout

        // Empty previous results.
        labels.removeAllViews()
        probabilities.removeAllViews()

        // Configure the bottom sheet before displaying it
        bottomSheetDialog.bottom_sheet_toolbar.apply {
            title = "Result: ${map.values.last().toUpperCase(Locale.ROOT)}"
            menu.getItem(0).setOnMenuItemClickListener {
                bottomSheetDialog.dismiss()
                true
            }
        }

        bottomSheetDialog.sheet_button_correct.setOnClickListener {
            SessionStatistics.correct(map[map.keys.max()!!]!!)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.sheet_button_incorrect.setOnClickListener {
            SessionStatistics.incorrect(map[map.keys.max()!!]!!)
            bottomSheetDialog.dismiss()
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val dialog = ProgressDialogHelper.create(this, "Classifying...")
            dialog.show()
            classifier.classifyPhoto(data) {
                dialog.dismiss()
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
            menu.add(Menu.NONE, MENU_MODEL_INFO, 0, "ML Info").setIcon(R.drawable.ic_action_info)
                .setShowAsActionFlags(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM
                )
            menu.add(Menu.NONE, MENU_ACCOUNT, 0, "Account").setIcon(R.drawable.baseline_account_circle_24)
                .setShowAsActionFlags(
                    MenuItem.SHOW_AS_ACTION_ALWAYS
                )
            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
            Glide.with(this)
                .load(FirebaseAuth.getInstance().currentUser!!.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

                        return true
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Handler(mainLooper).post {
                            if (resource != null) menu.findItem(MENU_ACCOUNT).icon = resource
                        }
                        return true
                    }

                })
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .transform(RoundedCorners(size / 2))
                .submit(size, size)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (/*item.itemId == MENU_LOG_OUT*/item.itemId == android.R.id.home) {
            // Ask the user to confirm when logging out.
            AlertDialog.Builder(this).setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("LOG OUT") { _: DialogInterface, _: Int ->
                    // Confirmed, sign out and show log in activity
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, SplashScreenActivity::class.java))
                    finish()
                }.setNegativeButton("CANCEL", null).show()
        } else if (item.itemId == MENU_ACCOUNT)
            startActivity(Intent(this, AccountActivity::class.java))
        else if (item.itemId == MENU_MODEL_INFO)
            startActivity(Intent(this, ModelDetailActivity::class.java))
            //showModelInfo()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Home"

        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_action_logout)
            setDisplayHomeAsUpEnabled(true)
        }

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

        // When the capture button is tapped, pick a photo
        capture_button.setOnClickListener {
            classifier.capturePhoto()
        }
    }
}
