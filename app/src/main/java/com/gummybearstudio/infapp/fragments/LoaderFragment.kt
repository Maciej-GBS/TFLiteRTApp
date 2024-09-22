package com.gummybearstudio.infapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.gummybearstudio.infapp.R
import com.gummybearstudio.infapp.backend.ModelFileProvider
import kotlinx.coroutines.launch

class LoaderFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ModelFileProvider.launchDialog(this) { navigateToCamera() }
    }

    private fun navigateToCamera() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (activity != null) {
                    Navigation.findNavController(requireActivity(), R.id.navigatorContainer).navigate(
                        LoaderFragmentDirections.actionLoaderToCamera())
                }
            }
        }
    }
}