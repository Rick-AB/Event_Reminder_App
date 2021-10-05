package com.example.eventreminder.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.eventreminder.R
import com.example.eventreminder.databinding.FragmentHomeBinding
import com.example.eventreminder.model.Event
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class HomeFragment : Fragment() {

    private var TAG = "com.example.eventreminder.screens"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var event: Event
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        event = arguments?.getSerializable("event") as Event
        setListeners()
        updateData()
        return binding.root
    }

    companion object {
        fun newInstance(event: Event): HomeFragment {
            val homeFragment = HomeFragment()
            val bundle = Bundle()
            bundle.putSerializable("event", event)
            homeFragment.arguments = bundle
            return homeFragment
        }
    }


    private fun getDaysLeft(date: String): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale.US)
        val eventDate = LocalDate.parse(date, formatter)
        val now: LocalDate = LocalDate.now()
        val diff = Duration.between(now.atStartOfDay(), eventDate.atStartOfDay())
        val dateDifference = diff.toDays()

        return dateDifference.toString()
    }

    private fun setListeners() {
        binding.addEventTv.setOnClickListener {
            startEventActivity(null)
        }
        binding.editEventTv.setOnClickListener {
            startEventActivity(this.event)
        }
        binding.notificationsTv.setOnClickListener {
            startNotificationsActivity()
        }
    }

    private fun startNotificationsActivity() {
        val intent = Intent(requireContext(), NotificationsActivity::class.java)
        intent.putExtra("event", event)
        startActivity(intent)
    }

    private fun startEventActivity(event: Event?) {
        val intent = Intent(requireContext(), EventActivity::class.java)
        intent.putExtra("event", event)
        startActivity(intent)
    }

    private fun updateData() {
        Log.d(TAG, "updateData: $event")
        binding.eventTitleTv.text = event.title
        binding.countDownTv.text = getDaysLeft(event.date)
        binding.eventDateTv.text = event.date.replace("-", ".")
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }


}