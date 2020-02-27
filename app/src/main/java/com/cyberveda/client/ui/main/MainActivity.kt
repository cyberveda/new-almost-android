package com.cyberveda.client.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.bumptech.glide.RequestManager
import com.cyberveda.client.R
import com.cyberveda.client.models.AUTH_TOKEN_BUNDLE_KEY
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.ui.BaseActivity
import com.cyberveda.client.ui.auth.AuthActivity
import com.cyberveda.client.ui.main.account.AccountFragment
import com.cyberveda.client.ui.main.account.BaseAccountFragment
import com.cyberveda.client.ui.main.account.ChangePasswordFragment
import com.cyberveda.client.ui.main.account.UpdateAccountFragment
import com.cyberveda.client.ui.main.blog.*

import com.cyberveda.client.ui.main.create_blog.BaseCreateBlogFragment
import com.cyberveda.client.ui.main.create_feedback.BaseCreateFeedbackFragment
import com.cyberveda.client.ui.main.create_feedback.CreateFeedbackFragment
import com.cyberveda.client.util.BOTTOM_NAV_BACKSTACK_KEY
import com.cyberveda.client.util.BottomNavController
import com.cyberveda.client.util.BottomNavController.*
import com.cyberveda.client.util.setUpNavigation
import com.cyberveda.client.viewmodels.ViewModelProviderFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar
import org.jetbrains.anko.startActivity
import javax.inject.Inject
import kotlin.math.log

class MainActivity : BaseActivity(),
    NavGraphProvider,
    OnNavigationGraphChanged,
    OnNavigationReselectedListener,
    MainDependencyProvider {
    private val TAG = "lgx_MainActivity"
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    override fun getGlideRequestManager(): RequestManager {
        return requestManager
    }

    override fun getVMProviderFactory(): ViewModelProviderFactory {
        return providerFactory
    }

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_blog,
            this,
            this
        )
    }

    override fun getNavGraphId(itemId: Int) = when (itemId) {
        R.id.nav_blog -> {
            R.navigation.nav_blog
        }
//        R.id.nav_create_feedback -> {
//            R.navigation.nav_create_feedback
//        }

//        R.id.nav_account -> {
//            R.navigation.nav_account
//        }

        R.id.nav_chat -> {
            R.navigation.nav_chat
        }
        else -> {
            R.navigation.nav_blog
        }
    }

    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment is BaseAccountFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseBlogFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseCreateBlogFragment) {
                    fragment.cancelActiveJobs()
                }

                if (fragment is BaseCreateFeedbackFragment) {
                    fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onReselectNavItem(
        navController: NavController,
        fragment: Fragment
    ) = when (fragment) {

        is ViewBlogFragment -> {
            navController.navigate(R.id.action_viewBlogFragment_to_home)
        }

        is UpdateBlogFragment -> {
            navController.navigate(R.id.action_updateBlogFragment_to_home)
        }

        is CreateFeedbackFragment -> {
            navController.navigate(R.id.action_createFeedbackFragment_to_blogFragment)

        }
        
//        is BlogFragment -> {
//            navController.navigate(R.id.action_blogFragment_self)
//        }

        
        is AccountFragment -> {
            navController.navigate(R.id.action_accountFragment_to_blogFragment)
        }

        is UpdateAccountFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_blogFragment)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_blogFragment)
        }

//        is UpdateAccountFragment -> {
//            navController.navigate(R.id.action_updateAccountFragment_to_home)
//        }
//
//        is ChangePasswordFragment -> {
//            navController.navigate(R.id.action_changePasswordFragment_to_home)
//        }

        else -> {
            // do nothing
        }
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupActionBar()
        setupBottomNavigationView(savedInstanceState)
        subscribeObservers()
        restoreSession(savedInstanceState)
    }


    private fun setupBottomNavigationView(savedInstanceState: Bundle?) {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.setupBottomNavigationBackStack(null)
            bottomNavController.onNavigationItemSelected()
        } else {
            (savedInstanceState[BOTTOM_NAV_BACKSTACK_KEY] as IntArray?)?.let { items ->
                val backstack = BackStack()
                backstack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backstack)
            }
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        savedInstanceState?.get(AUTH_TOKEN_BUNDLE_KEY)?.let { authToken ->
            sessionManager.setValue(authToken as AuthToken)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save auth token
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)

        // save backstack for bottom nav
        outState.putIntArray(
            BOTTOM_NAV_BACKSTACK_KEY,
            bottomNavController.navigationBackStack.toIntArray()
        )
    }

    fun subscribeObservers() {
        Log.d(TAG, "subscribeObservers: 186: triggered")
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivityMessaging, subscribeObservers: ViewState: ${authToken}")
            Log.d(TAG, "subscribeObservers: 191: does it trigger?")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
//        tool_bar.setOnClickListener {
//            //            navAuthActivity()
//            sessionManager.logout()
//        }
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)

        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }


}
