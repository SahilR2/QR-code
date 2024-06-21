package com.example.updated

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toBitmap
import com.example.network.ApiClient
import com.example.network.ApiService
import com.example.updated.ui.theme.UpdatedTheme
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.color.Color
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


private const val TAG = "MainActivity"
private const val PICK_PHOTO_CODE=123

class MainActivity : ComponentActivity() {
    //startActivity(Intent(this,PhoneNumberVerificationActivity::class.java))
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.N)
    private var photoUri: Uri? = null
    private lateinit var decorview:View
    private lateinit var enteredText: String
    private lateinit var viewFlipper: ViewFlipper
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val decorView = window.decorView
// Hide both the navigation bar and the status bar.
// Hide both the navigation bar and the status bar.
        // Hide the status bar.
//        val apiService: ApiService = ApiClient.getClient().create(ApiService::class.java)
//
//
//        // Login
//        login(apiService, "username", "password")
//
//        // Fetch plans
//
//        // Fetch plans
//        fetchPlans(apiService)
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
           // Finish the current activity to prevent going back to it
        val isVerificationCompleted = intent.getBooleanExtra("isVerificationCompleted", false)
        val receivedValue = intent.getBooleanExtra("key",false)
        val obj=PhoneNumberVerificationActivity()
        if ( !isVerificationCompleted) {
            val intent = Intent(this, PhoneNumberVerificationActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

       // val intent = Intent(this, PhoneNumberVerificationActivity::class.java)
        //startActivity(intent)
        //finish()
        viewFlipper = findViewById<ViewFlipper>(R.id.viewFlipper)
        val editText = findViewById<EditText>(R.id.textview)
        val insertButton = findViewById<Button>(R.id.insert)
        val genButton = findViewById<Button>(R.id.Gen)


//        val imageResourceId: Int = R.drawable.kfc
//        val imageBitmap: Bitmap = BitmapFactory.decodeResource(resources, imageResourceId)/* Your image loading logic */
//        val userInputText: String = "Special, thus awesome."/* Get user input */
//            generateQRCode(imageBitmap, userInputText)
        insertButton.setOnClickListener {
            Log.d(TAG, "Open Image picker on Android")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        genButton.setOnClickListener {
            enteredText = editText.text.toString()

            // Check if both image and text are available
            if (photoUri != null && !enteredText.isEmpty()) {
                // Convert the selected image to a Bitmap
                val imagebitmap: Bitmap? = getBitmapFromUri(photoUri)



                // Call your specific function with the combinedBitmap and enteredText
                if (imagebitmap != null) {
                    viewFlipper.showNext();
                    generateQRCode(imagebitmap,enteredText);
                } else {
                    // Handle the case where combinedBitmap is null
                    Toast.makeText(this, "Failed to generate combined image", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case where either image or text is missing
                Toast.makeText(this, "Please insert an image and enter text", Toast.LENGTH_SHORT).show()
            }
        }
        val shareButton: Button = findViewById(R.id.shareButton)
        val downloadButton: Button = findViewById(R.id.downloadButton)
        shareButton.setOnClickListener {
            showShareIntent()
        }
        if(receivedValue)
        {
           shareButton.visibility= View.GONE
            downloadButton.visibility=View.GONE
        }
        downloadButton.setOnClickListener {
            downloadImage()
        }



    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == RESULT_OK) {
                photoUri = data?.data

            } else {
                Toast.makeText(this, "Image pick action canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        return try {
            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateQRCode(imageBitmap: Bitmap, userInputText: String) {

        val renderOption = RenderOption().apply {
            content = userInputText // content to encode
            size = 780 // size of the final QR code image
            borderWidth = 20 // width of the empty space around the QR code
            patternScale = 0.35f // specify a scale for patterns
            roundedPatterns = true // if true, blocks will be drawn as dots instead
            clearBorder = false// if true, the background will NOT be drawn on the border area
//            margin =

        }
//        val backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.kfc)
        val color = Color().apply {
            light = -0x1
            dark = getDominantColor(imageBitmap)
            background = -0x1
            auto = true
        }
        renderOption.color=color

// Blend the QR code with the background manually

        // A blend background (to draw a QR code onto an area of a still image)
        val background = StillBackground().apply {
            bitmap = imageBitmap//BitmapFactory.decodeResource(resources, R.drawable.kfc)// assign a bitmap as the background
            //clippingRect = Rect(0, 0, 200, 200) // crop the background before applying
            alpha = 0.90f // alpha of the background to be drawn
            //borderRadius = 200
        }
        renderOption.background=background



        val result = AwesomeQrRenderer.renderAsync(renderOption, { result ->
            runOnUiThread {
                if (result.bitmap != null) {
                    val generatedBitmap: Bitmap? = result.bitmap
                    generatedBitmap?.let {
                        // Display the generated QR code bitmap in the ImageView
                        val ima: ImageView = findViewById(R.id.img)
                        ima.setImageBitmap(it)
                    }
                }
                else {

                }
            }
        }, { exception ->
            exception.printStackTrace()
            // Oops, something went wrong.
        })
    }
    private fun showShareIntent() {
        // Step 1: Create Share intent
        val intent = Intent(Intent.ACTION_SEND).setType("image/*")
        val imageView = findViewById<ImageView>(R.id.img)
        // Step 2: Get Bitmap from your imageView
        val bitmap: Bitmap = imageView .drawable.toBitmap() // your imageView here.

        // Step 3: Compress image
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        // Step 4: Save image & get path of it
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "tempimage",
            null
        )

        // Step 5: Get URI of saved image
        val uri = Uri.parse(path)

        // Step 6: Put Uri as extra to share intent
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // Step 7: Start/Launch Share intent
        startActivity(Intent.createChooser(intent, "Share image using"))
    }
    private fun downloadImage() {
                  try{
                      val intent = Intent(Intent.ACTION_SEND).setType("image/*")
                      val imageView = findViewById<ImageView>(R.id.img)
                      // Step 2: Get Bitmap from your imageView
                      val bitmap: Bitmap = imageView .drawable.toBitmap() // your imageView here.

                      // Step 3: Compress image
                      val bytes = ByteArrayOutputStream()
                      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

                      // Step 4: Save image & get path of it
                      val path = MediaStore.Images.Media.insertImage(
                          contentResolver,
                          bitmap,
                          "tempimage",
                          null
                      )
                // Notify the user that the download was successful
                Toast.makeText(this, "Image downloaded successfully", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                // Notify the user about the error
                Toast.makeText(this, "Failed to download image", Toast.LENGTH_SHORT).show()
            }

        }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun getDominantColor(backgroundImage: Bitmap?): Int {
        val newBitmap = backgroundImage?.let { Bitmap.createScaledBitmap(it, 8, 8, true) }
        var red = 0
        var green = 0
        var blue = 0
        var c = 0
        var r: Int
        var g: Int
        var b: Int
        if (backgroundImage != null) {
            for (y in 0 until (newBitmap?.height ?:backgroundImage.height )) {
                for (x in 0 until (newBitmap?.height ?: backgroundImage.height)) {
                    val color = newBitmap?.getPixel(x, y)
                    r = color!! shr 16 and 0xFF
                    g = color shr 8 and 0xFF
                    b = color and 0xFF
                    if (r > 200 || g > 200 || b > 200) continue
                    red += r
                    green += g
                    blue += b
                    c++
                }
            }
        }
        newBitmap?.recycle()
        return if (c == 0) {
            // got a bitmap with no pixels in it
            // avoid the "divide by zero" error
            -0x1000000
        } else {
            red = Math.max(0, Math.min(0xFF, red / c))
            green = Math.max(0, Math.min(0xFF, green / c))
            blue = Math.max(0, Math.min(0xFF, blue / c))
            0xFF shl 24 or (red shl 16) or (green shl 8) or blue
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UpdatedTheme {
        Greeting("Android")
    }
}