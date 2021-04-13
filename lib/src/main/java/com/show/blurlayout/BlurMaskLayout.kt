package com.show.blurlayout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import com.showmethe.blurlayout.R

/**
 *  com.show.healthylife.widget
 *  2020/10/16
 *  23:38
 *  ShowMeThe
 */

class BlurMaskLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var backgroundBitmap: Bitmap? = null
    private val picture = Picture()
    private var sampling = 4f
    private val globalCanvas = Canvas()
    private var idDrawMySelf = false
    private var blurRadius = 12f
    private val rect = Rect()
    private var skip = false


    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        startDraw()
        true
    }


    init {
        initAttr(context, attrs)
        setWillNotDraw(false)
        background = null
    }


    private fun initAttr(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BlurMaskLayout)
        blurRadius = array.getFloat(R.styleable.BlurMaskLayout_blurRadius, 12f)
                .coerceAtMost(25f).coerceAtLeast(5f)
        sampling = array.getFloat(R.styleable.BlurMaskLayout_blurSampling, 4f)
                .coerceAtMost(10f).coerceAtLeast(4f)
        array.recycle()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            attachToWindow()
        } else {
            detachedFromWindow()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachToWindow()
    }

    override fun onDetachedFromWindow() {
        detachedFromWindow()
        super.onDetachedFromWindow()
    }

    private fun detachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDrawListener)

    }

    private fun attachToWindow() {
        viewTreeObserver.addOnPreDrawListener(preDrawListener)

    }


    private fun startDraw() {
        getGlobalVisibleRect(rect)
        val rectWidth = rect.width()
        val rectHeight = rect.height()

        if (rectHeight <= 0 || rectWidth <= 0) {
            return
        }

        if(width <= 0 || height <= 0){
            return
        }

        
        //start
        val canvas = picture.beginRecording(rectWidth, rectHeight)
        canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
        idDrawMySelf = true
        rootView.draw(canvas)
        idDrawMySelf = false
        //end
        picture.endRecording()

        val scaledWidth = (width / sampling).toInt()
        val scaledHeight = (height / sampling).toInt()

        if(scaledWidth <= 0 || scaledHeight <= 0){
            return
        }

        val newBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

        globalCanvas.setBitmap(newBitmap)
        globalCanvas.save()

        globalCanvas.scale(1f / sampling, 1f / sampling)
        globalCanvas.drawPicture(picture)
        globalCanvas.restore()

        Util.getManager().process(newBitmap!!, blurRadius)

        /**
         * when get the same result stop postInvalidate
         */
        if(backgroundBitmap != null && compare2Bitmap(backgroundBitmap!!,newBitmap)){
            return
        }

        backgroundBitmap = newBitmap
        postInvalidate()
    }


    private fun compare2Bitmap(bmp1: Bitmap, bmp2: Bitmap):Boolean{
        val width: Int = bmp1.width
        val height: Int = bmp1.height
        if (width != bmp2.width) return false
        if (height != bmp2.height) return false


        val steps = (50f * ( 4 / sampling)).toInt()

        for (w in 0 until width step steps) {
            for (h in 0 until height step steps){
                if (bmp1.getPixel(w, h) != bmp2.getPixel(w, h)){
                    return false
                }
            }
        }
        return true
    }



    override fun onDraw(canvas: Canvas) {
        if (!idDrawMySelf) {
            canvas.save()
            if (backgroundBitmap != null) {
                canvas.drawBitmap(backgroundBitmap!!, null, rect.apply { set(0, 0, width, height) }, null)
            }
            canvas.restore()
        }
    }



}