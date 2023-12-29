package com.example.six_fam

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.six_fam.databinding.ActivityMainBinding
import com.example.six_fam.databinding.ItemInviteBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val firestore1 = FirebaseFirestore.getInstance()
    val permissions= arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.INTERNET
    )
    val permissionCode = 78

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isAllPermissionsGranted()){
            if (isLocationEnabled(this)){
                setUpLocationListener()
            }
            else{
                showGPSNotEnabledDialog(this)
            }
        }
        else {
            askForPermission()
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        val name = currentUser?.displayName.toString()
        val mail = currentUser?.email.toString()
        val phoneNumber = currentUser?.phoneNumber.toString()
        val imageUrl = currentUser?.photoUrl.toString()
        val currentTimeString = getCurrentTimeString()

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val user = hashMapOf(
            "name" to name,
            "mail" to mail,
            "phoneNumber" to phoneNumber,
            "imageUrl" to imageUrl,
            "time" to currentTimeString
        )

        db.collection("users").document(mail).set(user).addOnSuccessListener {

        }.addOnFailureListener {  }

        binding.bottomBar.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.nav_guard -> {
                    inflateFragment(GuardFragment.newInstance())
                }
                R.id.nav_dashboard -> {
                    inflateFragment(MapsFragment())
                }
                R.id.nav_home -> {
                    inflateFragment(HomeFragment.newInstance())
                }
                R.id.nav_profile -> {
                    inflateFragment(ProfileFragment.newInstance())
                }
            }

            true
        }
        binding.bottomBar.selectedItemId=R.id.nav_home

        requestBatteryInfoAndSave()






        Toast.makeText(this, "Welcome", Toast.LENGTH_LONG).show()


    }



    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        Log.d("Location89","onLocationResult:latitude ${location.latitude}")
                        Log.d("Location89","onLocationResult:latitude ${location.longitude}")

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val mail = currentUser?.email.toString()

                        val db = Firebase.firestore

                        // Create a new user with a first and last name
                        val loacationData = mutableMapOf<String,Any>(
                            "lac" to location.latitude.toString(),
                            "loc" to location.longitude.toString()
                        )

                        db.collection("users").document(mail).update(loacationData).addOnSuccessListener {  }.addOnFailureListener {  }
                    }
                }
            },
            Looper.myLooper()
        )
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Function to show the "enable GPS" Dialog box
     */
    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable GPS")
            .setMessage("required_for_this_app")
            .setCancelable(false)
            .setPositiveButton("enable_now") { _, _ ->
                context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }
    fun isAllPermissionsGranted(): Boolean {
        for (item in permissions) {
            if( ContextCompat
                .checkSelfPermission(
                    this,item
                ) != PackageManager.PERMISSION_DENIED){
                return false
            }
        }
        return true
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this,permissions,permissionCode)
    }

    private fun inflateFragment(newInstance: Fragment) {
        val transaction=supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,newInstance)
        transaction.commit()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode==permissionCode){
            if(allPermissionGranted()){
                setUpLocationListener()
            }
            else{

            }
        }
    }

    private fun openCamera() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivity(intent)
    }
    private fun allPermissionGranted(): Boolean {
        for (item in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    item
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    private fun requestBatteryInfoAndSave() {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Get the battery percentage
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()

        // Save the battery percentage to the database
        saveBatteryPercentageToDatabase(batteryPercentage)
    }

    private fun saveBatteryPercentageToDatabase(percentage: Int) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

        val data = hashMapOf(
            "batteryPercentage" to percentage
        )

        firestore1.collection("users").document(currentUserEmail)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    private fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

}