package com.example.regexvalidator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] options = {
            "zip-code",
            "address",
            "phone",
            "email",
            "pesel",
            "idcard",
            "name&surname",
            "password",
            "censorship"
    };

    String picked_data = "zip-code";
    EditText t;
    TextView result;
    TextView additional;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Spinner spinner = findViewById(R.id.data_type_picker);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter ad = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                options
        );

        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);

        t = (EditText) findViewById(R.id.text_to_validate);
        result = (TextView) findViewById(R.id.result);
        additional = (TextView) findViewById(R.id.additional_data);

        Button btn = (Button) findViewById(R.id.validate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    validateText();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void validateText() throws JSONException {
        String textToValidate = String.valueOf(t.getText());
        switch (picked_data) {
            case "zip-code":
                Pattern z = Pattern.compile("^[0-9]{2}(?:-[0-9]{3})?$");
                Matcher mz = z.matcher(textToValidate);
                boolean bz = mz.matches();
                if(bz){
                    RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
                    String fullApi = "https://kodpocztowy.intami.pl/api/" + textToValidate;
                    @SuppressLint("SetTextI18n") JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                            Request.Method.GET,
                            fullApi,
                            null,
                            (Response.Listener<JSONArray>) response -> {
                                try {
                                    JSONObject city = response.getJSONObject(0);
                                    String cityName = city.getString("miejscowosc");
                                    additional.setText(cityName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            (Response.ErrorListener) error -> {
                                Toast.makeText(MainActivity.this, "Some error occurred! Cannot fetch zip-code", Toast.LENGTH_LONG).show();
                            }
                    )
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("Accept", "application/json");
                            return params;
                        }
                    };

                    volleyQueue.add(jsonObjectRequest);
                }
                setResult(bz);
                break;
            case "address":
                Pattern a = Pattern.compile("^[a-z](?: [0-9])?$");
                Matcher ma = a.matcher(textToValidate);
                boolean ba = ma.matches();
                setResult(ba);
        }
    }



    @SuppressLint("SetTextI18n")
    private void setResult(boolean b) {
        if(b) {
            result.setTextColor(Color.parseColor("#45FF00"));
            result.setText("CORRECT");
        }
        else {
            result.setTextColor(Color.parseColor("#FF0000"));
            result.setText("WRONG");
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        picked_data = options[i];
        if (picked_data.equals("zip-code")) t.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6) });
        else t.setFilters(new InputFilter[] {});
        result.setText("");
        additional.setText("");
        t.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
//
    }
}