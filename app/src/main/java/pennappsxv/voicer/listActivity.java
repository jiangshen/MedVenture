package pennappsxv.voicer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.util.Log;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.codec.digest.HmacUtils;
import java.nio.charset.StandardCharsets;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;


public class listActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private String TAG = "TAG";
    TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

    StringRequest postRequest = new StringRequest(
        Request.Method.POST,
        APIKeys.priaid_authservice_url,
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("Response", response);
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.Response", error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                try {
                    //String raw = "Bearer " + URLEncoder.encode(APIKeys.priaid_authservice_url,"UTF-8");
                    String encode = URLEncoder.encode(APIKeys.priaid_authservice_url,"UTF-8");


                    byte [] pass = APIKeys.apiMedicPassword.getBytes(StandardCharsets.UTF_8);
                    byte [] encodeURL = URLEncoder.encode(APIKeys.priaid_authservice_url,"UTF-8").getBytes(StandardCharsets.UTF_8);
                    byte[] hash = HmacUtils.hmacMd5(pass, encodeURL);
                    byte[] encoded = Base64.encodeBase64(hash);
                    String bearer_credentials = APIKeys.apiMedicUserName + ":" + encoded.toString();
                    params.put("Authorization", "Bearer " + bearer_credentials);


                }
                catch(UnsupportedEncodingException e) {
                    Log.d(TAG, "Unsupported encoding");

                }


                return params;
            }
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        tts = new TextToSpeech(this, this);
    }

    public void transition(View v) {
        Intent intent = new Intent(this, lastActivity.class);
        startActivity(intent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
        }
        tts.speak("Press next to continue", TextToSpeech.QUEUE_FLUSH, null,
                null);
    }
}
