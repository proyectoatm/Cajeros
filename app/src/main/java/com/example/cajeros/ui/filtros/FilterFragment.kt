package com.example.cajeros.ui.filtros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cajeros.R
import com.example.cajeros.databinding.DialogFilterBinding
import com.example.cajeros.databinding.FragmentFilterBinding
class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private var _bindingDialogFilter: DialogFilterBinding? = null
    private val bindingDialogFilter get() = _bindingDialogFilter!!
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val filterViewModel = ViewModelProvider(this).get(FilterViewModel::class.java)
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _bindingDialogFilter = DialogFilterBinding.inflate(inflater, container, false)

        val bancos: Button = binding.botonBancos
        filterViewModel.banco.observe(viewLifecycleOwner) {
            bancos.text = it
        }
        val scrollFilters: LinearLayout = bindingDialogFilter.scrollFilter
        bancos.setOnClickListener{
            filterViewModel.abrirDialogFilter(this.requireActivity(), R.style.TemaAvataresDialog, inflater, R.layout.dialog_filter, scrollFilters.id)
        }

        val dispos: Button = binding.botonDispos
        filterViewModel.dispo.observe(viewLifecycleOwner) {
            dispos.text = it
        }
        val scrollDispos: LinearLayout = bindingDialogFilter.scrollFilter
        dispos.setOnClickListener{
            filterViewModel.abrirDialogDispo(this.requireActivity(), R.style.TemaAvataresDialog, inflater, R.layout.dialog_filter, scrollDispos.id)
        }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}