package com.example.act_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientManagementActivity : AppCompatActivity() {

    private lateinit var sessionToken: String
    private lateinit var recyclerView: RecyclerView
    private var clientList: MutableList<Client> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_management)

        // Prevent bottom navigation from moving with keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Get session token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Set up RecyclerView for client list
        recyclerView = findViewById(R.id.clientRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up the Add Client button
        val addClientButton = findViewById<Button>(R.id.addClientButton)
        val clientNameEditText = findViewById<EditText>(R.id.editTextNewClient)

        addClientButton.setOnClickListener {
            val clientName = clientNameEditText.text.toString().trim()
            if (clientName.isNotEmpty()) {
                addClient(clientName)
                clientNameEditText.text.clear() // Clear input field after adding
            } else {
                Toast.makeText(this, "Please enter a client name", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch client list from the server
        fetchClientList()
    }

    private fun fetchClientList() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getClientList(request).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful) {
                    val clients = response.body()?.clients ?: emptyList()
                    clientList.clear()
                    clientList.addAll(clients)
                    if (clients.isEmpty()) {
                        Toast.makeText(this@ClientManagementActivity, "No clients found.", Toast.LENGTH_SHORT).show()
                    }
                    recyclerView.adapter = ClientRecyclerViewAdapter(clientList)
                } else {
                    Log.e("API_ERROR", "Failed to fetch client list: ${response.message()}")
                    Toast.makeText(this@ClientManagementActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching client list: ${t.message}")
                Toast.makeText(this@ClientManagementActivity, "Error fetching client list.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addClient(clientName: String) {
        val request = AddClientRequest(sessionToken, clientName)
        RetrofitClient.apiService.addClient(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@ClientManagementActivity, "Client added successfully", Toast.LENGTH_SHORT).show()
                    fetchClientList() // Refresh the client list
                } else {
                    Log.e("API_ERROR", "Failed to add client: ${response.message()}")
                    Toast.makeText(this@ClientManagementActivity, "Failed to add client.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error adding client: ${t.message}")
                Toast.makeText(this@ClientManagementActivity, "Error adding client.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeClient(client: Client) {
        val request = RemoveClientRequest(sessionToken, client.client_name)
        RetrofitClient.apiService.removeClient(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@ClientManagementActivity, "Client removed successfully", Toast.LENGTH_SHORT).show()
                    fetchClientList() // Refresh the client list
                } else {
                    Log.e("API_ERROR", "Failed to remove client: ${response.message()}")
                    Toast.makeText(this@ClientManagementActivity, "Failed to remove client.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error removing client: ${t.message}")
                Toast.makeText(this@ClientManagementActivity, "Error removing client.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class ClientRecyclerViewAdapter(private val clients: List<Client>) :
        RecyclerView.Adapter<ClientRecyclerViewAdapter.ClientViewHolder>() {

        inner class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val clientNameTextView: TextView = view.findViewById(R.id.clientNameTextView)
            val removeButton: ImageView = view.findViewById(R.id.removeClientButton) // Corrected here
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_client, parent, false)
            return ClientViewHolder(view)
        }

        override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
            val client = clients[position]
            holder.clientNameTextView.text = client.client_name
            holder.removeButton.setOnClickListener {
                removeClient(client)
            }
        }

        override fun getItemCount(): Int {
            return clients.size
        }
    }
}
