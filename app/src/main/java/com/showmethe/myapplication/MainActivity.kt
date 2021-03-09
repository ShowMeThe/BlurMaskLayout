package com.showmethe.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bo = false

        btn.setOnClickListener {
            if(bo){
                ivLogo.setImageResource(R.drawable.ic_launcher_background)
            }else{
                ivLogo.setImageResource(R.drawable.ic_launcher_foreground)
            }
            bo = !bo
        }



    }
}
