package com.awesome.photo;
import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_recycler_view)
    RecyclerView imageRecyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    ImageListAdapter imageListAdapter;
    List<ImageListModel> imageList = new ArrayList<>();

    LinearLayoutManager linearLayoutManager;
    CustProgressBar custProgressBar;

    DatabaseHelper databaseHelper;

    final String apiURL = "https://api.unsplash.com/photos?per_page=30";
    final String authToken = "Client-ID N_sLL516R-J1Lb91_rkJshfo2wJjznxO5SjtdCwV9Q8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        custProgressBar = new CustProgressBar();
        databaseHelper = new DatabaseHelper(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        imageRecyclerView.setLayoutManager(linearLayoutManager);

        setImageList();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                setImageList();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setImageList()
    {
        if (!checkInternet())
        {
            imageList = databaseHelper.getData();
            imageListAdapter = new ImageListAdapter(MainActivity.this, imageList);
            imageRecyclerView.setAdapter(imageListAdapter);
            imageListAdapter.notifyDataSetChanged();
        }
        else {
            custProgressBar.createProgressBar(MainActivity.this);
            Map<String, String> headers = new HashMap<String, String>();
            // Delete cache
            databaseHelper.deleteData();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, apiURL, new Response.Listener<String>() {
                public void onResponse(String result)
                {
                    custProgressBar.closeProgressBar();

                    if (result != null)
                    {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                try {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    ImageListModel imageListModel = new ImageListModel();
                                    imageListModel.setImageURL(jsonObject.getJSONObject("urls").getString("regular"));
                                    imageListModel.setAuthorName(jsonObject.getJSONObject("user").getString("name"));
                                    imageListModel.setDescription(jsonObject.getString("description"));

                                    imageList.add(imageListModel);
                                    imageListAdapter = new ImageListAdapter(MainActivity.this, imageList);
                                    imageRecyclerView.setAdapter(imageListAdapter);
                                    imageListAdapter.notifyDataSetChanged();

                                    // insert data into local DB
                                    databaseHelper.insertData(downloadImage(imageListModel.getImageURL()), imageListModel.getAuthorName(), imageListModel.getDescription());
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    custProgressBar.closeProgressBar();
                    Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    headers.put("Authorization", authToken);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(stringRequest);
        }
    }

        private boolean checkInternet()
        {
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() == true
                    || connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() == true)
            {
                return true;
            }
            else {
                return false;
            }
        }

        String downloadImage(String imageURL)
        {
            String filePath = null;

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            else
            {
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(imageURL);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle(uri.getPath());
                request.setDescription("Downloading...");
                request.setAllowedOverMetered(true);
                request.setAllowedOverRoaming(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, uri.getPath());
                request.setVisibleInDownloadsUi(false);
                downloadManager.enqueue(request);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),uri.getPath());
                if (file.exists())
                {
                    filePath = file.getAbsolutePath();
                    Log.i("filePath", file.getAbsolutePath());
                }
            }
            return filePath;
        }
}