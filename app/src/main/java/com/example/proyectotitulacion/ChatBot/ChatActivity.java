package com.example.proyectotitulacion.ChatBot;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectotitulacion.Mapa.HomeActivity;
import com.example.proyectotitulacion.Citas.MainActivity;
import com.example.proyectotitulacion.PerfilUsuario.PerfilActivity;
import com.example.proyectotitulacion.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private RecyclerView recyclerQuick;
    private EditText editTextChatMessage;
    private Button buttonSendMessage;
    private ChatAdapter chatAdapter;
    private QuickReplyAdapter quickAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    // Base de conocimiento cargada de JSON: intent -> respuestas
    private Map<String, String[]> knowledge = new HashMap<>();

    // Intenciones -> patrones regex (coincidencia flexible)
    private final Map<String, Pattern> patterns = new HashMap<>();

    // Sinónimos (palabra -> intención)
    private final Map<String, String> synonyms = new HashMap<>();

    // Contexto simple
    private String currentContext = null; // ej: "cita", "horario", "servicios"

    private final Handler handler = new Handler();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        recyclerQuick = findViewById(R.id.recyclerViewChat);
        editTextChatMessage = findViewById(R.id.editTextChatMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        chatAdapter = new ChatAdapter(messageList);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Quick replies iniciales
        List<String> quick = new ArrayList<>();
        quick.add("Horarios");
        quick.add("Servicios");
        quick.add("Agendar cita");
        quick.add("Cancelar cita");
        quick.add("Dolor de espalda");
        quickAdapter = new QuickReplyAdapter(quick, text -> {
            addUserMessage(text);
            generateBotResponse(text);
        });
        LinearLayoutManager lmH = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerQuick.setLayoutManager(lmH);
        recyclerQuick.setAdapter(quickAdapter);

        // Carga del JSON
        loadKnowledgeFromAssets();
        buildPatternsAndSynonyms();

        buttonSendMessage.setOnClickListener(v -> {
            String user = editTextChatMessage.getText().toString().trim();
            if (user.isEmpty()) {
                Toast.makeText(this, "Escribe algo", Toast.LENGTH_SHORT).show();
                return;
            }
            addUserMessage(user);
            editTextChatMessage.setText("");
            generateBotResponse(user);
        });

        addBotMessage("¡Hola! Soy tu asistente virtual. ¿En qué puedo ayudarte?");

        // MENU
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_chat);
            nav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_chat) return true;
                else if (id == R.id.nav_calendar) { startNewActivity(MainActivity.class); return true; }
                else if (id == R.id.nav_home) { startNewActivity(HomeActivity.class); return true; }
                else if (id == R.id.nav_profile) { startNewActivity(PerfilActivity.class); return true; }
                return false;
            });
        }
    }

    private void addUserMessage(String text) { addMessage(new ChatMessage(text, true)); }
    private void addBotMessage(String text) { addMessage(new ChatMessage(text, false)); }
    private void addTyping() { addMessage(new ChatMessage("", false, true)); }

    private void addMessage(ChatMessage message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    private void replaceTypingWith(String botText) {
        // Busca último mensaje typing y lo reemplaza por texto real
        for (int i = messageList.size() - 1; i >= 0; i--) {
            ChatMessage m = messageList.get(i);
            if (m.isTyping()) {
                messageList.set(i, new ChatMessage(botText, false));
                chatAdapter.notifyItemChanged(i);
                recyclerViewChat.scrollToPosition(i);
                return;
            }
        }
        addBotMessage(botText);
    }

    private void generateBotResponse(String userMessageRaw) {
        String user = userMessageRaw.toLowerCase().trim();

        // 1) Contexto específico (si aplica)
        if (currentContext != null) {
            String contextual = handleContext(user, currentContext);
            if (contextual != null) {
                showTypingThen(contextual);
                return;
            }
        }

        // 2) Detectar intención por regex
        String intent = detectIntentByRegex(user);
        if (intent == null) {
            // 3) Fallback: sinónimos / palabras clave
            intent = detectIntentBySynonyms(user);
        }

        // 4) Ajuste de contexto
        if (intent != null) {
            if (intent.equals("cita") || intent.equals("agendar") || intent.equals("cancelar_cita")) {
                currentContext = "cita";
            } else if (intent.equals("horario")) {
                currentContext = "horario";
            } else if (intent.equals("servicios")) {
                currentContext = "servicios";
            } else {
                currentContext = null; // reset cuando sea algo puntual
            }
        }

        // 5) Responder desde la base de conocimiento
        String reply = pickReply(intent != null ? intent : "desconocido");
        showTypingThen(reply);
    }

    private void showTypingThen(String reply) {
        addTyping();
        int delay = 400 + random.nextInt(600); // 400–1000 ms
        handler.postDelayed(() -> replaceTypingWith(reply), delay);
    }

    private String handleContext(String user, String ctx) {
        if (ctx.equals("cita")) {
            if (user.contains("cancel")) return pickReply("cancelar_cita");
            if (user.contains("agendar") || user.contains("agenda") || user.contains("program")) return pickReply("agendar");
            if (user.contains("horario") || user.contains("dispon")) return pickReply("horario");
        }
        if (ctx.equals("horario")) {
            if (user.matches(".(sabado|sábado|domingo|fin de semana).")) {
                return "Los fines de semana (sábado y domingo) la clínica está cerrada.";
            }
            if (user.contains("abren") || user.contains("abierto")) return pickReply("horario");
        }
        if (ctx.equals("servicios")) {
            if (user.contains("espalda") || user.contains("lumb")) return pickReply("dolor_espalda");
            if (user.contains("rehab")) return pickReply("rehabilitacion");
            if (user.contains("lesion") || user.contains("lesión")) return pickReply("lesion");
        }
        return null;
    }

    private String detectIntentByRegex(String user) {
        for (Map.Entry<String, Pattern> e : patterns.entrySet()) {
            if (e.getValue().matcher(user).find()) return e.getKey();
        }
        return null;
    }

    private String detectIntentBySynonyms(String user) {
        // Tomamos la palabra más específica que aparezca
        String best = null; int bestLen = 0;
        for (Map.Entry<String, String> e : synonyms.entrySet()) {
            String key = e.getKey();
            if (user.contains(key) && key.length() > bestLen) {
                best = e.getValue();
                bestLen = key.length();
            }
        }
        return best;
    }

    private String pickReply(String intent) {
        String[] arr = knowledge.get(intent);
        if (arr == null || arr.length == 0) arr = knowledge.get("desconocido");
        if (arr == null || arr.length == 0) return "No entendí, ¿puedes intentar con otras palabras?";
        return arr[random.nextInt(arr.length)];
    }

    private void loadKnowledgeFromAssets() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("bot_responses.json");
            Reader r = new InputStreamReader(is);
            Type type = new TypeToken<Map<String, String[]>>(){}.getType();
            knowledge = new Gson().fromJson(r, type);
        } catch (Exception e) {
            Log.e("ChatActivity", "Error cargando bot_responses.json", e);
            Toast.makeText(this, "No se pudo cargar la base de respuestas", Toast.LENGTH_LONG).show();
        }
    }

    private void buildPatternsAndSynonyms() {
        // Regex flexibles por intención
        patterns.put("hola", Pattern.compile("\\b(hola|buenas|que tal|qué tal|hi|hello)\\b"));
        patterns.put("adios", Pattern.compile("\\b(adios|adiós|hasta luego|nos vemos|bye)\\b"));
        patterns.put("como_estas", Pattern.compile("\\b(c(o|ó)mo est(a|á)s|que tal estas)\\b"));
        patterns.put("gracias", Pattern.compile("\\b(gracias|mil gracias|te lo agradezco)\\b"));
        patterns.put("ayuda", Pattern.compile("\\b(ayuda|ayud(a|arme)|necesito ayuda|tengo una duda)\\b"));

        patterns.put("horario", Pattern.compile("\\b(horario|horas|abren|cierran|disponible|apertura|cierre)\\b"));
        patterns.put("cita", Pattern.compile("\\b(cita|citas|agendar cita|programar cita|mis citas)\\b"));
        patterns.put("agendar", Pattern.compile("\\b(agendar|programar|reservar)\\b"));
        patterns.put("cancelar_cita", Pattern.compile("\\b(cancelar|anular).(cita)|\\b(cita).(cancelar|anular)\\b"));

        patterns.put("servicios", Pattern.compile("\\b(servicios|que hacen|qué hacen|tratamientos|ofrecen)\\b"));
        patterns.put("dolor_espalda", Pattern.compile("\\b(dolor).*(espalda)|\\b(lumbalgia|lumba(r|l)|dorsalgia)\\b"));
        patterns.put("rehabilitacion", Pattern.compile("\\b(rehabilitaci(ó|o)n|rehab)\\b"));
        patterns.put("lesion", Pattern.compile("\\b(lesi(ó|o)n|torcedura|esguince|fractura)\\b"));

        // Sinónimos simples (palabra -> intención)
        synonyms.put("lumbalgia", "dolor_espalda");
        synonyms.put("lumbago", "dolor_espalda");
        synonyms.put("espalda baja", "dolor_espalda");
        synonyms.put("cadera", "lesion");
        synonyms.put("rodilla", "lesion");
        synonyms.put("hombro", "lesion");
        synonyms.put("agendar", "agendar");
        synonyms.put("cancelar", "cancelar_cita");
        synonyms.put("horarios", "horario");
        synonyms.put("servicios", "servicios");
    }

    private void startNewActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}