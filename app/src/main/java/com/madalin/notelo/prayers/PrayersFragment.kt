package com.madalin.notelo.prayers

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.madalin.notelo.R

class PrayersFragment : Fragment() {

    companion object {
        fun newInstance() = PrayersFragment()
    }

    private lateinit var viewModel: PrayersViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prayers, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PrayersViewModel::class.java)
        // TODO: Use the ViewModel
    }

}