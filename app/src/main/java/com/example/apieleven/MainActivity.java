package com.example.apieleven;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    Button btnInsertUserInput, btnSearch;
    HashMap<String, String> hashMapOne;
    MyAdapter myAdapter = new MyAdapter();
    EditText etNameUserInput, etPhoneUserInput, etSearch;

    // আপনার লোকাল সার্ভারের URL
    private static final String BASE_URL = "http://192.168.0.101/api_eleven_folder/";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Volley RequestQueue একবার ইনিশিয়ালাইজ করা হলো (মেমোরি সেভ করার জন্য)
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        connect();
        insert();
        fetch();
        search();

        listView.setAdapter(myAdapter);
    }

    private void connect() {
        listView = findViewById(R.id.listView);
        btnInsertUserInput = findViewById(R.id.btnInsertUserInput);
        etNameUserInput = findViewById(R.id.etNameUserInput);
        etPhoneUserInput = findViewById(R.id.etPhoneUserInput);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
    }

    // --- ডাটাবেজ থেকে ডেটা খোঁজার লজিক (Search) ---
    private void search() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stSearch = etSearch.getText().toString().trim();
                String URL = BASE_URL + "search.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            arrayList.clear(); // নতুন ডেটা আসার আগে লিস্ট খালি করা হলো
                            JSONArray jsonArray = new JSONArray(s);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String stIDSearch = jsonObject.optString("id", "");
                                String stNameSearch = jsonObject.optString("name", "");
                                String stPhoneSearch = jsonObject.optString("phone", "");

                                hashMapOne = new HashMap<>();
                                hashMapOne.put("id", stIDSearch);
                                hashMapOne.put("name", stNameSearch);
                                hashMapOne.put("phone", stPhoneSearch);
                                arrayList.add(hashMapOne);
                            }
                            myAdapter.notifyDataSetChanged(); // লিস্টভিউ রিফ্রেশ
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("name", stSearch); // আপনার PHP কোডের $_POST['name'] এর সাথে ম্যাচড
                        return map;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
    }

    // --- ডাটাবেজে নতুন ডেটা ঢুকানোর লজিক (Create/Insert) ---
    private void insert() {
        btnInsertUserInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stNameTwo = etNameUserInput.getText().toString().trim();
                String stPhoneTwo = etPhoneUserInput.getText().toString().trim();
                String URL = BASE_URL + "insert.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        etNameUserInput.setText("");
                        etPhoneUserInput.setText("");
                        new AlertDialog.Builder(MainActivity.this).setMessage("Data inserted successfully").show();
                        fetch(); // নতুন ডেটা সহ লিস্ট আপডেট
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("name", stNameTwo);
                        map.put("phone", stPhoneTwo);
                        return map;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
    }

    // --- ডাটাবেজ থেকে সব ডেটা লোড করার লজিক (Read/Fetch) ---
    private void fetch() {
        arrayList.clear();
        String URL = BASE_URL + "fetch.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String stIDThree = jsonObject.optString("id", "");
                        String stNameThree = jsonObject.optString("name", "");
                        String stPhoneThree = jsonObject.optString("phone", "");

                        hashMapOne = new HashMap<>();
                        hashMapOne.put("id", stIDThree);
                        hashMapOne.put("name", stNameThree);
                        hashMapOne.put("phone", stPhoneThree);
                        arrayList.add(hashMapOne);
                    }
                    myAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }

    // --- কাস্টম অ্যাডাপ্টার ক্লাস (ListView Handler) ---
    public class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        // পলিশড: সঠিক পজিশন রিটার্ন যা স্ক্রোলিং ক্র্যাশ আটকাবে
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // পলিশড: ভিউ রিইউজ কন্ডিশন (মেমোরি লিক ও ক্র্যাশ ডিফেন্স)
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout, parent, false);
            }

            TextView tvIDLayout = convertView.findViewById(R.id.tvIDLayout);
            TextView tvNameLayout = convertView.findViewById(R.id.tvNameLayout);
            TextView tvPhoneLayout = convertView.findViewById(R.id.tvPhoneLayout);
            Button btnUpdateLayout = convertView.findViewById(R.id.btnUpdateLayout);
            Button btnDeleteLayout = convertView.findViewById(R.id.btnDeleteLayout);

            HashMap<String, String> hashMapTwo = arrayList.get(position);
            String stIDOne = hashMapTwo.get("id");
            String stNameOne = hashMapTwo.get("name");
            String stPhoneOne = hashMapTwo.get("phone");

            tvIDLayout.setText(stIDOne);
            tvNameLayout.setText(stNameOne);
            tvPhoneLayout.setText(stPhoneOne);

            // --- রো লেভেল ডেটা আপডেট লজিক (Update) ---
            btnUpdateLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout2, null);
                    builder.setView(dialogView);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    EditText etNameUpdateInput = dialogView.findViewById(R.id.etNameUpdateInput);
                    EditText etPhoneUpdateInput = dialogView.findViewById(R.id.etPhoneUpdateInput);
                    Button btnSaveUpdateInput = dialogView.findViewById(R.id.btnSaveUpdateInput);

                    etNameUpdateInput.setText(stNameOne);
                    etPhoneUpdateInput.setText(stPhoneOne);

                    btnSaveUpdateInput.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String stNameUpdate = etNameUpdateInput.getText().toString().trim();
                            String stPhoneUpdate = etPhoneUpdateInput.getText().toString().trim();
                            String URL = BASE_URL + "update.php";

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    alertDialog.dismiss();
                                    fetch(); // লিস্ট রিফ্রেশ
                                    new AlertDialog.Builder(MainActivity.this).setMessage("Data updated successfully").show();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    volleyError.printStackTrace();
                                }
                            }) {
                                @Nullable
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> map = new HashMap<>();
                                    map.put("id", stIDOne);
                                    map.put("name", stNameUpdate);
                                    map.put("phone", stPhoneUpdate);
                                    return map;
                                }
                            };
                            requestQueue.add(stringRequest);
                        }
                    });
                }
            });

            // --- রো লেভেল ডেটা ডিলিট লজিক (Delete) ---
            btnDeleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Are you sure to delete?:")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String URL = BASE_URL + "delete.php";

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String s) {
                                            fetch(); // লিস্ট রিফ্রেশ
                                            new AlertDialog.Builder(MainActivity.this).setMessage("Data deleted successfully").show();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            volleyError.printStackTrace();
                                        }
                                    }) {
                                        @Nullable
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String, String> map = new HashMap<>();
                                            map.put("id", stIDOne);
                                            return map;
                                        }
                                    };
                                    requestQueue.add(stringRequest);
                                }
                            }).show();
                }
            });

            return convertView;
        }
    }
}