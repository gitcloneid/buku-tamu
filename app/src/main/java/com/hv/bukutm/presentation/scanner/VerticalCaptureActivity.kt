package com.hv.bukutm.presentation.scanner

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import com.hv.bukutm.R
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.Size

class VerticalCaptureActivity : CaptureActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical_capture)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        barcodeView.barcodeView.framingRectSize = Size(
            (resources.displayMetrics.widthPixels * 0.75).toInt(),
            (resources.displayMetrics.widthPixels * 0.75).toInt() // Square size
        )
        barcodeView.decodeContinuous { result ->
            val intent = Intent().apply {
                putExtra(com.google.zxing.client.android.Intents.Scan.RESULT, result.text)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume() // Ensure camera preview starts
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause() // Pause camera preview to avoid leaks
        barcodeView.setTorchOff()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                barcodeView.setTorchOff()
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                barcodeView.setTorchOn()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}