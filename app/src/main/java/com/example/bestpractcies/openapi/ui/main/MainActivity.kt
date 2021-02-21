package com.example.bestpractcies.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.example.bestpractcies.R
import com.example.bestpractcies.openapi.BaseApplication
import com.example.bestpractcies.openapi.models.auth.AUTH_TOKEN_BUNDLE_KEY
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.ui.BaseActivity
import com.example.bestpractcies.openapi.ui.auth.AuthActivity
import com.example.bestpractcies.openapi.ui.main.account.ChangePasswordFragment
import com.example.bestpractcies.openapi.ui.main.account.UpdateAccountFragment
import com.example.bestpractcies.openapi.ui.main.blog.UpdateBlogFragment
import com.example.bestpractcies.openapi.ui.main.blog.ViewBlogFragment
import com.example.bestpractcies.openapi.util.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : BaseActivity(),
        BottomNavController.OnNavigationGraphChanged,
        BottomNavController.OnNavigationReselectedListener
{

    @Inject
    @Named("AccountFragmentFactory")
    lateinit var accountFragmentFactory: FragmentFactory

    @Inject
    @Named("BlogFragmentFactory")
    lateinit var blogFragmentFactory: FragmentFactory

    @Inject
    @Named("CreateBlogFragmentFactory")
    lateinit var createBlogFragmentFactory: FragmentFactory

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
                this,
                R.id.main_fragments_container,
                R.id.menu_nav_blog,
                this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()
        setupBottomNavigationView(savedInstanceState)

        restoreSession(savedInstanceState)
        subscribeObservers()
    }

    fun subscribeObservers(){

        sessionManager.cachedToken.observe(this, Observer{ authToken ->
            Timber.d("MainActivity, subscribeObservers: ViewState: $authToken")
            if(authToken == null || authToken.account_pk == -1 || authToken.token == null){
                navAuthActivity()
                finish()
            }
        })
    }

    override fun onGraphChange() {
        expandAppBar()
    }

    override fun onReselectNavItem(
            navController: NavController,
            fragment: Fragment
    ){
        Timber.d("logInfo: onReSelectItem")
        when(fragment){

            is ViewBlogFragment -> {
                navController.navigate(R.id.action_viewBlogFragment_to_home)
            }

            is UpdateBlogFragment -> {
                navController.navigate(R.id.action_updateBlogFragment_to_home)
            }

            is UpdateAccountFragment -> {
                navController.navigate(R.id.action_updateAccountFragment_to_home)
            }

            is ChangePasswordFragment -> {
                navController.navigate(R.id.action_changePasswordFragment_to_home)
            }

            else -> {
                // do nothing
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> onBackPressed()
        }
        return item.let { super.onOptionsItemSelected(it) }
    }

    override fun inject() {
        (application as BaseApplication).mainComponent()
                .inject(this)
    }


    private fun setupBottomNavigationView(savedInstanceState: Bundle?){
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.setupBottomNavigationBackStack(null)
            bottomNavController.onNavigationItemSelected()
        }
        else{
            (savedInstanceState[BOTTOM_NAV_BACKSTACK_KEY] as IntArray?)?.let { items ->
                val backstack = BottomNavController.BackStack()
                backstack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backstack)
            }
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?){
        savedInstanceState?.get(AUTH_TOKEN_BUNDLE_KEY)?.let{ authToken ->
            Timber.d("restoreSession: Restoring token: $authToken")
            sessionManager.setValue(authToken as AuthToken)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save auth token
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)

        // save backstack for bottom nav
        outState.putIntArray(BOTTOM_NAV_BACKSTACK_KEY, bottomNavController.navigationBackStack.toIntArray())
    }



    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    private fun setupActionBar(){
        setSupportActionBar(tool_bar)
    }

    private fun navAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseMainComponent()
    }

    override fun displayProgressBar(bool: Boolean){
        if(bool){
            progress_bar.visibility = View.VISIBLE
        }
        else{
            progress_bar.visibility = View.GONE
        }
    }


}