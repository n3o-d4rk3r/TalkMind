package bd.com.pseudotic.talkmind


import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import bd.com.pseudotic.talkmind.activities.ChatActivity
import bd.com.pseudotic.talkmind.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View binding for the main activity
    private var mainActivityBinding : ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding!!.root)


        // Continue button listener to start the chat activity
        mainActivityBinding!!.btnContinue.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

    }

    override fun onDestroy() {
        // Clear view binding on destroy to avoid memory leaks
        mainActivityBinding = null
        super.onDestroy()
    }
}