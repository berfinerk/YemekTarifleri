package com.example.yemektarifleri

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yemektarifleri.databinding.FragmentListeBinding
import com.example.yemektarifleri.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar

class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permission: ActivityResultLauncher<String>  //izin istemek için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>  //galeriye gitmek için
    private val secilenGorsel : Uri?=null
    private val secilenBitmap : Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener{gorselsec(it)}
        binding.kaydetBtn.setOnClickListener{kaydet(it)}
        binding.silBtn.setOnClickListener{sil(it)}

        arguments?.let {
          val bilgi=   TarifFragmentArgs.fromBundle(it).bilgi

            if (bilgi=="yeni"){
                //yeni tarif ekleniyor
                binding.silBtn.isEnabled=false
                binding.kaydetBtn.isEnabled=true
            }
            else{
                // Önceden eklenmiş bir tarif gösteriliyor
                binding.silBtn.isEnabled=true
                binding.kaydetBtn.isEnabled=false
            }

        }
    }

    fun kaydet(view:View){
        // Kaydetme işlemleri burada yapılacak

    }

    fun sil(view:View){
        // Silme işlemleri burada yapılacak

    }

    fun gorselsec(view:View){
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            // İzin verilmemiş, izin istemeliyiz.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
            // Kullanıcı daha önce reddetti, tekrar açıklama yapıp izin iste..
                Snackbar.make(
                    view,
                    "Galeriye ulaşıp fotoğraf seçmemiz lazım!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(
                    // İzin isteme
                    "izin ver",
                    View.OnClickListener {

                    }).show()
            }else{
            // Direkt izin iste
        } else{
            // İzin zaten var, galeriye git


        }


    }

    private fun registerLauncher() {

        activityResultLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val intentFormResult = result.data
                if (intentFormResult !=null)

            }

        }

        var permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //doğruysa izin verildi
                    //galeriye gidebiliriz

                } else {
                    //iizn verilmedi
                    Toast.makeText(requireContext(), "Izin verilmedi!", Toast.LENGTH_LONG).show()
                }


            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


