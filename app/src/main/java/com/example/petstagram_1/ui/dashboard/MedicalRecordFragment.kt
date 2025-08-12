// --- In file: ui/dashboard/MedicalRecordFragment.kt ---
package com.example.petstagram_1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petstagram_1.R

class MedicalRecordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medical_record, container, false)
    }
}