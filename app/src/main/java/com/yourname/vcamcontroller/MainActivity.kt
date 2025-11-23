package com.yourname.vcamcontroller

import android.Manifest
import android.os.*
import android.net.Uri
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yourname.vcamcontroller.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private val cameraDir by lazy {
    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera1")
  }

  private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()){
      it?.let { saveFile("virtual.mp4", it) }
  }
  private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
      it?.let { saveFile("1000.bmp", it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.btnPickVideo.setOnClickListener { pickVideo.launch("video/*") }
    binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }

    val list = listOf("0°","90°","180°","270°")
    binding.spinnerAngle.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)

    binding.switchEnableVcam.setOnCheckedChangeListener {_,_-> writeConfig() }

    checkPermissions()
  }

  private fun saveFile(name:String, uri:Uri){
    if(!cameraDir.exists()) cameraDir.mkdirs()
    val target = File(cameraDir, name)
    contentResolver.openInputStream(uri)?.use { input ->
      FileOutputStream(target).use { output -> input.copyTo(output) }
    }
    binding.tvCurrentFile.text = "Đã chọn: $name"
  }

  private fun writeConfig(){
    if(!cameraDir.exists()) cameraDir.mkdirs()
    val angles = listOf(0,90,180,270)
    val selected = angles[binding.spinnerAngle.selectedItemPosition]
    val json = JSONObject().apply {
      put("enabled", binding.switchEnableVcam.isChecked)
      put("rotation", selected)
    }
    FileOutputStream(File(cameraDir,"config.json")).use{
      it.write(json.toString().toByteArray())
    }
  }

  private fun checkPermissions(){
    if (Build.VERSION.SDK_INT >= 33){
      requestPermissions(arrayOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_IMAGES
      ),10)
    } else {
      requestPermissions(arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
      ),11)
    }
  }
}