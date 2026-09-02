package com.example.data.downloader

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

object MediaMuxerHelper {
    
    fun muxVideoAndAudio(videoFile: File, audioFile: File, outputFile: File): Boolean {
        try {
            if (outputFile.exists()) outputFile.delete()
            
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFile.absolutePath)
            var videoTrackIndex = -1
            var videoTrackInfo: MediaFormat? = null
            
            for (i in 0 until videoExtractor.trackCount) {
                val format = videoExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("video/") == true) {
                    videoExtractor.selectTrack(i)
                    videoTrackInfo = format
                    break
                }
            }
            
            val audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(audioFile.absolutePath)
            var audioTrackIndex = -1
            var audioTrackInfo: MediaFormat? = null
            
            for (i in 0 until audioExtractor.trackCount) {
                val format = audioExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioExtractor.selectTrack(i)
                    audioTrackInfo = format
                    break
                }
            }
            
            if (videoTrackInfo == null && audioTrackInfo == null) return false
            
            val muxerVideoTrackIndex = if (videoTrackInfo != null) muxer.addTrack(videoTrackInfo) else -1
            val muxerAudioTrackIndex = if (audioTrackInfo != null) muxer.addTrack(audioTrackInfo) else -1
            
            muxer.start()
            
            val bufferInfo = MediaCodec.BufferInfo()
            val maxBufferSize = maxOf(
                videoTrackInfo?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) ?: 1048576,
                audioTrackInfo?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) ?: 1048576
            )
            val buffer = ByteBuffer.allocate(maxBufferSize)
            
            if (videoTrackInfo != null && muxerVideoTrackIndex >= 0) {
                while (true) {
                    val sampleSize = videoExtractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    
                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                    
                    muxer.writeSampleData(muxerVideoTrackIndex, buffer, bufferInfo)
                    videoExtractor.advance()
                }
            }
            
            if (audioTrackInfo != null && muxerAudioTrackIndex >= 0) {
                while (true) {
                    val sampleSize = audioExtractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    
                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.flags = audioExtractor.sampleFlags
                    bufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    
                    muxer.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
                    audioExtractor.advance()
                }
            }
            
            muxer.stop()
            muxer.release()
            videoExtractor.release()
            audioExtractor.release()
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
