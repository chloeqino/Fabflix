package uci.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uci.fabflixmobile.data.NetworkManager;
import uci.fabflixmobile.databinding.ActivityLoginBinding;
import uci.fabflixmobile.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "ec2-54-203-218-206.us-west-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-fall22-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    JSONObject resjson = null;
                    try {
                        resjson = new JSONObject(response);

                        Log.d("login.success", resjson.getString("status"));
                        if (resjson.getString("status").equals("success")) {
                            finish();
                            // initialize the activity(page)/destination
                            Intent MainPage = new Intent(LoginActivity.this, MainActivity.class);
                            // activate the list page.
                            startActivity(MainPage);
                        }else{
                            message.setText(resjson.getString("message"));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Complete and destroy login activity once successful

                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("email", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("recap","false");
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}