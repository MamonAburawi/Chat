package com.info.chat.screens.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.info.chat.R
import com.info.chat.utils.AuthUtil
import com.info.chat.utils.ErrorMessage
import com.info.chat.utils.LoadState
import com.info.chat.utils.eventbus_events.KeyboardEvent
import com.info.chat.databinding.*
import com.google.android.material.textfield.TextInputEditText
import org.greenrobot.eventbus.EventBus


class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding



    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false)

        //check if user has previously logged in
        if (AuthUtil.firebaseAuthInstance.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)


        // Navigate to signup fragment
        binding.gotoSignUpFragmentTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        //Report text change to viewModel and Observe if email format is correct
        binding.emailEditText.afterTextChanged { email ->
            viewModel.isEmailFormatCorrect(email).observe(this, Observer { isEmailFormatCorrect ->
                if (!isEmailFormatCorrect) {//email format is not correct
                    binding.email.error = getString(R.string.wrong_email_format)
                } else {
                    binding.email.isErrorEnabled = false
                }

            })
        }


        //password length must be at least 6 characters
        binding.passwordEditText.afterTextChanged {
            if (it.length < 6) {
                binding.password.error = getString(R.string.password_size)
            } else {
                binding.password.isErrorEnabled = false
            }
        }


        //handle login click
        binding.loginButton.setOnClickListener {
            login()
        }


        //hide issue layout on x icon click
        binding.issue.cancelImage.setOnClickListener {
            binding.issue.root.visibility = View.GONE
        }


        //login on keyboard done click when focus is on passwordEditText
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            login()
            true
        }

    }

    private fun login() {
        EventBus.getDefault().post(KeyboardEvent())
        if (binding.email.error != null || binding.password.error != null || binding.email.editText!!.text.isEmpty() || binding.password.editText!!.text.isEmpty()) {
            //name or password doesn't match format
            Toast.makeText(context, "Check email and password then retry.", Toast.LENGTH_LONG)
                .show()
        } else {

            //All fields are correct we can login
            viewModel.login(
                AuthUtil.firebaseAuthInstance,
                binding.email.editText!!.text.toString(),
                binding.password.editText!!.text.toString()
            ).observe(this, Observer { loadState ->

                when (loadState) {
                    LoadState.SUCCESS -> {   //triggered when login with email and password is successful
                        this@LoginFragment.findNavController()
                            .navigate(R.id.action_loginFragment_to_homeFragment)
                        Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                        viewModel.doneNavigating()
                    }
                    LoadState.LOADING -> {
                        binding.loadingLayout.visibility = View.VISIBLE
                        binding.issue.root.visibility = View.GONE
                    }
                    LoadState.FAILURE -> {
                        binding.loadingLayout.visibility = View.GONE
                        binding.issue.root.visibility = View.VISIBLE
                        binding.issue.textViewIssue.text = ErrorMessage.errorMessage
                    }
                }
            })

        }
    }


    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

    }
}
