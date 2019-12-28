package com.example.ourchat.ui.profile

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.ourchat.R
import com.example.ourchat.databinding.ProfileFragmentBinding
import com.example.ourchat.ui.main.MainActivity
import com.example.ourchat.ui.main.SharedViewModel
import com.example.ourchat.ui.main.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_profile_picture.view.*
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    lateinit var binding: ProfileFragmentBinding
    lateinit var mainActivity: MainActivity

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel
    private var sharedViewModel: SharedViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        sharedViewModel =
            activity?.let { ViewModelProviders.of(it).get(SharedViewModel::class.java) }


        //todo show user data(image , about , friends)
        viewModel.downloadBio()
        viewModel.downloadProfileImage()

        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)


        binding.bottomSheet.cameraButton.setOnClickListener {
            mainActivity = activity as MainActivity
            mainActivity.dispatchTakePictureIntent()
        }
        binding.bottomSheet.galleryButton.setOnClickListener {
            mainActivity = activity as MainActivity
            mainActivity.selectFromGallery()
        }
        //show selection bottom sheet when those buttons clicked
        binding.profileImage.setOnClickListener { selectProfilePicture() }
        binding.cameraImageView.setOnClickListener { selectProfilePicture() }


        //Observe camera image change from parent activity
        sharedViewModel?.imageBitmap?.observe(this, androidx.lifecycle.Observer {
            binding.profileImage.setImageBitmap(it)


            // Get the data from an ImageView as bytes
            binding.profileImage.isDrawingCacheEnabled = true
            binding.profileImage.buildDrawingCache()
            val bitmap = (binding.profileImage.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            sharedViewModel?.uploadImageAsBytearray(data)


            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })

        //Observe gallery image change from parent activity
        sharedViewModel?.galleryImageUri?.observe(this, androidx.lifecycle.Observer {
            binding.profileImage.setImageURI(it)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })


        //Observe upload image state and show appropriate ui
        sharedViewModel?.uploadState?.observe(this, androidx.lifecycle.Observer {
            when (it) {
                SharedViewModel.UploadState.UPLOADING -> {
                    binding.uploadProgressBar.visibility = View.VISIBLE
                    binding.uploadText.visibility = View.VISIBLE
                    binding.profileImage.alpha = .5f

                }
                SharedViewModel.UploadState.SUCCESS -> {
                    binding.uploadProgressBar.visibility = View.GONE
                    binding.uploadText.visibility = View.GONE
                    binding.profileImage.alpha = 1f
                    Toast.makeText(context, "Upload successful.", Toast.LENGTH_SHORT).show()
                }
                SharedViewModel.UploadState.FAILURE -> {
                    binding.uploadProgressBar.visibility = View.GONE
                    binding.uploadText.visibility = View.GONE
                    binding.profileImage.alpha = 1f
                    Toast.makeText(context, "Upload failed, retry later.", Toast.LENGTH_LONG).show()
                }
            }
        })


        //edit bio handle click
        binding.editTextview.setOnClickListener {
            if (binding.editTextview.text.equals(getString(R.string.edit))) {
                //show edit text to allow user to edit bio and change text view text to submit
                binding.editTextview.text = getString(R.string.submit)
                binding.editTextview.setTextColor(Color.GREEN)

                binding.editEdittext.visibility = View.VISIBLE


            } else if (binding.editTextview.text.equals(getString(R.string.submit))) {
                //hide edit text and upload changes to user document
                binding.editTextview.text = getString(R.string.edit)
                binding.editTextview.setTextColor(Color.CYAN)

                //upload bio to user document
                viewModel.uploadBio(binding.editEdittext.text.toString())
                binding.editEdittext.visibility = View.GONE

                //hide keyboard
                mainActivity = activity as MainActivity
                hideKeyboard(mainActivity)
            }
        }


        //Observe if bio uploaded to user document
        viewModel.bioUploadState.observe(this, Observer {
            when (it) {
                ProfileViewModel.BioState.UPLOADING -> {
                    binding.bioProgressBar.visibility = View.VISIBLE
                }

                ProfileViewModel.BioState.SUCCESS -> {
                    //bio updated successfully
                    binding.aboutMeText.text = binding.editEdittext.text
                    Toast.makeText(context, "Bio updated", Toast.LENGTH_SHORT).show()
                    binding.bioProgressBar.visibility = View.GONE
                }

                ProfileViewModel.BioState.FAILURE -> {
                    Toast.makeText(context, "Error updating bio, retry later.", Toast.LENGTH_LONG)
                        .show()
                    binding.bioProgressBar.visibility = View.GONE
                }
            }

        })


        //Show loading untill bio is downloaded
        viewModel.bioDownloadState.observe(this, Observer {
            when (it) {
                ProfileViewModel.BioState.DOWNLOADING -> {
                    binding.bioProgressBar.visibility = View.VISIBLE
                }

                ProfileViewModel.BioState.SUCCESS -> {
                    //bio updated successfully
                    binding.bioProgressBar.visibility = View.GONE
                }

                ProfileViewModel.BioState.FAILURE -> {
                    binding.bioProgressBar.visibility = View.GONE
                }
            }
        })


        //show downloaded bio in textview
        viewModel.bio.observe(this, Observer {
            binding.aboutMeText.text = it
        })



    }


    private fun selectProfilePicture() {
        println("ProfileFragment.selectProfilePicture:")
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
