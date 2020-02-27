package com.cyberveda.client.messagingmvvm.ui.different_user_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cyberveda.client.R
import com.cyberveda.client.databinding.DifferentUserProfileFragmentBinding
import com.cyberveda.client.messagingmvvm.Utils.AuthEmailUtil
import com.cyberveda.client.messagingmvvm.Utils.CLICKED_USER
import com.cyberveda.client.messagingmvvm.model.User
import com.cyberveda.client.messagingmvvm.ui.main_activity.SharedViewModel
import com.cyberveda.client.messagingmvvm.ui.profile.FriendsAdapter
import com.google.gson.Gson

class DifferentUserProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {


    private lateinit var binding: DifferentUserProfileFragmentBinding
    private val adapter by lazy {
        FriendsAdapter(object : FriendsAdapter.ItemClickCallback {
            override fun onItemClicked(user: User) {
                //don't do anything for now
            }

        })
    }

    companion object {
        fun newInstance() = DifferentUserProfileFragment()
    }

    private lateinit var viewModel: DifferentUserProfileFragmentViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.different_user_profile_fragment,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(DifferentUserProfileFragmentViewModel::class.java)
        sharedViewModel = ViewModelProvider(activity!!).get(SharedViewModel::class.java)

        //get data of clicked user from find user fragment
        val gson = Gson()
        val user = gson.fromJson(arguments?.getString(CLICKED_USER), User::class.java)


        activity?.title = user.username?.split("\\s".toRegex())?.get(0) + "'s profile"

        //check if alreadyFriends
        viewModel.checkIfFriends(user.uid).observe(this, Observer { friendRequestState ->
            when (friendRequestState) {//change button color and icon to show that a request is sent or not
                DifferentUserProfileFragmentViewModel.FriendRequestState.SENT -> {
                    showButtonAsSentRequest()
                }
                DifferentUserProfileFragmentViewModel.FriendRequestState.NOT_SENT -> {
                    showButtonAsRequestNotSent()
                }
                DifferentUserProfileFragmentViewModel.FriendRequestState.ALREADY_FRIENDS -> {
                    showButtonAsAlreadyFriends()
                }
            }
        })


        //set data to views and download image
        binding.bioTextView.text = user.bio ?: getString(R.string.about_placeholder_text)
        binding.name.text = user.username
        binding.email.text = user.email

        if (AuthEmailUtil.getAuthEmail() != "admin@gmail.com") {
            binding.educationTextView.visibility = View.GONE
            binding.ageTextView.visibility = View.GONE
            binding.genderRadioGroup.visibility = View.GONE
            binding.maritalStatusRadioGroup.visibility = View.GONE
            binding.ageContainerDifferentProfile.visibility = View.GONE
            binding.genderContainerDifferentProfile.visibility = View.GONE
            binding.maritalStatusContainerDifferentProfile.visibility = View.GONE
            binding.educationContainerDifferentProfile.visibility = View.GONE
            binding.friendsLayout.visibility = View.GONE

        } else {


            binding.educationTextView.text =
                user.education ?: getString(R.string.education_details_placeholder)

            binding.ageTextView.text = user.age?.toString() ?: getString(R.string.your_age)

            if (user.gender == getString(R.string.male)) {
                binding.genderRadioGroup.check(R.id.rb_male)
            } else if (user.gender == getString(R.string.female)) {
                binding.genderRadioGroup.check(R.id.rb_female)
            }

            if (user.maritalStatus == getString(R.string.married)) {
                binding.maritalStatusRadioGroup.check(R.id.rb_married)
            } else if (user.maritalStatus == getString(R.string.not_married)) {
                binding.maritalStatusRadioGroup.check(R.id.rb_not_married)
            }

            //load friends of that user
            sharedViewModel.loadFriends(user).observe(this, Observer { friendsList ->
                if (friendsList.isNullOrEmpty()) {
                    binding.friendsTextView.text = getString(R.string.no_friends_messaging)
                } else {
                    binding.friendsTextView.text = getString(R.string.friends_messaging)
                    binding.friendsCountTextView.text = friendsList.size.toString()
                    showFriendsInRecycler(friendsList)
                }
            })

        }

        viewModel.downloadProfilePicture(user.profile_picture_url)


        //show downloaded image in profile imageview
        viewModel.loadedImage.observe(this, Observer {
            it.into(binding.profileImage)
        })


        binding.sendFriendRequestButton.setOnClickListener {
            //add id to sentRequests document in user
            if (binding.sendFriendRequestButton.text == getString(R.string.friend_request_not_sent_messaging)) {
                viewModel.updateSentRequestsForSender(user.uid)
                showButtonAsSentRequest()
            } else if (binding.sendFriendRequestButton.text == getString(R.string.cancel_request_messaging)) {
                viewModel.cancelFriendRequest(user.uid)
                showButtonAsRequestNotSent()
            } else if (binding.sendFriendRequestButton.text == getString(R.string.delete_from_friends_messaging)) {
                viewModel.removeFromFriends(user.uid)
                showButtonAsRequestNotSent()
            }
        }

        binding.goToChatDirectlyFromDifferentUserProfile.setOnClickListener {
            val clickedUser = arguments?.getString(CLICKED_USER)
            if (clickedUser != null) {

                var bundle = bundleOf(
                    CLICKED_USER to clickedUser
                )

                findNavController().navigate(
                    R.id.action_differentUserProfile_to_chatFragment, bundle
                )
            } else {
                Toast.makeText(
                    context,
                    "Something went wrong, please clear the data and cache from settings of the app",
                    Toast.LENGTH_LONG
                ).show()

            }


        }




    }

    private fun showFriendsInRecycler(friendsList: List<User>?) {
        
        adapter.setDataSource(friendsList)
        binding.friendsRecycler.adapter = adapter

    }

    //change button to show that users are friends
    private fun showButtonAsAlreadyFriends() {
        binding.sendFriendRequestButton.text =
            getString(R.string.delete_from_friends_messaging)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_remove_circle_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.red) }
    }


    //change sent button to show that no request is sent
    private fun showButtonAsRequestNotSent() {
        binding.sendFriendRequestButton.text =
            getString(R.string.friend_request_not_sent_messaging)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_person_add_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.greyMessaging) }
    }


    //change sent button to show that  request is sent
    private fun showButtonAsSentRequest() {
        binding.sendFriendRequestButton.text = getString(R.string.cancel_request_messaging)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_done_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.greenMessaging) }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
