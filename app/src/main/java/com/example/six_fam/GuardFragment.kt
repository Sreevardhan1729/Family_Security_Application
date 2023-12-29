package com.example.six_fam


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.six_fam.databinding.ActivityLoginBinding
import com.example.six_fam.databinding.FragmentGuardBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class GuardFragment : Fragment(),InviteMailAdapter.OnActionClick {
    lateinit var mContext: Context
    private lateinit var adapter: InviteMailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    lateinit var binding: FragmentGuardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuardBinding.inflate(inflater, container, false)
        binding.sendInvite.setOnClickListener {
            sendInvite()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getInvites()
    }

    private fun getInvites() {
        val firestore = Firebase.firestore
        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.email.toString())
            .collection("invites").get().addOnCompleteListener {
                if(it.isSuccessful){
                    val list: MutableList<String> = mutableListOf()
                    for (item in it.result){
                        if (item.get("invite_status")==0L){
                            list.add(item.id)
                        }
                    }
                    if (!::adapter.isInitialized) {
                        adapter = InviteMailAdapter(list, this)
                        binding.inviteRecycler.layoutManager = LinearLayoutManager(context)
                        binding.inviteRecycler.adapter = adapter
                    } else {
                        adapter.updateDataset(list)
                    }
                }
            }
    }

    private fun sendInvite() {
        val mail = binding.inviteMail.text.toString()
        val firestore = Firebase.firestore
        val data = hashMapOf(
            "invite_status" to 0
        )
        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users").document(mail).collection("invites").document(senderMail)
            .set(data).addOnSuccessListener { }.addOnFailureListener { }
        binding.inviteMail.text= null
        Toast.makeText(mContext, "Invite Sent", Toast.LENGTH_LONG).show()
    }

    companion object {

        @JvmStatic
        fun newInstance() = GuardFragment()
    }

    override fun onAcceptClick(mail: String) {
        val firestore = Firebase.firestore
        val data = hashMapOf(
            "invite_status" to 1
        )
        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users").document(senderMail).collection("invites").document(mail)
            .set(data).addOnSuccessListener {
                Toast.makeText(mContext, "Accepted", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { }
        adapter.removeInvite(mail)
    }

    override fun onDenyClick(mail: String) {
        val firestore = Firebase.firestore
        val data = hashMapOf(
            "invite_status" to -1
        )
        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users").document(senderMail).collection("invites").document(mail)
            .set(data).addOnSuccessListener {
                Toast.makeText(mContext, "Accepted", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { }
        adapter.removeInvite(mail)
    }
}