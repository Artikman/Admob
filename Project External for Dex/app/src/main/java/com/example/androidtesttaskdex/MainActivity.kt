package com.example.androidtesttaskdex

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import com.google.android.gms.ads.*

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
        showInterstitial()

        // Kick off the first play of the "admob."
        startAdmob()

    }

    private fun createTimer(milliseconds: Long) {
        mCountDownTimer?.cancel()

        mCountDownTimer = object : CountDownTimer(milliseconds, 50) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mTimerMilliseconds = millisUntilFinished
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                mGameIsInProgress = false
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
}