package com.example.testtaskandroid

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.*
import dalvik.system.DexClassLoader
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

const val GAME_LENGTH_MILLISECONDS = 3000L
const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class MainActivity : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd
    private var mCountDownTimer: CountDownTimer? = null
    private var mGameIsInProgress = false
    private var mTimerMilliseconds = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        // Create the InterstitialAd and set it up.
        mInterstitialAd = InterstitialAd(this).apply {
            adUnitId = AD_UNIT_ID
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    Toast.makeText(this@MainActivity,
                        "onAdFailedToLoad() with error code: $errorCode",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onAdClosed() {
                    startAdmob()
                }
            })
        }

        // Create the "retry" button, which triggers an interstitial between admob plays.
        btn_load_ad.visibility = View.INVISIBLE
        btn_load_ad.setOnClickListener { showInterstitial() }
        btn_load_ad1.setOnClickListener { admobFromDex(this) }

        // Kick off the first play of the "admob."
        startAdmob()

    }

    private fun createTimer(milliseconds: Long) {
        mCountDownTimer?.cancel()

        mCountDownTimer = object : CountDownTimer(milliseconds, 50) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mTimerMilliseconds = millisUntilFinished
                timer.text = "seconds remaining: ${ millisUntilFinished / 1000 + 1 }"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                mGameIsInProgress = false
                timer.text = "done!"
                btn_load_ad.visibility = View.VISIBLE
            }
        }
    }

    // Show the ad if it's ready. Otherwise toast and restart the admob.
    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
            startAdmob()
        }
    }

    // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
    private fun startAdmob() {
        if (!mInterstitialAd.isLoading && !mInterstitialAd.isLoaded) {
            // Create an ad request.
            val adRequest = AdRequest.Builder().build()

            mInterstitialAd.loadAd(adRequest)
        }

        btn_load_ad.visibility = View.INVISIBLE
        resumeGame(GAME_LENGTH_MILLISECONDS)
    }

    private fun resumeGame(milliseconds: Long) {
        // Create a new timer for the correct length and start it.
        mGameIsInProgress = true
        mTimerMilliseconds = milliseconds
        createTimer(milliseconds)
        mCountDownTimer?.start()
    }

    // Resume the admob if it's in progress.
    public override fun onResume() {
        super.onResume()

        if (mGameIsInProgress) {
            resumeGame(mTimerMilliseconds)
        }
    }

    // Cancel the timer if the admob is paused.
    public override fun onPause() {
        mCountDownTimer?.cancel()
        super.onPause()
    }

    @Throws(Exception::class)
    fun admobFromDex(context: Context): String {
        val inputStream =
            context.resources.assets.open("classes.dex")
        val dexCacheFile =
            File(context.filesDir, "classes.dex")
        IOUtils.copy(inputStream, FileOutputStream(dexCacheFile))
        val dexClassLoader = DexClassLoader(
            dexCacheFile.absolutePath,
            context.cacheDir.path, context.cacheDir.path,
            context.javaClass.classLoader
        )
        val klass = dexClassLoader.loadClass("com.example.androidtesttaskdex.MainActivity")
        val method =
            klass.getDeclaredMethod("admobFromDex", Context::class.java)
        return method.invoke(context) as String
    }
}