package com.cyberveda.client.messagingmvvm.ui.findUser

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cyberveda.client.R
import com.cyberveda.client.databinding.FindUserFragmentBinding
import com.cyberveda.client.messagingmvvm.Utils.CLICKED_USER
import com.google.gson.Gson

class FindUserFragment : Fragment() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: FindUserFragmentBinding

    companion object {
        fun newInstance() = FindUserFragment()
    }

    private lateinit var viewModel: FindUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        activity?.title = "Search for friends"
        binding = DataBindingUtil.inflate(inflater, R.layout.find_user_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FindUserViewModel::class.java)
        // get list of users
        viewModel.loadUsers().observe(this, Observer { usersList ->
            //hide loading
            binding.loadingImage.visibility = View.GONE

            if (usersList.isNullOrEmpty()) {
                binding.noUsersLayout.visibility = View.VISIBLE
            } else {
                adapter.submitList(usersList)
                adapter.userList = usersList
            }


        })


        //setup recycler
        adapter = UserAdapter(UserClickListener { clickedUser ->

            val gson = Gson()


            if (clickedUser != null) {
                val clickedUserVar = gson.toJson(clickedUser)

                var bundle = bundleOf(
                    CLICKED_USER to clickedUserVar
                )


                findNavController().navigate(
                    R.id.action_findUserFragment_to_differentUserProfile,
                    bundle
                )
            } else {
                Toast.makeText(
                    context,
                    "Something went wrong, please clear the data and cache from settings of the app",
                    Toast.LENGTH_LONG
                ).show()

            }


        })

        binding.recycler.adapter = adapter


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


        inflater.inflate(R.menu.search_menu_messaging, menu)

        //do filtering when i type in search or click search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String?): Boolean {
                adapter.filter.filter(queryString)
                return false
            }

            override fun onQueryTextChange(queryString: String?): Boolean {
                adapter.filter.filter(queryString)
                if (queryString != null) {
                    adapter.onChange(queryString)
                }

                return false
            }
        })


    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_search -> {

            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }


}
