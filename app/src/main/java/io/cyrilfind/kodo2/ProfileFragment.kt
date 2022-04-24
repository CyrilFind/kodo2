package io.cyrilfind.kodo2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.permissions.RequestAccess
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.photopicker.PhotoPicker
import io.cyrilfind.kodo2.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import com.google.modernstorage.storage.AndroidFileSystem

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var photoUri: Uri
    private val fileSystem by lazy { AndroidFileSystem(requireContext()) }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->
            if (accepted) launchCamera()
            else showExplanation()
        }

    private val photoPicker =
        registerForActivityResult(PhotoPicker()) { uris ->
            if (uris.isNotEmpty()) {
                photoUri = uris.first()
                handleImage()
            } else Snackbar.make(requireView(), "Capture failed", Snackbar.LENGTH_LONG)
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { accepted ->
            if (accepted) handleImage()
            else Snackbar.make(requireView(), "Capture failed", Snackbar.LENGTH_LONG)
        }

    private val requestStorage =
        registerForActivityResult(RequestAccess()) { accepted ->
            if (accepted) initUri()
            else showExplanation()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun handleImage() {
        binding.imageView.load(photoUri)
        val body = convert(photoUri)
        lifecycleScope.launch {
            ServiceLocator.userWebService.updateAvatar(body)
        }
    }

    private fun convert(uri: Uri): MultipartBody.Part {
        val fileInputStream = requireContext().contentResolver.openInputStream(uri)!!
        val fileBody = fileInputStream.readBytes().toRequestBody()
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = fileBody
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.takePictureButton.setOnClickListener {
            launchCameraWithPermission()
        }

        binding.uploadImageButton.setOnClickListener {
            photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
        }

        lifecycleScope.launch {
            val userInfo = ServiceLocator.userWebService.getInfo().body()!!
            binding.imageView.load(userInfo.avatarUrl) {
                // affiche une image par défaut en cas d'erreur:
                error(R.drawable.ic_launcher_background)
            }
        }
        requestStorage.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ_AND_WRITE,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.Self
            )
        )
    }

    private fun initUri() {
        photoUri = fileSystem.createMediaStoreUri(
            filename = "picture-${UUID.randomUUID()}.jpg",
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            directory = "Todo",
        )!!
    }

    private fun launchCameraWithPermission() {
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(requireContext(), camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
        when {
            isAlreadyAccepted -> launchCamera()
            isExplanationNeeded -> showExplanation()
            else -> requestCamera.launch(camPermission)
        }
    }

    private fun showExplanation() {
        AlertDialog.Builder(requireContext())
            .setMessage("On a besoin de la caméra sivouplé ! 🥺")
            .setPositiveButton("Bon, ok") { _, _ -> launchAppSettings() }
            .setNegativeButton("Nope") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun launchAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }

    private fun launchCamera() {
        openCamera.launch(photoUri)
    }
}