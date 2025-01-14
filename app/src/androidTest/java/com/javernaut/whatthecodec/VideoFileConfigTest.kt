package com.javernaut.whatthecodec

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class VideoFileConfigTest {

    private val testFileName = "test_video_trimmed.mp4"

    @Test
    fun testVideoConfigAccessors() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Writing a video file from assets to internal memory
        val inputStream = context.assets.open(testFileName)

        val testFile = File(targetContext.filesDir, testFileName)
        testFile.createNewFile()

        val outputStream = testFile.outputStream()
        inputStream.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()
        inputStream.close()

        // Actual test
        val descriptor = targetContext.contentResolver.openFileDescriptor(Uri.parse("file://" + testFile.absolutePath), "r")

        val videoFileConfig = VideoFileConfig.create(descriptor!!)
        assertThat(videoFileConfig).isNotNull()

        if (videoFileConfig != null) {
            assertThat(videoFileConfig.height).isEqualTo(1080)
            assertThat(videoFileConfig.width).isEqualTo(1920)

            assertThat(videoFileConfig.fileFormat).isEqualTo("QuickTime / MOV")
            assertThat(videoFileConfig.codecName).isEqualTo("H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10")

            videoFileConfig.release()
        }

        // Clean up
        testFile.delete()
    }

}
