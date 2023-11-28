package com.example.cajeros.ui.perfil


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cajeros.R
import com.example.cajeros.databinding.AvatarSelectorBinding
import com.example.cajeros.databinding.FragmentProfileBinding
import com.example.cajeros.ui.auth.InicioSesion


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private var _bindingDialog: AvatarSelectorBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val bindingDialog get() = _bindingDialog!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _bindingDialog = AvatarSelectorBinding.inflate(inflater, container, false)

        val avatar: ImageButton = binding.avatar
        profileViewModel.background.observe(viewLifecycleOwner) {
            val field = R.drawable::class.java.getField(it)
            val id = field.getInt(null)
            avatar.background = getDrawable(this.requireActivity(), id)
        }
        val exitButton: ImageButton = bindingDialog.exitDialog
        val gridAvatares: GridLayout = bindingDialog.gridAvatares
        avatar.setOnClickListener{
            profileViewModel.abrirDialogAvatares(this.requireActivity(), R.style.TemaAvataresDialog, inflater, R.layout.avatar_selector, exitButton.id, gridAvatares.id)
        }
        val tv_email: TextView = binding.tvemail
        profileViewModel.tvemail.observe(viewLifecycleOwner){
            tv_email.text = it
        }

        val cerrarSesion: Button = binding.botonCerrarSesion
        cerrarSesion.setOnClickListener{
            profileViewModel.cerrarSesion()
            val intentLogin = Intent(this.requireActivity(), InicioSesion::class.java)
            startActivity(intentLogin)
            this.requireActivity().finish()
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