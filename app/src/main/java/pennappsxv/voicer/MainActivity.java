package pennappsxv.voicer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 1033;
    TextView tvSpeak;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeak = (TextView) findViewById(R.id.tv_speak);

        myRef = FirebaseDatabase.getInstance().getReference();

    }

    public void promptSpeechInput(View view) {
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
//                    Log.d("APP", result.get(0));

                    String resul = result.get(0);

                    tvSpeak.setText("\""+ resul + "\"");
                    myRef.child("speech_msg").setValue(resul);

                    if (existsKeyword(resul, "problem")) {
                        if (existsKeyword(resul, "with my")) {
                            Log.d("APP", "yep have with");
                            String splitted[] = resul.trim().split("with my");
                            myRef.child("problematic_body_part").setValue(splitted[1].trim());
                        } else {
                            myRef.child("problematic_body_part").setValue("NULL");
                        }
                        myRef.child("is_problem_phrase").setValue(true);
                    } else {
                        myRef.child("is_problem_phrase").setValue(false);
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
