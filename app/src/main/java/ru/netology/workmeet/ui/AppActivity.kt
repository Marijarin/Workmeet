package ru.netology.workmeet.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.ActivityAppBinding
import ru.netology.workmeet.ui.NewPostFragment.Companion.textArg
import ru.netology.workmeet.viewModel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController
    private var menuProvider: MenuProvider? = null

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val alertDialog: AlertDialog? = let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.sign_out
                ) { _, _ ->
                    appAuth.removeAuth()
                    findNavController(R.id.nav_host_fragment).navigateUp()
                }
            }
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(R.string.want_to_sign_out)
            builder.create()
        }
        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_postFeedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_eventFeedFragment_to_newEventFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_wallFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.postFeedFragment,
                R.id.eventFeedFragment,
                R.id.wallFragment
            )

        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)


        authViewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        checkGoogleApiAvailability()

        authViewModel.data.observe(this) {
            menuProvider?.also(::removeMenuProvider)
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    menu.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
                    menu.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signout -> {
                            alertDialog?.show()
                            true
                        }
                        R.id.signin -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
                            true
                        }
                        R.id.signup -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signUpFragment)
                            true
                        }
                        else -> false
                    }
            }.apply {
                menuProvider = this
            })
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }



    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

    }
}