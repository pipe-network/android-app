package com.pipe_network.app.application.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.mimemagic.MimeData
import id.zelory.compressor.Compressor
import java.io.File
import javax.inject.Inject

interface PictureStoringService {
    suspend fun save(name: String, data: ByteArray): File
}

class PictureStoringServiceImpl @Inject constructor(
    @ApplicationContext val context: Context
) : PictureStoringService {
    override suspend fun save(name: String, data: ByteArray): File {
        val mimeData = MimeData.fromByteArray(data)
        val pictureFile = File(
            context.filesDir,
            "$name.${mimeData.suffix}",
        )
        pictureFile.writeBytes(data)
        return Compressor.compress(context, pictureFile)
    }


}

