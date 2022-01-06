package inc.flide.vim8.ui

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import inc.flide.vim8.R
import inc.flide.vim8.ui.AboutUsActivity

class AboutUsActivity : AppCompatActivity() {
    private var github: ConstraintLayout? = null
    private var matrix: ConstraintLayout? = null
    private var website: ConstraintLayout? = null
    private var twitter: ConstraintLayout? = null
    private var googlePlayStore: ConstraintLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us_activity_layout)
        github = findViewById(R.id.constraintLayout_github)
        github.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> github.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    github.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://github.com/flide/8vim")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        matrix = findViewById(R.id.constraintLayout_matrix)
        matrix.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> matrix.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    matrix.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://app.element.io/#/room/#8vim/lobby:matrix.org")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        website = findViewById(R.id.constraintLayout_website)
        website.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> website.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    website.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://8vim.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        twitter = findViewById(R.id.constraintLayout_twitter)
        twitter.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> twitter.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    twitter.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://twitter.com/8vim_?s=09")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        googlePlayStore = findViewById(R.id.constraintLayout_playstore)
        googlePlayStore.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> googlePlayStore.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    googlePlayStore.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://play.google.com/store/apps/details?id=inc.flide.vi8") // missing 'http://' will cause crashed
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        val backArrow = findViewById<ImageView?>(R.id.back_arrow)
        backArrow.setOnTouchListener { view: View?, motionEvent: MotionEvent? ->
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                backArrow.setBackgroundColor(Color.LTGRAY)
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                backArrow.setBackgroundColor(Color.WHITE)
                val intent = Intent(this@AboutUsActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
            true
        }
    }
}