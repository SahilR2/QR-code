package com.example.updated.ui.theme
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.example.updated.R
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.RenderResult
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.BlendBackground
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.color.Color
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.zxing.qrcode.encoder.QRCode


class qrcode : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setTheme(R.style.Theme_AppCompat)
        setContentView(R.layout.activity_qrcode)
        // Kotlin

// A still background (a still image as the background)
        val drawableResourceId = R.drawable.logo_image
        val logoBitmap: Bitmap? = BitmapFactory.decodeResource(resources, drawableResourceId)
        val backgroundImage = BitmapFactory.decodeResource(resources,drawableResourceId)
        val drawableResourceId1 = R.drawable.logo_kfc
        //val logoBitmap1: Bitmap? = BitmapFactory.decodeResource(resources, drawableResourceId)
        val backgroundImage1 = BitmapFactory.decodeResource(resources,drawableResourceId1)
// A blend background (to draw a QR code onto an area of a still image)
      //  val background = BlendBackground()
       //background.bitmap = logoBitmap
      //  background.clippingRect = Rect(0, 0, 200, 200)
        //background.alpha = 0.7f
        //background.borderRadius = 10 // radius for blending corners
        // Kotlin
     //   val logoBitmap: Bitmap? =logoBitmap
        // Suppose you have an image named "logo_image.png" in your drawable folder
         // Replace with your actual image resource ID
      //  val resourceId = resources.getIdentifier(logo_image, "drawable", packageName)

        //val logo = Logo()
        //logo.bitmap = logoBitmap
        //logo.borderRadius = 30 // radius for logo's corners
        //logo.borderWidth = 30 // width of the border to be added around the logo
        //logo.scale = 0.3f // scale for the logo in the QR code
        //logo.clippingRect = RectF(0F, 0F, 200F, 200F) // crop the logo image before applying it to the QR code

// A gif background (animated)

        val renderOption = RenderOption().apply {
            content = "https://www.google.co.uk/" // content to encode
            size = 800 // size of the final QR code image
            borderWidth = 20 // width of the empty space around the QR code
            patternScale = 0.35f // specify a scale for patterns
            roundedPatterns = true // if true, blocks will be drawn as dots instead
            clearBorder = false // if true, the background will NOT be drawn on the border area
        }

        val color = Color().apply {
            light = -0x1 // for blank spaces
            dark = getDominantColor(backgroundImage1) // for non-blank spaces
           background =-0x1 // for the background
            auto = true // set to true to automatically pick out colors from the background image
        }
     //  renderOption.background = background
        renderOption.color = color
        //renderOption.logo = logo
        val background = StillBackground().apply {
            bitmap = BitmapFactory.decodeResource(resources,drawableResourceId1)// assign a bitmap as the background
            //clippingRect = Rect(0, 0, 200, 200) // crop the background before applying
            alpha = 0.90f // alpha of the background to be drawn
            //borderRadius = 200
        }
        renderOption.background=background
        val result = AwesomeQrRenderer.renderAsync(renderOption, { result ->
            runOnUiThread {
                if (result.bitmap != null) {
                    val generatedBitmap: Bitmap? = result.bitmap as? Bitmap
                    generatedBitmap?.let {
                        // Display the generated QR code bitmap in the ImageView
                        val ima: ImageView = findViewById(R.id.img)
                        ima.setImageBitmap(it)
                    }
                }  else {
                    // Oops, something went wrong.
                }
            }
        }, { exception ->
            exception.printStackTrace()
            // Oops, something went wrong.
        })

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


