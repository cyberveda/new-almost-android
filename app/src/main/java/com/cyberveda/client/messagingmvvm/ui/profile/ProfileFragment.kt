package com.cyberveda.client.messagingmvvm.ui.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cyberveda.client.R
import com.cyberveda.client.databinding.ProfileFragmentBinding
import com.cyberveda.client.messagingmvvm.Utils.AuthEmailUtil
import com.cyberveda.client.messagingmvvm.Utils.CLICKED_USER
import com.cyberveda.client.messagingmvvm.Utils.LOGGED_USER
import com.cyberveda.client.messagingmvvm.Utils.LoadState
import com.cyberveda.client.messagingmvvm.Utils.eventbus_events.KeyboardEvent
import com.cyberveda.client.messagingmvvm.model.User
import com.cyberveda.client.messagingmvvm.ui.main_activity.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottom_sheet_profile_picture.view.*
import kotlinx.android.synthetic.main.profile_fragment.*
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream

const val SELECT_PROFILE_IMAGE_REQUEST = 5
const val REQUEST_IMAGE_CAPTURE = 6

class ProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    lateinit var binding: ProfileFragmentBinding
    lateinit var adapter: FriendsAdapter

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = getString(R.string.my_profile)
        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        sharedViewModel = ViewModelProvider(activity!!).get(SharedViewModel::class.java)

        //setup bottom sheet
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)


        //get user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(LOGGED_USER, null)
        val loggedUser: User = gson.fromJson(json, User::class.java)
        //show user name & email & bio
        binding.bioTextView.text = loggedUser.bio ?: getString(R.string.about_placeholder_text)
        binding.ageTextView.text = loggedUser.age?.toString() ?: getString(R.string.your_age)

        if (loggedUser.gender == getString(R.string.male)) {
            binding.genderRadioGroup.check(R.id.rb_male)
        } else if (loggedUser.gender == getString(R.string.female)) {
            binding.genderRadioGroup.check(R.id.rb_female)
        }

        if (loggedUser.maritalStatus == getString(R.string.married)) {
            binding.maritalStatusRadioGroup.check(R.id.rb_married)
        } else if (loggedUser.maritalStatus == getString(R.string.not_married)) {
            binding.maritalStatusRadioGroup.check(R.id.rb_not_married)
        }


        binding.email.text = loggedUser.email
        binding.name.text = loggedUser.username
        //download profile photo
        setProfileImage(loggedUser.profile_picture_url)


        //create adapter and handle recycle item click callback
        adapter = FriendsAdapter(object : FriendsAdapter.ItemClickCallback {
            override fun onItemClicked(user: User) {

                Toast.makeText(context, "Your Friend ${user.email}", Toast.LENGTH_SHORT)
                    .show()


                if (AuthEmailUtil.getAuthEmail() == user.email){
                    Toast.makeText(context, "Go to My Profile", Toast.LENGTH_SHORT)
                        .show()
                } else{
                    val clickedUserString = gson.toJson(user)
                    var bundle = bundleOf(
                        CLICKED_USER to clickedUserString
                    )

                    findNavController().navigate(
                        R.id.action_profileFragment_to_differentUserProfile,
                        bundle
                    )
                }




            }
        })


        //load friends of logged in user and show in recycler
        sharedViewModel.loadFriends(loggedUser).observe(this, Observer { friendsList ->
            //hide loading
            binding.loadingFriendsImageView.visibility = View.GONE
            if (friendsList != null) {
                binding.friendsLayout.visibility = View.VISIBLE
                binding.noFriendsLayout.visibility = View.GONE
                showFriendsInRecycler(friendsList)
            } else {
                binding.friendsLayout.visibility = View.GONE
                binding.noFriendsLayout.visibility = View.VISIBLE
                binding.addFriendsButton.setOnClickListener {
                    this@ProfileFragment.findNavController()
                        .navigate(R.id.action_profileFragment_to_findUserFragment)
                }
            }

        })



        binding.bottomSheet.cameraButton.setOnClickListener {
            openCamera()
        }
        binding.bottomSheet.galleryButton.setOnClickListener {
            selectFromGallery()
        }

        binding.bottomSheet.hide.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        //show selection bottom sheet when those buttons clicked
        binding.profileImage.setOnClickListener { selectProfilePicture() }
        binding.cameraImageView.setOnClickListener { selectProfilePicture() }


        //edit bio handle click
        binding.editBioTextview.setOnClickListener {
            if (binding.editBioTextview.text.equals(getString(R.string.edit_messaging))) {
                //show edit text to allow user to edit bio and change text view text to submit
                binding.editBioTextview.text = getString(R.string.submit_messaging)
                binding.editBioTextview.setTextColor(Color.GREEN)
                binding.bioTextView.visibility = View.GONE
                binding.newBioEditText.visibility = View.VISIBLE


            } else if (binding.editBioTextview.text.equals(getString(R.string.submit_messaging))) {
                //hide edit text and upload changes to user document
                binding.editBioTextview.text = getString(R.string.edit_messaging)
                binding.editBioTextview.setTextColor(Color.parseColor("#b39ddb"))
                binding.bioTextView.visibility = View.VISIBLE
                binding.bioTextView.text = binding.newBioEditText.text
                binding.newBioEditText.visibility = View.GONE
                EventBus.getDefault().post(KeyboardEvent())
                //upload bio to user document
                viewModel.updateBio(binding.newBioEditText.text.toString())

                //hide keyboard
                EventBus.getDefault().post(KeyboardEvent())
            }
        }


        //edit age handle click
        binding.editAgeTextview.setOnClickListener {
            if (binding.editAgeTextview.text.equals(getString(R.string.edit_messaging))) {
                //show edit text to allow user to edit bio and change text view text to submit
                binding.editAgeTextview.text = getString(R.string.submit_messaging)
                binding.editAgeTextview.setTextColor(Color.GREEN)
                binding.ageTextView.visibility = View.GONE
                binding.newAgeEditText.visibility = View.VISIBLE


            } else if (binding.editAgeTextview.text.equals(getString(R.string.submit_messaging))) {
                //hide edit text and upload changes to user document
                binding.editAgeTextview.text = getString(R.string.edit_messaging)
                binding.editAgeTextview.setTextColor(Color.parseColor("#b39ddb"))
                binding.ageTextView.visibility = View.VISIBLE
                binding.ageTextView.text = binding.newAgeEditText.text
                binding.newAgeEditText.visibility = View.GONE
                EventBus.getDefault().post(KeyboardEvent())
                //upload age to user document


                if (binding.ageTextView.text.toString().toInt() < 1 || binding.ageTextView.text.toString().toInt() > 130) {

                    binding.ageTextView.text = getString(R.string.your_age)
                    //age check
                    Toast.makeText(context, "Please enter appropriate age.", Toast.LENGTH_SHORT)
                        .show()
                    //hide keyboard
                    EventBus.getDefault().post(KeyboardEvent())
                } else {
                    viewModel.updateAge(binding.newAgeEditText.text.toString().toInt())

                    //hide keyboard
                    EventBus.getDefault().post(KeyboardEvent())
                }


            }
        }


        //edit gender handle click
        gender_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_male -> {
                    Toast.makeText(context, "Your Friend ${checkedId}", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.updateGender(binding.rbMale.text.toString())
                    EventBus.getDefault().post(KeyboardEvent())


                }
                R.id.rb_female -> {
                    Toast.makeText(context, "Your Friend ${checkedId}", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.updateGender(binding.rbFemale.text.toString())
                    EventBus.getDefault().post(KeyboardEvent())


                }

            }

        }

        //edit marital status handle click
        marital_status_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_married -> {
                    Toast.makeText(context, "${checkedId}", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.updateMaritalStatus(binding.rbMarried.text.toString())
                    EventBus.getDefault().post(KeyboardEvent())


                }
                R.id.rb_not_married -> {
                    Toast.makeText(context, "${checkedId}", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.updateMaritalStatus(binding.rbNotMarried.text.toString())
                    EventBus.getDefault().post(KeyboardEvent())


                }

            }

        }

        //edit education handle click

        val spinner: Spinner = activity!!.findViewById(R.id.educations_spinner)
        spinner.onItemSelectedListener = this
        // Create an ArrayAdapter using the string array and a default spinner layout
        var adapterSpinner = ArrayAdapter.createFromResource(
            this@ProfileFragment.context!!,
            R.array.educations_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }


        if (loggedUser.education != null) {

            var spinnerPosition: Int = adapterSpinner.getPosition(loggedUser.education)
            binding.educationsSpinner.setSelection(spinnerPosition)
        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

        Toast.makeText(context, "${parent?.getItemAtPosition(pos)}", Toast.LENGTH_SHORT)
            .show()
        viewModel.updateEducation(parent?.getItemAtPosition(pos).toString())

    }

    private fun setProfileImage(profilePictureUrl: String?) {
        Glide.with(this).load(profilePictureUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.anonymous_profile)
                    .circleCrop()
            )
            .into(binding.profileImage)
    }

