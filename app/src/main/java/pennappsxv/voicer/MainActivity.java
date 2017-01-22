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

    final String[] symDict = {"abdomen", "chest", "head", "hips", "foot", "thigh", "shoulder"};

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

                    if (stage == 1) {
                        tvSpeak.setText("\""+ resul + "\"");
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
                        int count = 0;
                        String words = "";
                        for (String s : symDict) {
                            if (existsKeyword(resul, s)) {
                                count++;
                                words += (s + " ");
                            }
                        }
                        if (count > 0) {
                            words = words.trim();
                            resul = words;
                            tts.speak("Thank you",
                                    TextToSpeech.QUEUE_FLUSH, null, null);
                            Intent myIntent = new Intent(this, listActivity.class);
                            myIntent.putExtra("body_part", words);
                            startActivity(myIntent);
                        } else {
                            myRef.child("SYMPTOMS").setValue("NULL");
                            tvTitle.setText("Try Again");
                            tvTitle.setBackgroundColor(Color.parseColor("#E91E63"));
                            tts.speak("Sorry, we didn't get that, could you try again?",
                                    TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                        myRef.child("SYMPTOMS").setValue(resul);
                        tvSpeak.setText("\""+ resul + "\"");
                    }
                }
                break;
            }

        }
    }


    /**
     * Searches a string for key words
     * @param searchString  the string to search in
     * @param key           the key for searching
     * @return              whether the key exists in the string
     */
    private boolean existsKeyword(String searchString, String key) {
        Pattern p = Pattern.compile(key);
        Matcher m = p.matcher(searchString);
        return m.find();
    }
}
