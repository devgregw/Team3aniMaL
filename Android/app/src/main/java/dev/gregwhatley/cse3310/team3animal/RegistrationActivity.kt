package dev.gregwhatley.cse3310.team3animal

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.content_registration.*

class RegistrationActivity : AppCompatActivity() {
    private var profileImage: Bitmap? = null

    // Show the selected profile image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            val size = 512
            profileImage = BitmapFactory.decodeFile(result.uri.path)
            Glide.with(this)
                .load(profileImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .transform(RoundedCorners(size / 2))
                .override(size)
                .into(profile_image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Shows an error for a text input field
    private fun setTextInputError(field: TextInputEditText, message: String?) {
        field.error = message
    }

    // Checks if a text input field is invalid and sets an error if so
    private fun isFieldInvalid(field: TextInputEditText, errorMessage: String?): Boolean {
        if (field.text?.toString().isNullOrBlank()) {
            Log.d("Invalid Field", errorMessage ?: field.id.toString())
            setTextInputError(field, errorMessage)
            return true
        }
        return false
    }

    // Checks if a number input field is invalid or does not have the required number of digits.  If so, shows an error
    private fun isNumberInvalid(field: TextInputEditText, numDigits: Int, errorMessage: String?): Boolean {
        if (isFieldInvalid(field, errorMessage))
            return true
        if ((field.text?.toString()?.toIntOrNull() == null) or (field.text?.toString()?.length != numDigits)) {
            setTextInputError(field, errorMessage)
            return true
        }
        return false
    }

    // Sets enabled state for all fields and buttons
    private fun setFieldsEnabled(value: Boolean) {
        arrayOf(fab, name, email_address, select_image, uta_id, profession, address1, address2, city, state, zip, password, password_confirm)
            .forEach { v -> v.isEnabled = value }
    }

    // Loads existing profile info for account editing
    private fun populateFields() {
        val dialog = ProgressDialogHelper.create(this, "Please wait...")
        dialog.show()
        val user = FirebaseAuth.getInstance().currentUser!!
        FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                dialog.dismiss()
                AlertDialog.Builder(this@RegistrationActivity)
                    .setTitle("Error")
                    .setMessage("Unable to load your profile.")
                    .setCancelable(false)
                    .setPositiveButton("Dismiss") { _, _ -> finish()}.show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                Handler(this@RegistrationActivity.mainLooper).post {
                    dialog.dismiss()
                    val map = p0.value!! as Map<String, Any>
                    val address = map["address"] as Map<String, Any>
                    name.setText(FirebaseAuth.getInstance().currentUser!!.displayName!!)
                    email_address.setText(FirebaseAuth.getInstance().currentUser!!.email!!)
                    uta_id.setText(map["id"]?.toString())
                    profession.setText(map["profession"]?.toString())
                    address1.setText(address["line1"]?.toString())
                    address2.setText(address["line2"]?.toString())
                    city.setText(address["city"]?.toString())
                    state.setSelection(address["state"]?.toString()?.toInt() ?: 0)
                    zip.setText(address["zip"]?.toString())

                    // Load profile image
                    val size = 512
                    Glide.with(this@RegistrationActivity)
                        .load(FirebaseAuth.getInstance().currentUser!!.photoUrl!!)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .transform(RoundedCorners(size / 2))
                        .override(size)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                // An error occurred, show the default icon
                                profileImage = BitmapFactory.decodeResource(
                                    this@RegistrationActivity.resources,
                                    R.drawable.baseline_account_circle_24
                                )
                                profile_image.setImageBitmap(profileImage!!)
                                return true
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (resource == null) {
                                    profileImage = BitmapFactory.decodeResource(
                                        this@RegistrationActivity.resources,
                                        R.drawable.baseline_account_circle_24
                                    )
                                    Handler(this@RegistrationActivity.mainLooper).post {
                                        profile_image.setImageBitmap(profileImage!!)
                                    }
                                } else {
                                    Handler(this@RegistrationActivity.mainLooper).post {
                                        profile_image.setImageDrawable(resource)
                                        profileImage =
                                            profile_image.drawToBitmap(Bitmap.Config.ARGB_8888)
                                    }
                                }
                                return true
                            }

                        }).submit()

                    password_prompt.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setSupportActionBar(toolbar)
        val isEditing = intent.getBooleanExtra("edit", false)
        title = if (isEditing) "Update Profile" else "Create Account"
        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener { finish() }
        select_image.setOnClickListener {
            CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setRequestedSize(512, 512)
                .start(this)
        }

        // If the user is editng their account
        if (intent.getBooleanExtra("edit", false))
            populateFields()

        // Save button
        fab.setOnClickListener { view ->
            val name = name.text?.toString()
            val email = email_address.text?.toString()
            val id = uta_id.text?.toString()
            val profession = profession.text?.toString()
            val addrLine1 = address1.text?.toString()
            val addrLine2 = address2.text?.toString()
            val city = city.text?.toString()
            val addrState = state.selectedItemPosition
            val zipCode = zip.text?.toString()
            val password = password.text?.toString()
            val confirm = password_confirm.text?.toString()
            val errors = (isFieldInvalid(this.name, "Your name is required.")
                    or isFieldInvalid(this.email_address, "Your email address is required.")
                    or isNumberInvalid(this.uta_id, 10, "Your UTA ID is required.")
                    or isFieldInvalid(this.profession, "Your profession is required.")
                    or isFieldInvalid(this.address1, "Your street address (line 1) is required.")
                    or isFieldInvalid(this.city, "Your city is required.")
                    or isNumberInvalid(this.zip, 5, "Your ZIP code is required.")
                    or if (isEditing) false else isFieldInvalid(this.password, "A password is required."))

            // Checking for errors
            if (errors)
                return@setOnClickListener
            if (profileImage == null) {
                Snackbar.make(view, "Please select a profile image before continuing.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!password.equals(confirm)) {
                setTextInputError(this.password_confirm, "Confirmation password does not match.")
                return@setOnClickListener
            }

            setFieldsEnabled(false)

            // Loading dialog
            val dialog = ProgressDialogHelper.create(this, if (isEditing) "Saving changes..." else "Creating Account...")
            dialog.show()

            val helper = AccountHelper(
                email!!,
                password!!,
                profileImage!!,
                name!!,
                id!!,
                profession!!,
                addrLine1!!,
                addrLine2,
                city!!,
                addrState,
                zipCode!!
            )
            if (isEditing) {
                // Update the account
                helper.updateAccount { successful, error ->
                    if (successful) {
                        dialog.dismiss()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        dialog.dismiss()
                        setFieldsEnabled(true)
                        Log.e("Registration", "Profile update failed: $error")
                        Snackbar.make(
                            fab,
                            "An error occurred while updating your profile: " + error,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                // Create the account
                helper.createAccount { successful, error ->
                    if (successful) {
                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        dialog.dismiss()
                        setFieldsEnabled(true)
                        Log.e("Registration", "Profile creation failed: $error")
                        Snackbar.make(
                            fab,
                            "An error occurred while creating your profile: " + error,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
