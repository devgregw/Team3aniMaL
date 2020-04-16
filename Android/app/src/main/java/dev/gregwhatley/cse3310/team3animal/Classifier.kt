package dev.gregwhatley.cse3310.team3animal

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Tasks
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class Classifier(private val activity: AppCompatActivity) {
    private val interpreter: Interpreter
    private val executorService = Executors.newCachedThreadPool()
    private val labels = arrayOf("bird", "butterfly", "cat", "dog", "horse", "spider")
    private val inputWidth: Int
    private val inputHeight: Int
    private val imageMean = 127.5f
    private val imageStdDev = 127.5f
    private var photoPath: String? = null

    companion object {
        const val CAPTURE_REQUEST_CODE = 512
    }

    init {
        // Load the model from internal storage
        val descriptor = activity.resources.openRawResourceFd(R.raw.ml_basic_plaidml_binary_98)
        val stream = FileInputStream(descriptor.fileDescriptor)
        val channel = stream.channel
        val offset = descriptor.startOffset
        val length = descriptor.declaredLength
        val buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, length) as ByteBuffer

        // Create an interpreter
        interpreter = Interpreter(buffer)

        // Save the model's desired size of input
        inputWidth = interpreter.getInputTensor(0).shape()[1]
        inputHeight = interpreter.getInputTensor(0).shape()[2]
    }

    private fun classifyWorker(): SortedMap<Float, String> {
        // Create an image processor to resize and normalize the image.
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(imageMean, imageStdDev))
            .build()

        // Create a tensor image of type UInt8
        var tensorImage = TensorImage(DataType.UINT8)

        // Load captured photo
        tensorImage.load(BitmapFactory.decodeFile(photoPath!!))

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
    }

    fun classifyCapturedPhoto(callback: (SortedMap<Float, String>?) -> Unit) {
        if ((photoPath::isNullOrBlank)()) capturePhoto() // Capture a photo if one hasn't been captured already
        else {
            // Asynchronously classify the image and run the callback after
            Tasks.call(executorService, Callable { classifyWorker() }).addOnCompleteListener {
                callback(it.result)
            }
        }
    }

    fun capturePhoto() {
        // Create an intent to launch the camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Create a temp file to store the photo
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${stamp}_", ".jpg", dir).apply {
            photoPath = absolutePath
        }
        val uri = FileProvider.getUriForFile(activity, "dev.gregwhatley.cse3310.team3animal", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        // Launch camera
        activity.startActivityForResult(intent, CAPTURE_REQUEST_CODE)
    }
}