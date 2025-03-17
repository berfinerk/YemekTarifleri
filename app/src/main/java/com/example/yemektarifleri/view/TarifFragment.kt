package com.example.yemektarifleri.view

import android.Manifest
import android.app.DirectAction
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yemektarifleri.databinding.FragmentTarifBinding
import com.example.yemektarifleri.model.Tarif
import com.example.yemektarifleri.room.TarifDAO
import com.example.yemektarifleri.roomdb.TarifDataBase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>  // İzin istemek için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>  // Galeriye gitmek için
    private var secilenGorsel: Uri? = null // Seçilen görselin URI'sini tutar
    private var secilenBitmap: Bitmap? = null // Seçilen görselin bitmap halini tutar
    private var mDisposable = CompositeDisposable()
    private var secilenTarif :Tarif? =null

    private lateinit var db : TarifDataBase
    private lateinit var tarifDao : TarifDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher() // İzin ve galeriye gitme işlemleri için register işlemi

        db = Room.databaseBuilder(requireContext(),TarifDataBase::class.java, name = "Tarifler")
            .build()
        tarifDao=db.TarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener { gorselsec(it) }
        binding.kaydetBtn.setOnClickListener { kaydet(it) }
        binding.silBtn.setOnClickListener { sil(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (bilgi == "yeni") {
                // Yeni tarif ekleniyor
                secilenTarif=null
                binding.silBtn.isEnabled = false
                binding.kaydetBtn.isEnabled = true
            } else {
                // Önceden eklenmiş bir tarif gösteriliyor
                binding.silBtn.isEnabled = true
                binding.kaydetBtn.isEnabled = false
                val id =TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }
    }
    private fun handleResponse(tarif: Tarif){
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        secilenTarif=tarif
    }

    fun kaydet(view: View) {
        // Kaydetme işlemleri burada yapılacak
        val isim = binding.isimText.text.toString()
        val malzeme =binding.malzemeText.text.toString()

        if (secilenBitmap !=null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim, malzeme, byteDizisi)

            //RxJava
            mDisposable.add(
                tarifDao.insert(tarif)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }
    private fun handleResponseForInsert(){
        //bir önceki fragment a dön
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View) {
        if (secilenTarif != null){
            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }

    }

    fun gorselsec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) ve üzeri için izin kontrolü
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeriye ulaşıp fotoğraf seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            // Kullanıcı butona bastığında izin iste
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                } else {
                    // Daha önce izin sorulmamışsa direkt izin iste
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // Eğer izin verilmişse, galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            // Android 12 ve altı sürümler için izin kontrolü
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Galeriye ulaşıp fotoğraf seçmemiz lazım!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        // Galeri sonucu işleyecek launcher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFormResult = result.data
                if (intentFormResult != null) {
                    secilenGorsel = intentFormResult.data

                    try {
                        secilenBitmap = if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                        }
                        binding.imageView.setImageBitmap(secilenBitmap)
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                    }
                }
            }
        }

        // İzin sonucu işleyecek launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // Eğer izin verildiyse galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                // İzin verilmezse uyarı göster
                Toast.makeText(requireContext(), "İzin verilmedi!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap,maximumBoyut : Int) : Bitmap{
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani >1 ){
            //gorsel yatay
            width =maximumBoyut
            val kisaltilmisYukseklik = width / bitmapOrani
            height =kisaltilmisYukseklik.toInt()
        }else{
            //gorsel dikey
            height =maximumBoyut
            val kisaltilmisGenislik = height* bitmapOrani
            width =kisaltilmisGenislik.toInt()
        }


        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}