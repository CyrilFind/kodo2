package io.cyrilfind.kodo2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import com.google.modernstorage.storage.SharedFileSystem
import io.cyrilfind.kodo2.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var photoUri: Uri

    private val sharedFileSystem by lazy { SharedFileSystem(requireContext()) }
    private val storagePermissions by lazy { StoragePermissions(requireContext()) }

    private val permissionAndCameraLauncher = registerForActivityResult(RequestAccess()) {
            
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { accepted ->
            if (accepted) handleImage()
            else Snackbar.make(requireView(), "Capture failed", Snackbar.LENGTH_LONG)
        }

    private fun handleImage() {
        binding.imageView.load(photoUri)
        val body = convert(photoUri)
        lifecycleScope.launch {
            ServiceLocator.userWebService.updateAvatar(body)
        }
    }

    private fun convert(uri: Uri): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = requireContext().contentResolver.openInputStream(uri)!!.readBytes()
                .toRequestBody()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.takePictureButton.setOnClickListener {
            launchCameraWithPermission()
        }
        lifecycleScope.launch {
            val userInfo = ServiceLocator.userWebService.getInfo().body()!!
            binding.imageView.load(userInfo.avatarUrl) {
                // affiche une image par défaut en cas d'erreur:
                error(R.drawable.ic_launcher_background)
            }
        }
    }

    private fun launchCameraWithPermission() {
        val camPermission = Manifest.permission.CAMERA
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionStatus = checkSelfPermission(requireContext(), camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
        when {
            storagePermissions.() && isAlreadyAccepted -> launchCamera()
            isExplanationNeeded -> showExplanation()
            else -> permissionAndCameraLauncher.launch(arrayOf(camPermission, storagePermission))
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
        lifecycleScope.launch {
            photoUri = sharedFileSystem.createMediaStoreUri(
                filename = "picture-${UUID.randomUUID()}.jpg",
                directory = "pictures"
            )!!
            cameraLauncher.launch(photoUri)
        }
    }
}