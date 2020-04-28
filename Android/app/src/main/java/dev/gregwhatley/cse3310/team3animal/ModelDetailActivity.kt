package dev.gregwhatley.cse3310.team3animal

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.tasks.Tasks
import kotlinx.android.synthetic.main.activity_model_detail.*
import java.io.ByteArrayOutputStream

class ModelDetailActivity : AppCompatActivity() {
    companion object {
        // ID for the jump to top button
        const val MENU_TOP_ID = 1
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Add the jump to top button
        menu?.add(Menu.NONE, MENU_TOP_ID, 0, "Top")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            ?.setIcon(R.drawable.ic_action_top)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            // Back button
            finish()
        if (item.itemId == MENU_TOP_ID) {
            // Jump to top
            vertical_scroll_view.smoothScrollTo(0, 0)
            horizontal_scroll_view.smoothScrollTo(0, 0)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_detail)

        // Show back button
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_action_back)
            setDisplayHomeAsUpEnabled(true)
        }

        title = "ML Model Information"
        model_detail.text = "Loading..."

        // Load model info file asynchronously
        Tasks.call {
            val input = resources.openRawResource(R.raw.model_data)
            val buffer = ByteArray(input.available())
            input.read(buffer)
            val output = ByteArrayOutputStream()
            output.write(buffer)
            output.close()
            input.close()
            val string = output.toString("UTF-8")

            // Must be run on main thread
            Handler(mainLooper).post {
                model_detail.typeface = Typeface.MONOSPACE
                model_detail.setTextIsSelectable(true)
                model_detail.textSize = 10f
                model_detail.text = string
            }
        }
    }
}
