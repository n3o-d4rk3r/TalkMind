package bd.com.pseudotic.talkmind.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import bd.com.pseudotic.talkmind.R

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import bd.com.pseudotic.talkmind.adapters.MessageAdapter
import bd.com.pseudotic.talkmind.clients.OpenAIClient
import bd.com.pseudotic.talkmind.databinding.ActivityChatBinding
import bd.com.pseudotic.talkmind.models.Chat
import bd.com.pseudotic.talkmind.utils.Constants
import bd.com.pseudotic.talkmind.utils.RecyclerItemDecoration

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import java.util.*

class ChatActivity : AppCompatActivity() {

    // View binding for the chat activity
    private var  chatActivityBinding : ActivityChatBinding? = null
    // Instance of OpenAIClient for handling AI chatbot messages
    private var openAIClient: OpenAIClient? = null
    // ArrayList to store messages in the chat
    private var messageLists : ArrayList<Chat> = ArrayList()
    // Adapter for displaying messages in the RecyclerView
    private var mAdapter : MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        chatActivityBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatActivityBinding!!.root)

        // Initialize Shared Preferences
        val spf = getSharedPreferences(Constants.SPF_NAME, MODE_PRIVATE)
        val darkMode : Boolean = spf.getBoolean(Constants.SPF_DATA, false)

        // Set functionality for Dark Mode toggle button
        chatActivityBinding!!.ivMode.setOnClickListener {
            if (darkMode) {
                // Disable Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                spf.edit().putBoolean(Constants.SPF_DATA, false).apply()
            } else {
                // Enable Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                spf.edit().putBoolean(Constants.SPF_DATA, true).apply()

            }
        }

        // Set functionality for Go Back button
        chatActivityBinding!!.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize Adapter and RecyclerView
        mAdapter  = MessageAdapter(this, messageLists)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatActivityBinding!!.rvChats.layoutManager = layoutManager
        chatActivityBinding!!.rvChats.addItemDecoration(RecyclerItemDecoration())
        chatActivityBinding!!.rvChats.adapter = mAdapter


        // Set functionality for Message Send button
        chatActivityBinding!!.ivSend.setOnClickListener {
            val prompt = chatActivityBinding!!.etText.text.toString()
            if (prompt == "" || prompt.isEmpty()){
                addText(Constants.TYPE_AI, Constants.EMPTY_AI_MESSAGE)
            }else {
                userPrompt(prompt, Constants.TYPE_USER)
                if (checkForInternet(this)){ sentRequest()}
            }
        }

        // Initialize OpenAI
        setupOpenAI()

        /**
         * @param savedInstanceState - The saved instance state of the activity
         * */
        savedInstanceState?.let {
            messageLists = it.getParcelableArrayList("messageLists") ?: ArrayList()
            if (messageLists.size >= 1){
                chatActivityBinding!!.tvIntro.visibility = View.GONE
            }
            mAdapter?.updateList(messageLists)
            mAdapter?.notifyDataSetChanged()
        }

    }

    /**
     * Save and restore data for $messageLists when the activity is destroyed and recreated
     **/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("messageLists", messageLists)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        messageLists = savedInstanceState.getParcelableArrayList("messageLists")!!
    }

    // Clean up resources when the activity is destroyed
    override fun onDestroy() {
        chatActivityBinding = null
        super.onDestroy()
    }

    // Set up OpenAI client with API key and model parameters
    private fun setupOpenAI() {
        openAIClient = OpenAIClient()
        openAIClient!!.setModel(Constants.MODEL)
        openAIClient!!.setApiUrl(Constants.BASE_URL)
        openAIClient!!.setApiKey(Constants.API_KEY)
        val enable: Boolean =
            openAIClient!!.getModel().equals(OpenAIClient.GPT_3_5_TURBO)
        openAIClient!!.setMaxTokensEnabled(enable)
        openAIClient!!.setMaxTokens(Constants.MAX_TOKENS.toDouble())
        openAIClient!!.setTemperature(Constants.TEMPERATURE.toDouble())
    }

    // Send user input to OpenAI API and update UI with response
    private fun sentRequest() {
        val userInput: String = chatActivityBinding!!.etText.text.toString()
        if (!TextUtils.isEmpty(userInput)) {
            userPrompt(userInput, 2)
            chatActivityBinding!!.etText.setText("")
            try {
                openAIClient!!.setPrompt(userInput)
                openAIClient!!.generateResponse(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(Constants.TAG, e.message.toString()) }
                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body!!.string()
                        try {
                            updateText(messageLists.size - 1, openAIClient!!.getResponse(responseBody)!!)
                        } catch (e: JSONException) {
                            try {
                                removeMessage(messageLists.size - 1)
                            } catch (ex: JSONException) {
                                // Handle errors
                                Log.e(Constants.TAG, ex.message!!)
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                // Handle errors
                Log.e(Constants.TAG, e.message!!)
            }
        }
    }

    // Add gpt message and user message to the chat UI
    private fun userPrompt(message: String, type: Int) {
        when (type) {
            2 -> {
                val chatMessage = Chat(Constants.TYPE_AI, Constants.TYPE_AI, "typing...")
                runOnUiThread {
                    chatActivityBinding!!.tvIntro.visibility = View.GONE
                    messageLists.add(chatMessage)
                    mAdapter!!.notifyItemInserted(messageLists.size - 1)
                    chatActivityBinding!!.rvChats.smoothScrollToPosition(messageLists.size - 1)
                }            }
            1 -> {
                val chatMessage = Chat(type, type, message)
                runOnUiThread {
                    chatActivityBinding!!.tvIntro.visibility = View.GONE
                    messageLists.add(chatMessage)
                    mAdapter!!.notifyItemInserted(messageLists.size - 1)
                    chatActivityBinding!!.rvChats.smoothScrollToPosition(messageLists.size - 1)

                    if (!checkForInternet(this)){
                        addText(Constants.TYPE_AI, Constants.NO_AI_INTERNET)
                        chatActivityBinding!!.etText.setText("")
                    }
                }
            }
            else -> {
                updateText(type, message)
            }
        }
    }

    // Add a message to the chat UI
    private fun addText(type: Int, message: String){
        val chat = Chat(type, type ,message)
        runOnUiThread {
            messageLists.add(chat)
            mAdapter!!.notifyItemInserted(messageLists.size - 1)
            chatActivityBinding!!.rvChats.smoothScrollToPosition(messageLists.size - 1)

        }
    }

    // Updates the text of a message based on its type
    private fun updateText(type: Int, message: String){
        runOnUiThread {
            messageLists[type].message = message
            mAdapter!!.notifyItemChanged(messageLists.size - 1)
            chatActivityBinding!!.rvChats.smoothScrollToPosition(messageLists.size - 1)

        }
    }

    // Removes a message at a given position from the message list
    private fun removeMessage(position: Int) {
        runOnUiThread {
            messageLists.removeAt(position)
            mAdapter!!.notifyItemRemoved(position)
            chatActivityBinding!!.rvChats.smoothScrollToPosition(messageLists.size - 1)

        }
    }

    // Checks if the device is currently connected to the internet
    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

}
