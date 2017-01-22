package pennappsxv.voicer;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class lastActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    String out = "Based on your information, you may have bronchitis, lung disease, cold or " +
            "strep throat. ";

    DatabaseReference myRef;

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last);

        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("RESULTS").setValue(out);

        tts = new TextToSpeech(this, this);
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            tts.setLanguage(Locale.ENGLISH);
        }
        tts.speak(out + "Thank you for using our service, goodbye!", TextToSpeech.QUEUE_FLUSH, null,
                null);
    }
}
