package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.content_account.*
import java.lang.Exception
import java.util.*

class AccountActivity : AppCompatActivity() {
    companion object {
        const val MENU_EDIT = 2
        const val UPDATE_REQUEST_CODE = 1
    }

    private var loadedProfileImage = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If the user finished updating their profile, reload the info
        if (requestCode == UPDATE_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            recreate()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setProfileImage(drawable: Drawable?) {
        // Sets the profile image on the main thread if it hasn't been done already
        if (loadedProfileImage)
            return
        else loadedProfileImage = drawable != null

        Handler(this@AccountActivity.mainLooper).post {
            profile_image.setImageDrawable(drawable)
        }
    }

    // Setup menu options
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.add(Menu.NONE, MENU_EDIT, 0, "Edit Profile")?.setIcon(R.drawable.ic_action_edit)
            ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Show profile editor
        if (item.itemId == MENU_EDIT) {
            startActivityForResult(Intent(this, RegistrationActivity::class.java).apply {
                putExtra("edit", true)
            }, UPDATE_REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // User is somehow not logged in, return to the splash screen
            startActivity(Intent(this, SplashScreenActivity::class.java))
            finish()
            return
        }

        title = user.displayName?.ifEmpty { "No Name" } ?: "No Name"
        email_address.text = user.email

        // Loading profile image
        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics).toInt()
        Glide.with(this@AccountActivity)
            .load(user.photoUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    //setProfileImage(null)
                    return true
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    setProfileImage(resource)
                    return true
                }

            })
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transform(RoundedCorners(size / 2))
            .submit(size, size)

        // Loading profile info from database
        FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                AlertDialog.Builder(this@AccountActivity)
                    .setTitle("Error")
                    .setMessage("An error occurred while loading account information.")
                    .setPositiveButton("Dismiss") { _, _ -> finish()}
                    .show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val map = p0.value!! as HashMap<String, Any>
                val addressMap = map["address"] as HashMap<String, Any>
                // Changing the views must be done on the main thread
                Handler(this@AccountActivity.mainLooper).post {
                    // Setting account creation date
                    val formatter =
                        SimpleDateFormat("MMMM d, yyyy")
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = FirebaseAuth.getInstance().currentUser!!.metadata!!.creationTimestamp
                    date.text = "Registered on ${formatter.format(calendar.time)}"

                    // Setting UTA ID and profession
                    uta_id.text = map["id"]?.toString()
                    profession.text = map["profession"].toString()

                    // Address
                    var addressText = addressMap["line1"]?.toString() ?: "Undefined"
                    if (addressMap.containsKey("line2"))
                        addressText += "\n" + addressMap["line2"].toString()
                    addressText += "\n${addressMap["city"]?.toString()} ${resources.getStringArray(R.array.states)[addressMap["state"]?.toString()?.toInt() ?: 0]}, ${addressMap["zip"]?.toString()}"
                    address.text = addressText

                    // Hide the spinner and show the profile info
                    progress_circular.animate().setDuration(250L).alpha(0f).setUpdateListener {
                        if (it.animatedFraction > 0.95f)
                            progress_circular.visibility = View.GONE
                    }.start()
                    account_layout.alpha = 0f
                    account_layout.visibility = View.VISIBLE
                    account_layout.animate().setDuration(250L).alpha(1f).start()
                }
            }

        })
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        setSupportActionBar(toolbar)

        // Back button
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener { finish() }

        // Load profile info from the database
        loadProfile()
    }
}
