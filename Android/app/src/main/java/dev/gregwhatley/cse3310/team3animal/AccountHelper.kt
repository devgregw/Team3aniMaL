package dev.gregwhatley.cse3310.team3animal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.lang.Exception

typealias AccountCreationHandler = (Boolean, String?) -> Unit

class AccountHelper(private val emailAddress: String,
                    private val password: String,
                    private val profileImageBitmap: Bitmap,
                    private val name: String,
                    private val utaId: String,
                    private val profession: String,
                    private val addressLine1: String,
                    private val addressLine2: String?,
                    private val city: String,
                    private val state: Int,
                    private val zip: String) {

    private lateinit var handler: AccountCreationHandler

    // For new accounts, create the Firebase Auth user (step 1)
    private fun createFirebaseUser() {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener {
                    if (it.isCanceled || it.exception != null || !it.isSuccessful)
                        handler(false, it.exception?.message ?: "Unknown error.")
                    else waitForAuth()
                }
        } catch (e: Exception) {
            handler(false, e.message)
        }
    }

    // For new accounts, wait for the user to be signed in (step 2)
    private fun waitForAuth() {
        FirebaseAuth.getInstance().addAuthStateListener { state ->
            if (state.currentUser == null)
                return@addAuthStateListener
            saveProfileImage(state.currentUser!!.uid)
        }
    }

    // Upload profile image to Firebase Storage
    private fun saveProfileImage(uid: String) {
        try {
            val stream = ByteArrayOutputStream()
            profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val ref = FirebaseStorage.getInstance().reference.child("user").child(uid)
                .child("profileImage.jpg")
            ref.putBytes(stream.toByteArray()).addOnCompleteListener {
                if (it.isCanceled || it.exception != null || !it.isSuccessful)
                    handler(false, it.exception?.message ?: "Unknown error.")
                else getProfileImageUrl(uid, ref)
            }
        } catch (e: Exception) {
            handler(false, e.message)
        }
    }

    // Get the image URL for the profile image after being uploaded
    private fun getProfileImageUrl(uid: String, ref: StorageReference) {
        try {
            ref.downloadUrl.addOnCompleteListener {
                if (it.isCanceled || it.exception != null || !it.isSuccessful)
                    handler(false, it.exception?.message ?: "Unknown error.")
                else setFirebaseUserProperties(uid, it.result)
            }
        } catch (e: Exception) {
            handler(false, e.message)
        }

    }

    // Set the display name and profile image URI properties on the Firebase Auth user
    private fun setFirebaseUserProperties(uid: String, profileImageUrl: Uri) {
        try {
            FirebaseAuth.getInstance().currentUser!!.updateProfile(UserProfileChangeRequest.Builder()
                .setPhotoUri(profileImageUrl)
                .setDisplayName(name)
                .build())
                .addOnCompleteListener {
                    if (it.isCanceled || it.exception != null || !it.isSuccessful)
                        handler(false, it.exception?.message ?: "Unknown error.")
                    else createProfile(uid)
                }
        } catch (e: Exception) {
            handler(false, e.message)
        }
    }

    // Save additional info to the Firebase Realtime Database
    private fun createProfile(uid: String) {
        val userInfo = HashMap<String, Any>().apply {
            put("id", utaId)
            put("profession", profession)
            put("address", HashMap<String, Any>().apply {
                put("line1", addressLine1)
                if (!addressLine2.isNullOrBlank())
                    put("line2", addressLine2)
                put("city", city)
                put("state", state)
                put("zip", zip)
            })
        }.toMap()
        try {
            FirebaseDatabase.getInstance().reference.child("users").child(uid).setValue(userInfo) { error, _ ->
                if (error != null)
                    handler(false, error.message)
                else handler(true, null)
            }
        } catch (e: Exception) {
            handler(false, e.message)
        }
    }

    // Starts the account creation process
    fun createAccount(completion: AccountCreationHandler) {
        handler = completion
        createFirebaseUser()
    }

    // For updated accounts, changes email address (step 1)
    private fun updateEmailAddress() {
        FirebaseAuth.getInstance().addAuthStateListener { state ->
            if (state.currentUser == null)
                return@addAuthStateListener
            try {
                state.currentUser!!.updateEmail(emailAddress).addOnCompleteListener {
                    if (it.isCanceled || it.exception != null || !it.isSuccessful)
                        handler(false, it.exception?.message ?: "Unknown error.")
                    else updatePassword()
                }
        } catch (e: Exception) {
                handler(false, e.message)
            }
        }
    }

    // If the user did not opt to change their password, save their new profile image and continue from there
    // If they want to change their password, save it then continue like normal
    private fun updatePassword() {
        if (password.isBlank()) {
            saveProfileImage(FirebaseAuth.getInstance().currentUser!!.uid)
        } else {
            try {
                FirebaseAuth.getInstance().currentUser!!.updatePassword(password).addOnCompleteListener {
                    if (it.isCanceled || it.exception != null || !it.isSuccessful)
                        handler(false, it.exception?.message ?: "Unknown error.")
                    else saveProfileImage(FirebaseAuth.getInstance().currentUser!!.uid)
                }
            } catch (e: Exception) {
                handler(false, e.message)
            }
        }
    }

    // Starts the account update process
    fun updateAccount(completion: AccountCreationHandler) {
        handler = completion
        updateEmailAddress()
    }
}