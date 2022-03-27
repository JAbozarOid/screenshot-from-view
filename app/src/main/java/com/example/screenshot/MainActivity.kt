package com.example.screenshot

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
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
                shareScreenShootResult()
            }
        }
    }


    private fun shareScreenShootResult() {
        val dateFormatter by lazy {
            SimpleDateFormat(
                "yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault()
            )
        }
        val filename = "${getString(R.string.my_ScreenShoot)}${dateFormatter.format(Date())}.png"
        val screenShootFolderPath = File.separator + this.getAppName()

        val uri = customView.makeScreenShot()
            .saveScreenShot(this, filename, screenShootFolderPath, permissionListener)
            ?: return

        dispatchShareImageIntent(uri)
    }

    private fun dispatchShareImageIntent(screenShotUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, screenShotUri)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun Context.getAppName(): String {
        var appName: String = ""
        val applicationInfo = applicationInfo
        val stringId = applicationInfo.labelRes
        appName = if (stringId == 0) {
            applicationInfo.nonLocalizedLabel.toString()
        } else {
            getString(stringId)
        }
        return appName
    }

    private fun View.makeScreenShot(): Bitmap {
        setBackgroundColor(Color.WHITE)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    private fun Bitmap.saveScreenShot(
        requireContext: Context,
        filename: String,
        ScreenShootFolderPath: String,
        permissionListener: () -> Boolean,
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            saveImageInQ(this, filename, ScreenShootFolderPath, requireContext.contentResolver)
        else
            legacySave(this, filename, ScreenShootFolderPath, permissionListener)
    }

    private fun saveImageInQ(
        bitmap: Bitmap,
        filename: String,
        parentFileName: String,
        contentResolver: ContentResolver
    ): Uri? {
        val fos: OutputStream?
        val uri: Uri?
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + parentFileName)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { contentResolver.openOutputStream(it) }.also { fos = it }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        fos?.flush()
        fos?.close()

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        uri?.let {
            contentResolver.update(it, contentValues, null, null)
        }
        return uri
    }

    private fun legacySave(
        bitmap: Bitmap,
        filename: String,
        parentFileName: String,
        permissionListener: () -> Boolean,
    ): Uri? {
        val fos: OutputStream?
        if (!permissionListener()) {
            return null
        }

        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
                    parentFileName + File.separator + filename
        val imageFile = File(path)
        if (imageFile.parentFile?.exists() == false) {
            imageFile.parentFile?.mkdir()
        }
        imageFile.createNewFile()
        fos = FileOutputStream(imageFile)
        //val uri: Uri = Uri.fromFile(imageFile)

        val photoURI = FileProvider.getUriForFile(
            Objects.requireNonNull(applicationContext),
            BuildConfig.APPLICATION_ID + ".provider", imageFile
        )

        fos.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        fos.flush()
        fos.close()

        return photoURI
    }
    private val permissionListener: () -> Boolean = {
        if (ContextCompat.checkSelfPermission(
               this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            requestStoragePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            false
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var saveImageFlag = true
            permissions.entries.forEach {
                saveImageFlag = it.value
            }
            if (saveImageFlag) {
                shareScreenShootResult()
            } else {
                toast("cant_share_ScreenShoot")
            }
        }


    private fun Context.toast(message: String) { //Just to display a toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}