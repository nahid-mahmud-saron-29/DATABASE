📱 Android CRUD Application with PHP-MySQL Backend
এটি একটি সম্পূর্ণ অ্যান্ড্রয়েড ক্রুড (CRUD - Create, Read, Update, Delete) অ্যাপ্লিকেশন। এখানে ফ্রন্টএন্ড হিসেবে Java এবং নেটওয়ার্কিংয়ের জন্য Volley Library ব্যবহার করা হয়েছে। ব্যাকএন্ডে PHP স্ক্রিপ্ট এবং ডেটাবেজ হিসেবে MySQL ব্যবহার করা হয়েছে।

🚀 মূল ফিচারসমূহ (Key Features)
Create: অ্যাপ থেকে ডেটাবেজে সরাসরি নতুন ইউজার যুক্ত করা।

Read (Fetch): ডেটাবেজের সব ডেটা রিয়েল-টাইমে ListView-তে প্রদর্শন করা।

Update & Delete: কাস্টম অ্যাডাপ্টারের মাধ্যমে নির্দিষ্ট রোর ডেটা আপডেট এবং ডিলিট করা।

Search: নামের ওপর ভিত্তি করে লাইভ ডেটা ফিল্টারিং বা সার্চ করা।

Crash-Proof Design: convertView রিসাইক্লিং, optString() ডিফেন্স এবং মেমোরি লিক প্রতিরোধের মাধ্যমে অ্যাপের পারফরম্যান্স একদম স্টেবল করা হয়েছে।




```JAVA
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



```PHP
Connect.php
<?php
    $con = mysqli_connect("localhost:3307","root","","api_eleven_database");
    if(!$con){
        die("Connection Failed".mysqli_connect_error());
    }
?>

insert.php
<?php
    require_once "connect.php";

    $id = $_POST['id']??'';
    $name = $_POST['name']??'';
    $phone = $_POST['phone']??'';

    $sql = "INSERT INTO api_eleven_table(id,name,phone) VALUES('$id','$name','$phone')";

    $res = mysqli_query($con,$sql);
?>

fetch.php
<?php
    require_once "connect.php";
    header("Content-Type:application/Json");

    $sql = "SELECT * FROM api_eleven_table";

    $res = mysqli_query($con,$sql);

    $data = array();
    while($row = mysqli_fetch_assoc($res)){
        array_push($data,$row);
    }
    echo json_encode($data);
?>

update.php
<?php
    require_once "connect.php";

    $id = $_POST['id']??'';
    $name = $_POST['name']??'';
    $phone = $_POST['phone']??'';

    $sql = "UPDATE api_eleven_table 
        SET id='$id', name='$name', phone='$phone' 
        WHERE id='$id'";


    $res = mysqli_query($con,$sql);
?>


delete.php
<?php
    require_once "connect.php";

    $id = $_POST['id']??'';

    $sql = "DELETE FROM api_eleven_table WHERE id='$id'";

    $res = mysqli_query($con,$sql);
?>

search.php
<?php
header("Content-Type: application/json; charset=utf-8");
require_once "connect.php";

$name = $_POST['name'];

$sql = "SELECT * FROM api_eleven_table WHERE name LIKE '%$name%'";
$result = mysqli_query($con, $sql);
$data = array();

foreach($result as $item){
 $userInfo = array();
 $userInfo['id'] = $item['id'];
 $userInfo['name'] = $item['name'];
 $userInfo['phone'] = $item['phone'];
 array_push($data, $userInfo);
}
echo json_encode($data, JSON_PRETTY_PRINT);
?>


activity.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:orientation="vertical"
    android:background="#FFFFFF"
    >
    <EditText
        android:id="@+id/etNameUserInput"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_margin="10dp"
        android:padding="10dp"
        android:hint="Enter your name"
        android:textColor="#000000"
        />
    <EditText
        android:id="@+id/etPhoneUserInput"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_margin="10dp"
        android:padding="10dp"
        android:hint="Enter your phone"
        android:textColor="#000000"
        />
    <Button
        android:id="@+id/btnInsertUserInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Insert"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        />
    <EditText
        android:id="@+id/etSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search Name"/>
    <Button
        android:id="@+id/btnSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Search"/>
    <ListView
        android:id="@+id/listView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_margin="10dp"
        />
</LinearLayout>




layout.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF"
    android:orientation="vertical"
    >
    <TextView
        android:id="@+id/tvIDLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="ID: "
        android:textSize="20sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_margin="10dp"
        />
    <TextView
        android:id="@+id/tvNameLayout"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:textSize="20sp"
        android:textStyle="bold"
        android:padding="10dp"
        />
    <TextView
        android:id="@+id/tvPhoneLayout"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:textSize="20sp"
        android:textStyle="bold"
        android:padding="10dp"
        />
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        >
        <Button
            android:id="@+id/btnUpdateLayout"
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:text="UPDATE"
            android:layout_weight="1"
            android:layout_margin="10dp"
            />
        <Button
            android:id="@+id/btnDeleteLayout"
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:text="DELETE"
            android:layout_weight="1"
            android:layout_margin="10dp"
            />
    </LinearLayout>

</LinearLayout>



layout2.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    >
    <EditText
        android:id="@+id/etNameUpdateInput"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_margin="10dp"
        android:padding="10dp"
        android:hint="Enter your name"
        android:textColor="#000000"
        />
    <EditText
        android:id="@+id/etPhoneUpdateInput"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_margin="10dp"
        android:padding="10dp"
        android:hint="Enter your phone"
        android:textColor="#000000"
        />
    <Button
        android:id="@+id/btnSaveUpdateInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Save"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        />
</LinearLayout>


