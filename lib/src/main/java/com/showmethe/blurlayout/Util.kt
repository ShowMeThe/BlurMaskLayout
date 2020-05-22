package com.showmethe.blurlayout

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.FloatRange
import java.lang.ref.SoftReference

class Util {


    companion object {

        private val instant by  lazy { Util() }

        private lateinit var rs: RenderScript

        fun init(context: Context) {
            rs = RenderScript.create(context.applicationContext)
        }

        fun getManager() : Util{
            return instant
        }
    }

    fun process(src: Bitmap, @FloatRange(from = 0.0,to = 25.0)  radius: Float): Bitmap {
        val input = Allocation.createFromBitmap(rs, src,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT)
        val output: Allocation = Allocation.createTyped(rs, input.type)
        val script  = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(src)
        return  src
    }

    private val bitmapWeakPool = HashMap<String,SoftReference<Bitmap>>()

    fun putBitmap(width: Int,height: Int,src: SoftReference<Bitmap>){
        src.get()?.apply {
            val key = "${width}&${height}"
            bitmapWeakPool[key] = src
        }
    }
    fun getBitmap(width: Int,height:Int) = bitmapWeakPool["${width}&${height}"]
}