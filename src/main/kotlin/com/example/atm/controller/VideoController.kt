package com.example.atm.controller

import com.example.atm.service.VideoService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@RestController
class VideoController(val videoService: VideoService) {

    @GetMapping("/atm/{atmId}/video")
    @PreAuthorize("hasRole('ADMIN')")
    fun downloadVideo(
        @PathVariable("atmId") atmId: String,
        @RequestParam startTime: String,
        @RequestParam endTime: String
    ): ResponseEntity<Resource> {
        return try {
            val start = LocalDateTime.parse(startTime)
            val end = LocalDateTime.parse(endTime)

            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().body(null)
            }

            val videoResource = videoService.getVideoFootage(atmId, start, end)
            if (videoResource != null) {
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${videoResource.filename}\"")
                    .body(videoResource)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: DateTimeParseException) {
            ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(null)
        }
    }
}
