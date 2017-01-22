package pennappsxv.voicer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final int REQ_CODE_SPEECH_INPUT = 1033;
    TextView tvSpeak;
    TextView tvTitle;
    DatabaseReference myRef;

    TextToSpeech tts;

    int stage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stage = 0;

//        Bindings
        tvSpeak = (TextView) findViewById(R.id.tv_speak);
        tvTitle = (TextView) findViewById(R.id.tv_title);

//        Firebase
        myRef = FirebaseDatabase.getInstance().getReference();

        /* TTS */
        tts = new TextToSpeech(getApplicationContext(), this);

    }

    @Override
    public void onInit(int status) {
        Log.d("Speech", "OnInit - Status ["+status+"]");
        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            tts.setLanguage(Locale.ENGLISH);
        }
        tts.speak("Welcome, please tap the screen to begin.", TextToSpeech.QUEUE_FLUSH, null,
                null);
    }

    public void promptSpeechInput(View view) {

        if (stage < 1) {
            tvTitle.setText("Having any problems?");
            tvTitle.setBackgroundColor(Color.parseColor("#2196F3"));
            tts.speak("What problems are you having? Press on the screen and speak to the " +
                    "microphone.", TextToSpeech.QUEUE_FLUSH, null, null);
            stage++;
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent
                    .LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now");
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        "Error, not supported",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String resul = result.get(0);

                    tvSpeak.setText("\""+ resul + "\"");

                    if (stage == 1) {
                        if (existsKeyword(resul, "problems? with my ")) {
                            String split[] = resul.trim().split("with my");
                            myRef.child("PROBLEMATIC_BODY_PART").setValue(WordUtils.capitalize(split[1].trim()));
                            stage++;

                            tvTitle.setText("Any symptoms?");
                            tvTitle.setBackgroundColor(Color.parseColor("#2196F3"));
                            tts.speak("Thank you, now what symptoms are you having?", TextToSpeech.QUEUE_FLUSH, null, null);

                        } else {
                            myRef.child("PROBLEMATIC_BODY_PART").setValue("NULL");
                            tvTitle.setText("Try Again");
                            tvTitle.setBackgroundColor(Color.parseColor("#E91E63"));
                            tts.speak("Sorry, we didn't get that, could you try again?",
                                    TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (stage == 2) {
                        myRef.child("SYMPTOMS").setValue(resul);
                    }
                }
                break;
            }

        }
    }

    private boolean existsKeyword(String text, String s) {
        Pattern p = Pattern.compile(s);
        Matcher m = p.matcher(text);
        return m.find();
    }
}
