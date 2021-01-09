package com.karkia.emojistatusapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karkia.emojistatusapp.filter.EmojiFilter
import com.karkia.emojistatusapp.model.EmojiUserModel
import com.karkia.emojistatusapp.view.UserViewHolder

class MainActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var rvUsers: RecyclerView

    // Access a Cloud Firestore instance from your Activity
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        rvUsers = findViewById(R.id.rvUsers)

        // query firestore DB to get our users and their emojis
        val query = db.collection("users")
        val options = FirestoreRecyclerOptions.Builder<EmojiUserModel>()
            .setQuery(query, EmojiUserModel::class.java)
            .setLifecycleOwner(this)
            .build()
        val adapter = object : FirestoreRecyclerAdapter<
                EmojiUserModel, UserViewHolder
                >(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(this@MainActivity)
                    .inflate(
                        android.R.layout.simple_list_item_2,
                        parent,
                        false
                    )

                return UserViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: UserViewHolder,
                position: Int,
                model: EmojiUserModel
            ) {
                val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
                val tvEmojis: TextView = holder.itemView.findViewById(android.R.id.text2)

                tvName.text = model.displayName
                tvEmojis.text = model.emojis
            }
        } // end of view adapter

        rvUsers.adapter = adapter
        rvUsers.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miLogout) {
            Log.i(TAG, "Logout")
            // simply logout the user
            auth.signOut()
            // navigate back to login screen
            val logoutIntent = Intent(this, LoginActivity::class.java)
            // clear the whole back-stack
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        } else if (item.itemId == R.id.miEdit) {
            Log.i(TAG, "Show alert dialog to edit emoji status")
            showAlertDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog() {
        val editText = EditText(this)
        // restrict input length and ensure only emojis are input
        val emojiFilter = EmojiFilter().setMainActivityContext(this@MainActivity)
        val lengthFilter = InputFilter.LengthFilter(13)
        editText.filters = arrayOf(lengthFilter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Please update your emoji")
            .setView(editText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            Log.i(TAG, "Clicked on positive button!")
            val enteredEmojis = editText.text.toString()
            if (enteredEmojis.isBlank()) {
                Toast.makeText(
                    this,
                    "Emoji field cannot be empty!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(
                    this,
                    "No signed in user found!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // if everything is okay, then update firestore with the new emoji
            db.collection("users").document(currentUser.uid)
                .update("emojis", enteredEmojis)
            dialog.dismiss()
        }
    }
}