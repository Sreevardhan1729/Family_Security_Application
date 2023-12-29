package com.example.six_fam

import android.Manifest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.six_fam.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment() {
    private val listContacts:ArrayList<ContactModel> = ArrayList()
    lateinit var inviteAdapter:InviteAdapter
    lateinit var mcontext:Context
    lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext=context

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAcceptedMembers()
        updateEmptyStateVisibility()
        val item = Manifest.permission.READ_CONTACTS
        if (ContextCompat.checkSelfPermission(requireContext(), item) == PackageManager.PERMISSION_GRANTED
        ) {

            inviteAdapter = InviteAdapter(listContacts)
            fecthDatabaseContacts()

            CoroutineScope(Dispatchers.IO).launch {
                insertDatabaseContacts(fetchContacts())
            }
        }
        binding.recyclerInvite.layoutManager=LinearLayoutManager(mcontext,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerInvite.adapter=inviteAdapter
        binding.threeDots.setOnClickListener {
            SharedPref.putBoolean(PrefConst.isUserLoggedIn,false)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(requireContext(), "LoggedOut", Toast.LENGTH_LONG).show()
        }
    }

    private fun fecthDatabaseContacts(){
        val database =six_famDatabase.getDatabase(requireContext())
        database.contactDao().getAllContacts().observe(viewLifecycleOwner){
            listContacts.clear()
            listContacts.addAll(it)
            inviteAdapter.notifyDataSetChanged()
        }


    }

    private suspend fun insertDatabaseContacts(listContacts: ArrayList<ContactModel>) {
        val database =six_famDatabase.getDatabase(requireContext())
        database.contactDao().insertAll(listContacts)
    }

    @SuppressLint("Range")
    private fun fetchContacts(): ArrayList<ContactModel>{
        val cr = requireActivity().contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        val listContacts: ArrayList<ContactModel> = ArrayList()

        if (cursor != null && cursor.count > 0) {

            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if (hasPhoneNumber > 0) {

                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        ""
                    )

                    if (pCur != null && pCur.count > 0) {

                        while (pCur != null && pCur.moveToNext()) {

                            val phoneNum =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                            listContacts.add(ContactModel(name, phoneNum))

                        }
                        pCur.close()

                    }

                }
            }

            if (cursor != null) {
                cursor.close()
            }

        }
        Log.d("FetchContact89", "fetchContacts: end")
        return listContacts

    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()

    }

    private fun getAcceptedMembers() {
        val firestore = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        // Fetch users who have accepted the current user's invitation (invite_status = 1)
        firestore.collection("users")
            .document(currentUserEmail)
            .collection("invites")
            .whereEqualTo("invite_status", 1)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val acceptedMembers = mutableListOf<MemberModel>()

                    for (document in task.result!!) {
                        // document.id is the email of the user who accepted the invitation
                        // Fetch the user's name from the "name" field in the user's document
                        val acceptedUserId = document.id
                        val acceptedUserDocRef = firestore.collection("users").document(acceptedUserId)

                        acceptedUserDocRef.get().addOnSuccessListener { userDocSnapshot ->
                            if (userDocSnapshot.exists()) {
                                val userName = userDocSnapshot.getString("name")
                                val battery = userDocSnapshot.getLong("batteryPercentage")?.toInt()
                                val lac = userDocSnapshot.getString("lac")
                                val loc = userDocSnapshot.getString("loc")
                                val imageUrl = userDocSnapshot.getString("imageUrl").toString()
                                val latitude = lac?.toDouble()
                                val longitude = loc?.toDouble()
                                val locationApiClient = LocationApiClient()
                                val time = userDocSnapshot.getString("time").toString()
                                var addressLine2: String=""
                                if (longitude != null && latitude != null) {
                                    Log.d("location99", "Requesting location for coordinates: $latitude, $longitude")

                                    locationApiClient.getLocationByCoordinates(latitude, longitude) { response ->
                                        Log.d("location99", "Location request callback received")

                                        try {
                                            val jsonResponse = JSONObject(response)

                                            // Check if the "features" array exists in the JSON response
                                            if (jsonResponse.has("features")) {
                                                val featuresArray = jsonResponse.getJSONArray("features")

                                                // Check if there are features in the array
                                                if (featuresArray.length() > 0) {
                                                    // Get the first feature from the array
                                                    val firstFeature = featuresArray.getJSONObject(0)

                                                    // Check if the "properties" object exists in the feature
                                                    if (firstFeature.has("properties")) {
                                                        val propertiesObject = firstFeature.getJSONObject("properties")

                                                        // Extract the 'address_line2' field from the "properties" object
                                                        addressLine2 = propertiesObject.optString("address_line2", "N/A")

                                                        // Now 'addressLine2' contains the value of 'address_line2'
                                                        Log.d("location99", "Address Line 2: $addressLine2")
                                                        if (userName != null) {
                                                            // Now you have the user's name, you can use it in your UI or data model
                                                            val acceptedMember = MemberModel(userName,battery,imageUrl,addressLine2,time)
                                                            acceptedMembers.add(acceptedMember)

                                                            // Update the UI with the accepted members
                                                            activity?.runOnUiThread {
                                                                updateUI(acceptedMembers)
                                                            }
                                                        }
                                                    } else {
                                                        // Handle the case where "properties" is not present in the feature
                                                        Log.d("location99", "No 'properties' object in the feature")
                                                    }
                                                } else {
                                                    // Handle the case where there are no features
                                                    Log.d("location99", "No features found")
                                                }
                                            } else {
                                                // Handle the case where the "features" key is not present
                                                Log.d("location99", "No 'features' key in the JSON response")
                                            }
                                        } catch (e: Exception) {
                                            // Log the exception to identify the issue
                                            Log.e("location99", "Exception: ${e.message}", e)
                                        }
                                    }
                                }


                            }
                        }
                    }
                } else {
                    // Handle errors
                    Toast.makeText(mcontext, "Error fetching accepted members", Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun updateUI(acceptedMembers: List<MemberModel>) {
        val adapter = MemberAdapter(acceptedMembers)
        binding.recyleMember.layoutManager = LinearLayoutManager(mcontext)
        binding.recyleMember.adapter = adapter
    }

    private fun updateEmptyStateVisibility() {
        val firestore = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        firestore.collection("users")
            .document(currentUserEmail)
            .collection("invites")
            .whereEqualTo("invite_status", 1)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        binding.recyleMember.visibility = View.VISIBLE
                        binding.empty.visibility=View.GONE
                    } else {
                        binding.recyleMember.visibility = View.GONE
                        binding.empty.visibility=View.VISIBLE
                    }
                } else {
                    // Handle the case when the query fails
                    val exception = task.exception
                    // Log or display the error
                }
            }
    }
}
