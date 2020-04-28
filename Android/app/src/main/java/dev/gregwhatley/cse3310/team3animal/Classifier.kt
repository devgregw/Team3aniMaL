package dev.gregwhatley.cse3310.team3animal

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class Classifier(private val activity: AppCompatActivity) {
    companion object {
        val labels = arrayOf("bird", "butterfly", "cat", "dog", "horse", "spider")
        val executorService: ExecutorService = Executors.newCachedThreadPool()

        private lateinit var buffer: ByteBuffer
        private var initialized = false

        fun loadModel(resources: Resources): Task<Unit> {
            return Tasks.call(executorService, Callable {
                // Load the model from internal storage
                val descriptor = resources.openRawResourceFd(R.raw.ml_basic_tf_acc5)
                val stream = FileInputStream(descriptor.fileDescriptor)
                val channel = stream.channel
                val offset = descriptor.startOffset
                val length = descriptor.declaredLength
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, length) as ByteBuffer
                initialized = true
            })
        }
    }

    private val interpreter: Interpreter
    val inputWidth: Int
    val inputHeight: Int
    val imageMean = 127.5f
    val imageStdDev = 127.5f
    val inputTensorCount: Int
    val outputTensorCount: Int

    init {
        if (!initialized)
            throw java.lang.IllegalStateException("Model not initialized yet!")

        // Create an interpreter
        interpreter = Interpreter(buffer)

        // Save the model's desired size of input
        inputWidth = interpreter.getInputTensor(0).shape()[1]
        inputHeight = interpreter.getInputTensor(0).shape()[2]
        inputTensorCount = interpreter.inputTensorCount
        outputTensorCount = interpreter.outputTensorCount
    }

    private fun classifyWorker(bitmap: Bitmap): SortedMap<Float, String>? {
        try {
            // Create an image processor to resize and normalize the image.
            val processor = ImageProcessor.Builder()
                .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(imageMean, imageStdDev))
                .build()

            // Create a tensor image of type UInt8
            var tensorImage = TensorImage(DataType.UINT8)

            // Load captured photo
            tensorImage.load(bitmap)

            // Process the photo
            tensorImage = processor.process(tensorImage)

            // Create model output array.
            val output = Array(1) {FloatArray(labels.size)}

            // Classify photo with model
            interpreter.run(tensorImage.buffer, output)

            // Return a sorted map of probabilities to labels
            val outputMap: HashMap<Float, String> = HashMap()
            output[0].forEachIndexed { index, fl ->
                outputMap[fl] = labels[index]
            }
            return outputMap.toSortedMap()
        } catch (e: Exception) {
            // An error occurred, return null
            Log.e("Classifier", "Classify error: ${e.message ?: "<null>"}", e)
            return null
        }
    }

    fun classifyPhoto(data: Intent, callback: (SortedMap<Float, String>?) -> Unit) {
            try {
                // Get crop results
                val results = CropImage.getActivityResult(data)

                // Decode the cropped bitmap returned by the crop activity
                val croppedBitmap = BitmapFactory.decodeFile(results.uri.path)
                    ?: throw NullPointerException("Bitmap is null")

                // Asynchronously classify the image and run the callback after
                Tasks.call(executorService, Callable { classifyWorker(croppedBitmap) }).addOnSuccessListener {
                    callback(it)
                }.addOnFailureListener { callback(null) }
            } catch (e: Exception) {
                // An error occurred, return null
                Log.e("Classifier", "Classify error: ${e.message ?: "<null>"}", e)
                callback(null)
            }
    }

    fun capturePhoto() {
        // Start the crop activity which will let the user use the camera or pick a photo already captured
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1,1)
            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .setRequestedSize(inputWidth, inputHeight)
            .start(activity)
    }
}