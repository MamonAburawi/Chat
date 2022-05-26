package com.info.chat.screens.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.info.chat.R
import com.info.chat.utils.AuthUtil
import com.info.chat.utils.ErrorMessage
import com.info.chat.utils.LoadState
import com.info.chat.utils.eventbus_events.KeyboardEvent
import com.info.chat.databinding.SignupFragmentBinding
import org.greenrobot.eventbus.EventBus
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignupFragment : Fragment() {

    private lateinit var binding: SignupFragmentBinding
    private lateinit var pattern: Pattern

    companion object {
        fun newInstance() = SignupFragment()
    }

    private lateinit var viewModel: SignupViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.signup_fragment, container, false)
        return binding.root
    }





    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignupViewModel::class.java)


        //regex pattern to check email format
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$"
        pattern = Pattern.compile(emailRegex)


        //handle register click
        binding.registerButton.setOnClickListener {

            signUp()

        }


        //hide issue layout on x icon click
        binding.issue.cancelImage.setOnClickListener {
            binding.issue.root.visibility = View.GONE
        }

        //show proper loading/error ui
        viewModel.loadingState.observe(this, Observer {
            when (it) {
                LoadState.LOADING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.issue.root.visibility = View.GONE
                }
                LoadState.SUCCESS -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issue.root.visibility = View.GONE
                }
                LoadState.FAILURE -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issue.root.visibility = View.VISIBLE
                    binding.issue.textViewIssue.text = ErrorMessage.errorMessage
                }

            }
        })


        //sign up on keyboard done click when focus is on passwordEditText
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            signUp()
            true
        }

    }

    private fun signUp() {
        EventBus.getDefault().post(KeyboardEvent())

        binding.userName.isErrorEnabled = false
        binding.email.isErrorEnabled = false
        binding.password.isErrorEnabled = false


        if (binding.userName.editText!!.text.length < 4) {
            binding.userName.error = "User name should be at least 4 characters"
            return
        }


        //check if email is empty or wrong format
        if (!binding.email.editText!!.text.isEmpty()) {
            val matcher: Matcher = pattern.matcher(binding.email.editText!!.text)
            if (!matcher.matches()) {
                binding.email.error = "Email format isn't correct."
                return
            }
        } else if (binding.email.editText!!.text.isEmpty()) {
            binding.email.error = "Email field can't be empty."
            return
        }


        if (binding.password.editText!!.text.length < 6) {
            binding.password.error = "Password should be at least 6 characters"
            return
        }

        //email and pass are matching requirements now we can register to firebase auth

        viewModel.registerEmail(
            AuthUtil.firebaseAuthInstance,
            binding.email.editText!!.text.toString(),
            binding.password.editText!!.text.toString(),
            binding.userName.editText!!.text.toString()
        )


        viewModel.navigateToHomeMutableLiveData.observe(this, Observer { navigateToHome ->
            if (navigateToHome != null && navigateToHome) {
                this@SignupFragment.findNavController()
                    .navigate(R.id.action_signupFragment_to_homeFragment)
                Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show()
                viewModel.doneNavigating()
            }
        })

    }

}