/*    private fun uploadTakenImage(view: View) {
//create bitmap from profile imageView then upload it as bytearray
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        //upload image and show loading layout while uploading
        viewModel.uploadImageAsBytearray(data).observe(this, Observer { imageUploadState ->
            setProfileImageLoadUi(imageUploadState)
        })


        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }*/


    private fun showFriendsInRecycler(it: List<User>) {
        adapter.setDataSource(it)
        binding.friendsRecycler.adapter = adapter
        binding.friendsCountTextView.text = it.size.toString()
    }

    private fun setProfileImageLoadUi(it: LoadState?) {
        when (it) {

            LoadState.SUCCESS -> {
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = 1f
            }
            LoadState.FAILURE -> {
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = 1f
            }
            LoadState.LOADING -> {
                binding.uploadProgressBar.visibility = View.VISIBLE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = .5f

            }
        }
    }


    private fun selectProfilePicture() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        //result of selecting image from gallery
        if (requestCode == SELECT_PROFILE_IMAGE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {

            //set selected image in profile image view and upload it

            //upload image
            viewModel.uploadProfileImageByUri(data.data)


        }


        //result of taking camera image
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap


            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()


            //upload image
            viewModel.uploadImageAsBytearray(byteArray)


        }

        //show loading layout while uploading
        viewModel.uploadImageLoadStateMutableLiveData.observe(
            this,
            Observer { imageUploadState ->
                setProfileImageLoadUi(imageUploadState)
            })


        //set new image in profile image view
        viewModel.newImageUriMutableLiveData.observe(this, Observer {
            setProfileImage(it.toString())
        })
    }


    private fun selectFromGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            SELECT_PROFILE_IMAGE_REQUEST
        )
    }


    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


}

