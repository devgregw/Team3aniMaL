package dev.gregwhatley.cse3310.team3animal

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.collection.ImmutableSortedMap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class Classifier(val activity: AppCompatActivity) {
    private val interpreter: Interpreter
    private val executorService = Executors.newCachedThreadPool()
    private val labels = arrayOf("bird", "butterfly", "cat", "dog", "horse", "spider")
    private val inputWidth: Int
    private val inputHeight: Int
    private val imageMean = 127.5f
    private val imageStd = 127.5f
    private var photoPath: String? = null

    companion object {
        const val CAPTURE_REQUEST_CODE = 512
    }

    init {
        val descriptor = activity.resources.openRawResourceFd(R.raw.ml_basic_plaidml_binary_98)
        val stream = FileInputStream(descriptor.fileDescriptor)
        val channel = stream.channel
        val offset = descriptor.startOffset
        val length = descriptor.declaredLength
        interpreter = Interpreter(channel.map(FileChannel.MapMode.READ_ONLY, offset, length) as ByteBuffer)
        inputWidth = interpreter.getInputTensor(0).shape()[1]
        inputHeight = interpreter.getInputTensor(0).shape()[2]
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4*128*128*3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputWidth)
            for (j in 0 until inputHeight)
                buffer.apply {
                    val pixelVal = pixels[pixel++]
                    putFloat(((pixelVal shr 16 and 0xFF) - imageMean) / imageStd)
                    putFloat(((pixelVal shr 8 and 0xFF) - imageMean) / imageStd)
                    putFloat(((pixelVal and 0xFF) - imageMean) / imageStd)
                }
        bitmap.recycle()
        return buffer
    }

    private fun classifyWorker(): SortedMap<Float, String> {
        val tImg = ImageProcessor.Builder().add(ResizeOp(128, 128, ResizeOp.ResizeMethod.BILINEAR)).build().process(TensorImage(DataType.UINT8).apply { load(BitmapFactory.decodeFile(photoPath!!)) })
        val output = Array(1) {FloatArray(6)}
        interpreter.run(convertBitmapToByteBuffer(tImg.bitmap), output)
        val outputMap: HashMap<Float, String> = HashMap()
        output[0].forEachIndexed { index, fl ->
            outputMap[fl] = labels[index]
        }
        return outputMap.toSortedMap()
    }

    fun classifyCapturedPhoto(callback: (SortedMap<Float, String>?) -> Unit) {
        if ((photoPath::isNullOrBlank)()) capturePhoto()
        else {
            Tasks.call(executorService, Callable { classifyWorker() }).addOnCompleteListener {
                callback(it.result)
            }
        }
    }

    fun capturePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${stamp}_", ".jpg", dir).apply {
            photoPath = absolutePath
        }
        val uri = FileProvider.getUriForFile(activity, "dev.gregwhatley.cse3310.team3animal", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        activity.startActivityForResult(intent, CAPTURE_REQUEST_CODE)
    }
}