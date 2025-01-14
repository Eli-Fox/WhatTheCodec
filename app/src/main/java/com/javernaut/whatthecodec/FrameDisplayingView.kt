package com.javernaut.whatthecodec

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.sqrt

class FrameDisplayingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var videoFileConfig: VideoFileConfig? = null

    private var originFrameWidth = 0
    private var originFrameHeight = 0

    private var scaledViewHeight = 0

    private var childFramesPerRow = 1

    var childFramesCount = 1
        set(value) {
            field = value
            childFramesPerRow = sqrt(value.toDouble()).toInt()
        }

    private val frameSpacingBase = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics)
    private var frameSpacingDelta = 0f

    private var childFrameWidth = 0
    private var childFrameHeight = 0

    private var frames: Array<Bitmap>? = null
        set(value) {
            field?.forEach { it.recycle() }
            field = value
            requestLayout()
        }

    fun setVideoConfig(config: VideoFileConfig) {
        videoFileConfig = config
    }

    private fun calculateValues() {
        originFrameWidth = videoFileConfig!!.width
        originFrameHeight = videoFileConfig!!.height

        childFrameWidth = (measuredWidth - (childFramesPerRow - 1) * frameSpacingBase).toInt() / childFramesPerRow

        frameSpacingDelta = if (childFramesPerRow == 1) {
            0f
        } else {
            (measuredWidth - childFramesPerRow * childFrameWidth).toFloat() / (childFramesPerRow - 1)
        }

        childFrameHeight = originFrameHeight * childFrameWidth / originFrameWidth

        scaledViewHeight = (childFrameHeight * childFramesPerRow + (childFramesPerRow - 1) * getFinalFrameSpacing()).toInt()
    }

    fun loadPreviews() {
        videoFileConfig?.let {
            calculateValues()

            LoadingTask().execute()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(targetWidth, scaledViewHeight)
    }

    private fun getFinalFrameSpacing() = frameSpacingBase + frameSpacingDelta

    override fun onDraw(canvas: Canvas) {
        frames?.let {
            val bitmapsPerRow = sqrt(it.size.toDouble()).toInt()
            it.forEachIndexed { index, bitmap ->
                val childFrameXPos = index.rem(bitmapsPerRow)
                val left = childFrameXPos * childFrameWidth + childFrameXPos * getFinalFrameSpacing()

                val childFrameYPos = index / bitmapsPerRow
                val top = childFrameYPos * childFrameHeight + childFrameYPos * getFinalFrameSpacing()

                canvas.drawBitmap(bitmap, left, top, null)
            }
        }
    }

    // Well, I'm not proud of using AsyncTask, but this app doesn't need more sophisticated things at all
    private inner class LoadingTask : AsyncTask<Unit, Unit, Array<Bitmap>>() {
        private lateinit var progressDialog: ProgressDialog

        override fun onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, "Please wait...")
        }

        override fun doInBackground(vararg param: Unit?): Array<Bitmap> {
            val bitmaps = Array<Bitmap>(childFramesCount) {
                Bitmap.createBitmap(childFrameWidth, childFrameHeight, Bitmap.Config.ARGB_8888)
            }
            videoFileConfig?.fillWithPreview(bitmaps)
            return bitmaps
        }

        override fun onPostExecute(result: Array<Bitmap>?) {
            progressDialog.dismiss()
            frames = result
        }
    }
}
