package com.example.cajeros.ui.perfil

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cajeros.R
import com.example.cajeros.databinding.FragmentProfileBinding
import com.example.cajeros.ui.filtros.FilterViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val avatar: ImageButton = binding.avatar
        profileViewModel.background.observe(viewLifecycleOwner) {
            Log.d("testeo", "it: ${it}")
            val field = R.drawable::class.java.getField(it)
            Log.d("testeo", "field: ${field}")
            val id = field.getInt(null)
            avatar.background = getDrawable(this.requireActivity(), id)
        }
        return root
    }

    private fun refreshCurrentFragment(){
        val fragmentId = findNavController().currentDestination?.id
        findNavController().popBackStack(fragmentId!!,true)
        findNavController().navigate(fragmentId)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}