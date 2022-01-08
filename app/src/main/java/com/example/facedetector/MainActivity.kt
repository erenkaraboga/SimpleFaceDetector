package com.example.facedetector

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

const val REQUEST_CODE_PICK_IMAGE = 101
private var selectedImageUri: Uri? = null
var image: InputImage? =null
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView.setOnClickListener {
            openImageChooser()
        }
        button.setOnClickListener {
            setImageToMlKit()
        }
    }
    private fun drawRect(bitmap: Bitmap?, list: List<Face>): Bitmap? {
        if (bitmap != null) {
            val resultBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                bitmap.config
            )
            val pen = Paint()
            pen.color = Color.GREEN
            pen.style = Paint.Style.STROKE
            pen.strokeWidth = 8.0f
            val canvas = Canvas(resultBitmap)
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
            for (face in list){
                canvas.drawRect(face.boundingBox,pen)
            }
              return  resultBitmap
        }
        return null
    }
    private fun setImageToMlKit(){
        if(selectedImageUri!=null){
            try {
                image = InputImage.fromFilePath(applicationContext, selectedImageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val detector = FaceDetection.getClient()
            val result = detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.count()>0){
                        CoroutineScope(Dispatchers.IO).launch {
                            var bitmap = Glide.with(applicationContext).asBitmap().load(selectedImageUri)
                                .submit().get()
                            imageView.setImageBitmap(drawRect(bitmap,faces))
                        }
                        var faceCount=faces.count()
                        Toast.makeText(applicationContext,"Found $faceCount Faces",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(applicationContext,"No Faces Found",Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e->
                    e.printStackTrace()
                }
        }
        else{
            Toast.makeText(applicationContext,"Select an Image",Toast.LENGTH_SHORT).show()
        }
    }
    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    imageView.setImageURI(selectedImageUri)
                }
            }
        }
    }
}