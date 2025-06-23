package com.example.proyectotitulacion;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextChatMessage;
    @SuppressWarnings("FieldCanBeLocal") // Suprime
    private Button buttonSendMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    @SuppressWarnings("FieldCanBeLocal")
    private Map<String, String[]> botResponses;


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

        // --- Inicialización de vistas del Chatbot ---
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextChatMessage = findViewById(R.id.editTextChatMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        if (recyclerViewChat == null || editTextChatMessage == null || buttonSendMessage == null) {
            Toast.makeText(this, "Error: Faltan vistas en el layout activity_chat.xml", Toast.LENGTH_LONG).show();
            return;
        }

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        initializeBotResponses();

        buttonSendMessage.setOnClickListener(v -> {
            String userMessageText = editTextChatMessage.getText().toString().trim();
            if (!userMessageText.isEmpty()) {
                addMessage(userMessageText, true);
                editTextChatMessage.setText("");
                generateBotResponse(userMessageText);
            } else {
                Toast.makeText(ChatActivity.this, "Escribe algo", Toast.LENGTH_SHORT).show();
            }
        });


        addMessage("¡Hola! Soy tu asistente virtual. ¿En qué puedo ayudarte?", false);

        // MENU
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_chat);
            nav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_chat) {
                    return true;
                } else if (id == R.id.nav_calendar) {
                    startNewActivity(MainActivity.class);
                    return true;
                } else if (id == R.id.nav_home) {
                    startNewActivity(HomeActivity.class);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startNewActivity(PerfilActivity.class);
                    return true;
                }
                return false;
            });
        } else {
            Log.w("ChatActivity", "BottomNavigationView (R.id.bottomNavigationView) no encontrado en el layout.");
        }
    }
    private void addMessage(String text, boolean isUser) {
        if (chatAdapter == null || messageList == null || recyclerViewChat == null) return;

        ChatMessage message = new ChatMessage(text, isUser);
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    private void initializeBotResponses() {
        botResponses = new HashMap<>();
        
        botResponses.put("hola", new String[]{
                "¡Hola! Soy tu asistente virtual de fisioterapia. ¿En qué puedo ayudarte hoy?",
                "¡Qué tal! Listo para ayudarte con tus consultas de fisioterapia.",
                "Hola, ¿cómo te puedo asistir hoy?"
        });
        botResponses.put("adios", new String[]{
                "¡Hasta luego! Espero haberte ayudado.",
                "Adiós, ¡que tengas un excelente día y pronta recuperación!",
                "Nos vemos. ¡Cuídate!"
        });
        botResponses.put("como estas", new String[]{
                "Estoy funcionando perfectamente, ¡lista para ayudarte!",
                "Muy bien, ¿y tú? ¿En qué te puedo asistir?"
        });
        botResponses.put("ayuda", new String[]{
                "Claro, dime qué necesitas relacionado con nuestros servicios de fisioterapia.",
                "Estoy aquí para ayudarte. ¿Tienes alguna pregunta sobre citas, horarios o servicios?"
        });
        botResponses.put("gracias", new String[]{
                "¡De nada! Es un placer ayudarte.",
                "Un placer. Si tienes más preguntas, no dudes en consultar.",
                "No hay de qué. ¡Estoy para servirte!"
        });
        botResponses.put("nombre", new String[]{
                "Soy FisioBot, tu asistente virtual para la clínica.",
                "Puedes llamarme Asistente Fisio."
        });

        //Horarios de Atención
        botResponses.put("horario", new String[]{
                "Nuestros horarios de atención son de lunes a viernes de 8:00 AM a 2:00 PM. Ten en cuenta que el horario de 1:00 PM a 2:00 PM no está disponible para citas.",
                "Atendemos de lunes a viernes. Puedes agendar citas entre las 8:00 AM y la 1:00 PM. El bloque de 1:00 PM a 2:00 PM está reservado. Cerramos a las 2:00 PM.",
                "Estamos abiertos de lunes a viernes desde las 8:00 AM hasta las 2:00 PM. No agendamos citas a la 1:00 PM."
        });
        botResponses.put("horas", new String[]{
                "Te refieres a nuestros horarios de atención, ¿verdad? Son de lunes a viernes de 8:00 AM a 2:00 PM, excepto a la 1:00 PM.",
                "Los horarios son de Lunes a Viernes, de 8:00 AM a 2:00 PM. La franja de 1:00 PM a 2:00 PM no está disponible."
        });
        botResponses.put("atienden", new String[]{
                "Atendemos de lunes a viernes de 8:00 AM a 2:00 PM. La hora de la 1:00 PM no está disponible.",
                "Nuestro horario de atención es de L-V de 8 AM a 2 PM, excluyendo la 1 PM."
        });
        botResponses.put("disponible", new String[]{
                "Para verificar disponibilidad de citas, por favor especifica un día. Nuestros horarios generales son de lunes a viernes de 8:00 AM a 2:00 PM, sin citas a la 1:00 PM.",
                "Si buscas disponibilidad, considera nuestro horario: lunes a viernes, 8:00 AM - 2:00 PM (excepto 1:00 PM)."
        });
        botResponses.put("abren", new String[]{
                "Abrimos de lunes a viernes a las 8:00 AM y cerramos a las 2:00 PM. No hay citas disponibles a la 1:00 PM."
        });
        botResponses.put("cierran", new String[]{
                "Cerramos a las 2:00 PM de lunes a viernes."
        });
        botResponses.put("lunes", new String[]{"Los lunes atendemos de 8:00 AM a 2:00 PM, excepto a la 1:00 PM."});
        botResponses.put("martes", new String[]{"Los martes el horario es de 8:00 AM a 2:00 PM, sin citas a la 1:00 PM."});
        botResponses.put("miercoles", new String[]{"Los miércoles estamos de 8:00 AM a 2:00 PM, excepto a la 1:00 PM.", "Miércoles: 8 AM - 2 PM (excluye 1 PM)."});
        botResponses.put("jueves", new String[]{"Los jueves atendemos de 8:00 AM a 2:00 PM, la 1:00 PM no está disponible."});
        botResponses.put("viernes", new String[]{"Los viernes nuestro horario es de 8:00 AM a 2:00 PM, excepto la 1:00 PM."});
        botResponses.put("sabado", new String[]{"Lo siento, no atendemos los sábados.", "La clínica permanece cerrada los sábados."});
        botResponses.put("domingo", new String[]{"No abrimos los domingos.", "Los domingos la clínica está cerrada."});
        botResponses.put("fin de semana", new String[]{"Durante el fin de semana (sábado y domingo) la clínica permanece cerrada."});

        //Citas
        botResponses.put("cita", new String[]{
                "Puedo ayudarte con información sobre citas. ¿Quieres saber cómo agendar, consultar horarios disponibles o cancelar una cita?",
                "Para citas, nuestros horarios son de L-V de 8:00 AM a 2:00 PM (excepto 1:00 PM). ¿Necesitas agendar?"
        });
        botResponses.put("agendar", new String[]{
                "Para agendar una cita, puedes indicarme un día y veré la disponibilidad, o puedes usar la sección de 'Calendario/Citas' de la app.",
                "Puedes intentar agendar desde la sección 'Citas' de la app o dime qué día te interesa."
        });
        botResponses.put("cancelar cita", new String[]{
                "Para cancelar una cita, por favor dirígete a la sección de 'Mis Citas' en la app o contacta directamente a la clínica llamando al [5512925537]."
        });

        // Servicios (Nuevos y expandidos)
        botResponses.put("servicios", new String[]{
                "Ofrecemos una variedad de servicios de fisioterapia, incluyendo rehabilitación post-operatoria, terapia manual, tratamiento de lesiones deportivas, electroterapia, entre otros. ¿Te interesa alguno en particular?",
                "Nuestros principales servicios son: terapia manual, rehabilitación de lesiones, fisioterapia deportiva y neurológica. ¿Buscas algo específico?"
        });
        botResponses.put("que hacen", new String[]{
                "Nos especializamos en tratamientos de fisioterapia para ayudarte a recuperar movilidad y aliviar el dolor. ¿Tienes alguna condición específica sobre la que quieras consultar?",
                "Ayudamos a las personas a mejorar su calidad de vida mediante la fisioterapia. ¿Qué tipo de tratamiento necesitas?"
        });
        botResponses.put("dolor de espalda", new String[]{
                "El dolor de espalda es una de nuestras especialidades. Podemos evaluarte y ofrecerte un plan de tratamiento. ¿Te gustaría saber más sobre cómo agendar una evaluación?",
                "Sí, tratamos el dolor de espalda. Una evaluación inicial nos permitirá determinar el mejor enfoque para ti."
        });
        botResponses.put("rehabilitacion", new String[]{
                "Ofrecemos programas de rehabilitación para diversas condiciones, como post-operatorios, lesiones deportivas o neurológicas. ¿Qué tipo de rehabilitación necesitas?"
        });
        botResponses.put("lesion", new String[]{
                "Tratamos una amplia gama de lesiones musculoesqueléticas. ¿Podrías describirme un poco tu lesión?"
        });
    }

    private void generateBotResponse(String userMessage) {
        if (botResponses == null) {
            initializeBotResponses();
        }

        String normalizedUserMessage = userMessage.toLowerCase().trim();
        String botReply = null;
        Random random = new Random();
        String bestMatchKeyword = null;
        int longestMatchLength = 0;

        for (String keyword : botResponses.keySet()) {
            if (normalizedUserMessage.contains(keyword)) {
                if (keyword.length() > longestMatchLength) {
                    longestMatchLength = keyword.length();
                    bestMatchKeyword = keyword;
                }
            }
        }

        if (bestMatchKeyword != null) {
            String[] possibleReplies = botResponses.get(bestMatchKeyword);
            if (possibleReplies != null && possibleReplies.length > 0) {
                botReply = possibleReplies[random.nextInt(possibleReplies.length)];
            }
        }

        if (botReply == null) {
            String[] defaultReplies = {
                    "Lo siento, no entendí eso. ¿Podrías reformular tu pregunta sobre nuestros servicios de fisioterapia?",
                    "No estoy seguro de cómo responder a eso. Intenta preguntarme sobre horarios, citas o tipos de tratamiento.",
                    "Hmm, eso es nuevo para mí. Puedes preguntarme sobre nuestros horarios de atención (L-V, 8 AM - 2 PM, excepto 1 PM) o los servicios que ofrecemos."
            };
            botReply = defaultReplies[random.nextInt(defaultReplies.length)];
        }
        addMessage(botReply, false);
    }
    private void startNewActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}