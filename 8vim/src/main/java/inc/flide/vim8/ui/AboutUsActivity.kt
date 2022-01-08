package inc.flide.vim8.ui

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import inc.flide.vim8.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us_activity_layout)
        val github: ConstraintLayout = findViewById(R.id.constraintLayout_github)
        github.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> github.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    github.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://github.com/flide/8vim")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
        val matrix: ConstraintLayout = findViewById(R.id.constraintLayout_matrix)
        matrix.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> matrix.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    matrix.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://app.element.io/#/room/#8vim/lobby:matrix.org")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
        val website: ConstraintLayout = findViewById(R.id.constraintLayout_website)
        website.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> website.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    website.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://8vim.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
        val twitter: ConstraintLayout = findViewById(R.id.constraintLayout_twitter)
        twitter.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> twitter.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    twitter.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://twitter.com/8vim_?s=09")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
        val googlePlayStore: ConstraintLayout = findViewById(R.id.constraintLayout_playstore)
        googlePlayStore.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> googlePlayStore.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    googlePlayStore.setBackgroundColor(Color.WHITE)
                    val uri =
                        Uri.parse("https://play.google.com/store/apps/details?id=inc.flide.vi8") // missing 'http://' will cause crashed
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
        val backArrow = findViewById<ImageView?>(R.id.back_arrow)
        backArrow.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                backArrow.setBackgroundColor(Color.LTGRAY)
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                backArrow.setBackgroundColor(Color.WHITE)
                val intent = Intent(this@AboutUsActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
            true
        }
    }
}