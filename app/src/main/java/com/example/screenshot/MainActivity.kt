package com.example.screenshot

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var customView: CustomView
    private lateinit var shareBtn: AppCompatButton

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customView = findViewById(R.id.custom_view)
        shareBtn = findViewById(R.id.btn_share)
        shareBtn.setOnClickListener(this)

    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_share -> {
                val bmpFromView =
                    getScreenShot(customView) //Taking screenshot of the view from activity_main.xml
                val finalPath = saveImageToInternalStorage(bmpFromView) //Saving it to the sd card
                toast(finalPath.toString()) //Debug thing. Just to check the view width (so i can know if its a valid view or 0(just null))
            }
        }
    }

    private fun getScreenShot(view: View): Bitmap { //A few things are deprecated but i kept them anyway
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)
        // The bellow line return a directory in internal storage

        val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/Screenshots"

        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "share.JPEG")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }
        runOnUiThread {
            shareImage(file)
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }

    private fun Context.toast(message: String) { //Just to display a toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun shareImage(file: File) {

        val photoURI = FileProvider.getUriForFile(
            Objects.requireNonNull(applicationContext),
            BuildConfig.APPLICATION_ID + ".provider", file
        )

        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            toast("no image found")
        }
    }
}