package com.example.six_fam

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.six_fam.databinding.FragmentHomeBinding
import com.example.six_fam.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment() {

    lateinit var mcontext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext=context
    }
    lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getdetails()
    }

    private fun getdetails() {
        val firestore = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users").document(currentUserEmail).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val name = document.getString("name")
                    val email = document.getString("mail")
                    // If you store the image URL in the document, you can retrieve it as well
                    val imageUrl = document.getString("imageUrl")

                    // Update the UI with the retrieved details
                    binding.usernameTextView.text = name
                    binding.emailTextView.text = email
                    Glide.with(binding.profileImage.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.profileImage)

                    // You may use a library like Picasso or Glide to load the image
                    // Example using Glide:
                    // Glide.with(this).load(imageUrl).into(binding.profileImage)
                }
            } else {
                // Handle errors
                // Log.e("ProfileFragment", "Error getting user details", task.exception)
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}

