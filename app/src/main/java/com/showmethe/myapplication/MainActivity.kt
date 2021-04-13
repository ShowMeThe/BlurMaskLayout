package com.showmethe.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





    }

    private var bo = false
    private fun doMission(){
        Handler(Looper.getMainLooper()).postDelayed({
            if(bo){
                ivLogo.setImageResource(R.drawable.ic_launcher_background)
            }else{
                ivLogo.setImageResource(R.drawable.ic_launcher_foreground)
            }
            bo = !bo
            doMission()
        },1500)
    }


}
