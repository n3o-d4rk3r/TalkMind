package bd.com.pseudotic.talkmind.utils

/**
 * Constants file containing various constants used throughout the project
 * Created by Kabir on 3/25/2023
 */
class Constants {
    companion object {
        // API Key for OpenAI API
        const val API_KEY: String = "your api key"

        // Base URL for OpenAI API
        const val BASE_URL = "https://api.openai.com/v1/chat/completions"

        // Model to use for generating text
        const val MODEL = "gpt-3.5-turbo"

        // Temperature parameter for text generation
        const val TEMPERATURE = 0

        // Max tokens parameter for text generation
        const val MAX_TOKENS = 150

        // Type of the chat message - user or AI
        const val TYPE_USER = 1
        const val TYPE_AI = 0

        // Shared Preferences keys
        const val SPF_NAME = "modes"
        const val SPF_DATA = "dark_mode"

        // Logcat tag for error logging
        const val TAG = "error"

        // Error messages
        const val EMPTY_AI_MESSAGE = "I apologize, but it seems like no input has been detected." +
                " Would you like to provide some input for me to work with?"
        const val NO_AI_INTERNET = "Apologies, but I require an internet connection to function " +
                "properly. Could you please verify that your device is connected to the internet?"
    }
}
