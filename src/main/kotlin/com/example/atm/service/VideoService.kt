package com.example.atm.service

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class VideoService {

    // TODO: Move to application.properties
    private val videoBasePath = "/home/ali-zaidi/atm_videos"

    fun getVideoFootage(atmId: String, startTime: LocalDateTime, endTime: LocalDateTime): Resource? {

        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

        val date = startTime.format(dateFormatter)
        val startTimestamp = startTime.format(timestampFormatter)
        val endTimestamp = endTime.format(timestampFormatter)

        val videoFileName = "${atmId}_${startTimestamp}_${endTimestamp}.mp4"
        val videoFilePath = "$videoBasePath/$atmId/$date/$videoFileName"

        val file = File(videoFilePath)
        return if (file.exists()) {
            FileSystemResource(file)
        } else {
            null
        }
    }
}
