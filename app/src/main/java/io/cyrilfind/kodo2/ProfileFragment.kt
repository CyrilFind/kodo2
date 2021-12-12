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
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.Internal
import com.google.modernstorage.mediastore.MediaStoreRepository
import io.cyrilfind.kodo2.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    private var photoUri: Uri? = null
    private val mediaStore by lazy { MediaStoreRepository(requireContext()) }

    private val permissionAndCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->
            if (accepted) launchCamera()
            else showExplanation()
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { accepted ->
            if (accepted) binding.imageView.load(photoUri)
            else Snackbar.make(requireView(), "Capture failed", Snackbar.LENGTH_LONG)
        }

    init {
        lifecycleScope.launch {
            photoUri = mediaStore.createMediaUri(
                filename = "picture.jpg",
                type = FileType.IMAGE,
                location = Internal
            ).getOrThrow()
        }
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
            val camPermission = Manifest.permission.CAMERA
            val permissionStatus = checkSelfPermission(requireContext(), camPermission)
            val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
            val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
            when {
                isAlreadyAccepted -> launchCamera()
                isExplanationNeeded -> showExplanation()
                else -> permissionAndCameraLauncher.launch(camPermission)
            }
        }
    }

    private fun showExplanation() {
        AlertDialog.Builder(requireContext())
            .setMessage("On a besoin de la caméra sivouplé ! 🥺")
            .setPositiveButton("Bon, ok") { _, _ -> launchAppSettings() }
            .show()
    }

    private fun launchAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context?.packageName, null)
        )
        startActivity(intent)
    }

    private fun launchCamera() {
        cameraLauncher.launch(photoUri)
    }
}