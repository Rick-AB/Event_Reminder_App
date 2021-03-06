package com.example.eventreminder.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.eventreminder.R
import com.example.eventreminder.database.EventDatabase
import com.example.eventreminder.databinding.ActivityMainBinding
import com.example.eventreminder.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var TAG = "com.example.eventreminder.screens"
    private lateinit var binding: ActivityMainBinding
    private lateinit var events: List<Event>
    private lateinit var db: EventDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        db = EventDatabase.getInstance(this)
        getAllEvents()

        val pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.mainActivityViewpager.adapter = pagerAdapter
    }

    private fun getAllEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val temp = db.eventDao().getAllEvents()
            withContext(Dispatchers.Main) {
                events = temp
                binding.mainActivityViewpager.adapter?.notifyDataSetChanged()
                if (events.isEmpty()) {
                    Log.d(TAG, "getAllEvents: showStuffffffffff")
                    showNoEventView()
                } else {
                    hideNoEventView()
                }
            }
        }
    }

    fun addEvent(view: View) {
        startActivity(Intent(this, EventActivity::class.java))
    }

    private fun showNoEventView() {
        binding.floatingActionButton.visibility = View.VISIBLE
        binding.guideline7.visibility = View.VISIBLE
        binding.noEventAddTv.visibility = View.VISIBLE
        binding.noEventTv.visibility = View.VISIBLE
        binding.mainActivityViewpager.visibility = View.GONE
    }

    private fun hideNoEventView() {
        binding.floatingActionButton.visibility = View.GONE
        binding.guideline7.visibility = View.GONE
        binding.noEventAddTv.visibility = View.GONE
        binding.noEventTv.visibility = View.GONE
        binding.mainActivityViewpager.visibility = View.VISIBLE
    }

    private fun deleteExpiredEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            db.eventDao().deleteExpiredEvents()
        }
    }

    override fun onResume() {
        super.onResume()
        deleteExpiredEvents()
        getAllEvents()
    }

    private inner class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int {
            return if (::events.isInitialized) events.size else 0
        }

        override fun getItem(position: Int): Fragment {
            return HomeFragment.newInstance(events[position])
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }
}