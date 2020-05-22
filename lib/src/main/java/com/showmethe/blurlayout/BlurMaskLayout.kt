package com.showmethe.blurlayout

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import java.lang.ref.SoftReference

class BlurMaskLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var backgroundBitmap : Bitmap? = null
    private val picture = Picture()
    private var sampling = 4f
    private val globalCanvas = Canvas()
    private var idDrawMySelf = false
    private var blurRadius = 12f
    private val rect = Rect()
    private var skip = false

    companion object{

        private val postCallback by lazy<(Choreographer, Int, Runnable, Any?) -> Unit> {
            val method = Choreographer::class.java
                .getDeclaredMethod(
                    "postCallback",
                    Int::class.java,
                    Runnable::class.java,
                    Any::class.java
                ).apply {
                    isAccessible = true
                }
            return@lazy { c, type, run, token ->
                method.invoke(c, type, run, token)
            }
        }

        private inline fun doPostDraw(crossinline run: () -> Unit) {
            postCallback(
                Choreographer.getInstance(),
                4,
                Runnable { run() },
                null
            )
        }

    }



    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if(skip){
            skip = false
        }else{
            startDrawTask()
        }
        true
    }


    init {
        Util.init(context)
        initAttr(context,attrs)
        setWillNotDraw(false)
        background = null
    }

    private fun initAttr(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs,R.styleable.BlurMaskLayout)
        blurRadius = array.getFloat(R.styleable.BlurMaskLayout_blurRadius,12f).coerceAtMost(25f).coerceAtLeast(5f)
        sampling = array.getFloat(R.styleable.BlurMaskLayout_blurSampling,4f).coerceAtMost(10f).coerceAtLeast(4f)
        array.recycle()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if(visibility == View.VISIBLE){
            attachToWindow()
        }else{
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

    private fun detachedFromWindow(){
        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
    }

    private fun attachToWindow() {
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }



    private fun startDrawTask() {
        getGlobalVisibleRect(rect)
        val rectWidth = rect.width()
        val rectHeight = rect.height()

        //start
        val canvas = picture.beginRecording(rectWidth,rectHeight)
        canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
        idDrawMySelf = true
        rootView.draw(canvas)
        idDrawMySelf = false
        //end
        picture.endRecording()

        val scaledWidth = (width / sampling).toInt()
        val scaledHeight = (height / sampling).toInt()

         Util.getManager().getBitmap(scaledWidth,scaledHeight)?.get()?.apply {
            backgroundBitmap = this
        }
        if(backgroundBitmap == null){
            backgroundBitmap = Bitmap.createBitmap(scaledWidth,scaledHeight,Bitmap.Config.ARGB_8888)
            Util.getManager().putBitmap(scaledWidth,scaledHeight,SoftReference(backgroundBitmap!!))
        }

        globalCanvas.setBitmap(backgroundBitmap)
        globalCanvas.save()

        globalCanvas.scale(1f / sampling, 1f / sampling)
        globalCanvas.drawPicture(picture)
        globalCanvas.restore()


        backgroundBitmap = Util.getManager().process(backgroundBitmap!!,blurRadius)
        postToDraw()
    }


    private fun postToDraw() {
        post {
            doPostDraw {
                skip = true
            }
            invalidate()
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (!idDrawMySelf){
            canvas.save()
            if(backgroundBitmap!=null){
                canvas.drawBitmap(backgroundBitmap!!,null, rect.apply { set(0, 0, width, height) }, null)
            }
            canvas.restore()
        }
    }

}