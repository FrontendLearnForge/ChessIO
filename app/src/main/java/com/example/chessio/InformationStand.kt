package com.example.chessio

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.barteksc.pdfviewer.PDFView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class InformationStand : BaseActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var getPdfLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val informationContent = layoutInflater.inflate(
            R.layout.activity_information_stand,
            findViewById(R.id.content_frame)
        )
        pdfView = informationContent.findViewById(R.id.pdfView)
        setupToolbar("Справочная информация")
        updateNavHeader()
        getPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = saveFile(it)
                if (file != null) {
                    displayPdf(file)
                } else {
                    Toast.makeText(this, "Ошибка при загрузке файла", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadPdfFromAssets()
    }

    private fun loadPdfFromAssets() {
        val assetManager = assets
        val inputStream = assetManager.open("LawsOfChess2023Russian.pdf")
        val file = File(cacheDir, "temp.pdf")

        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        displayPdf(file)
    }

    private fun saveFile(uri: Uri): File? {
        val contentResolver = contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp.pdf")

        return try {
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    private fun displayPdf(file: File) {
        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .load()
    }
}
