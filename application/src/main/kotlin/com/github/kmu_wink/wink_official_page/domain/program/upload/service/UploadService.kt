package com.github.kmu_wink.wink_official_page.domain.program.upload.service

import com.github.kmu_wink.wink_official_page.domain.program.upload.dto.response.UploadImageResponse
import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UploadService(
    private val objectStoragePort: ObjectStoragePort,
) {
    fun uploadImage(): UploadImageResponse {
        val url = objectStoragePort.generatePresignedUrl("program/${UUID.randomUUID()}")
        return UploadImageResponse(url)
    }
}
