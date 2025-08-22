package com.example.proyectotitulacion.ChatBot;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectotitulacion.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnQuickReplyClickListener {

    private RecyclerView recyclerViewChat;
    private EditText editTextChatMessage;
    private Button buttonSendMessage;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    // Base de conocimiento cargada de JSON
    private List<IntentData> intents = new ArrayList<>();

    // Última intención para mantener contexto
    private String lastIntent = null;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextChatMessage = findViewById(R.id.editTextChatMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        chatAdapter = new ChatAdapter(messageList, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        loadKnowledgeFromJson();

        buttonSendMessage.setOnClickListener(v -> {
            String userMessage = editTextChatMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                sendMessage(userMessage);
                editTextChatMessage.setText("");
            }
        });
    }

    /** ======================
     * Manejo de mensajes
     * ====================== */
    private void sendMessage(String text) {
        // Mostrar mensaje del usuario
        messageList.add(new ChatMessage(text, true, false, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);

        // Mostrar "typing" del bot
        messageList.add(new ChatMessage("...", false, true, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);

        handler.postDelayed(() -> {
            // Eliminar typing
            int lastIndex = messageList.size() - 1;
            if (lastIndex >= 0 && messageList.get(lastIndex).isTyping()) {
                messageList.remove(lastIndex);
                chatAdapter.notifyItemRemoved(lastIndex);
            }

            // Procesar respuesta
            processBotReply(text);

        }, 1200); // delay de 1.2s para typing
    }

    private void processBotReply(String userText) {
        userText = userText.toLowerCase();

        // Buscar coincidencia con intents
        for (IntentData intent : intents) {
            // Verificar patrones
            for (String pattern : intent.patterns) {
                if (Pattern.compile(pattern).matcher(userText).find()) {
                    replyFromIntent(intent);
                    lastIntent = intent.tag;
                    return;
                }
            }
            // Verificar sinónimos
            for (String syn : intent.synonyms) {
                if (userText.contains(syn)) {
                    replyFromIntent(intent);
                    lastIntent = intent.tag;
                    return;
                }
            }
        }

        // Si no encontró nada, pero hay contexto
        if (lastIntent != null) {
            for (IntentData intent : intents) {
                if (intent.tag.equals(lastIntent)) {
                    replyFromIntent(intent);
                    return;
                }
            }
        }

        // Si no se encontró nada
        addBotMessage("Lo siento, no entendí tu mensaje.");
    }

    private void replyFromIntent(IntentData intent) {
        // Escoger respuesta aleatoria
        String[] replies = intent.responses;
        String botReply = replies[random.nextInt(replies.length)];
        addBotMessage(botReply);

        // Agregar quick replies si existen
        if (intent.quickReplies != null && !intent.quickReplies.isEmpty()) {
            for (String qr : intent.quickReplies) {
                messageList.add(new ChatMessage(qr, false, false, true));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
            }
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
        }
    }

    private void addBotMessage(String text) {
        messageList.add(new ChatMessage(text, false, false, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    /** ======================
     * Quick reply callback
     * ====================== */
    @Override
    public void onQuickReplyClick(String reply) {
        sendMessage(reply);
    }

    /** ======================
     * Cargar JSON de assets
     * ====================== */
    private void loadKnowledgeFromJson() {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("intents.json");
            Reader reader = new InputStreamReader(is);

            Type listType = new TypeToken<List<IntentData>>() {}.getType();
            intents = new Gson().fromJson(reader, listType);

            reader.close();
            is.close();
        } catch (Exception e) {
            Log.e("ChatActivity", "Error al cargar JSON", e);
        }
    }

    /** ======================
     * Modelo para JSON
     * ====================== */
    public static class IntentData {
        String tag;
        List<String> patterns;
        List<String> synonyms;
        String[] responses;
        List<String> quickReplies;
    }
}
