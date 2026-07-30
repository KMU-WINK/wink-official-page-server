package com.github.kmu_wink.wink_official_page.domain.program.upload.controller

import com.github.kmu_wink.wink_official_page.domain.program.upload.dto.response.UploadImageResponse
import com.github.kmu_wink.wink_official_page.domain.program.upload.service.UploadService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsMember
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@IsMember
@Tag(name = "[Program] [Upload] Index")
@RestController
@RequestMapping("/program/upload")
class UploadController(
    private val uploadService: UploadService,
) {
    @PostMapping("/image")
    @Operation(summary = "이미지 업로드")
    fun uploadImage(): ApiResponse<UploadImageResponse> = ApiResponse.ok(uploadService.uploadImage())
}
