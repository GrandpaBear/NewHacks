package com.ramenbois.newhacks.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.hardware.camera2.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.ramenbois.newhacks.myapplication.Model.Ingredient;
import com.ramenbois.newhacks.myapplication.Model.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainMenuActivity extends AppCompatActivity {

    private ImageButton mOpenCamera;
    private ImageButton mOpenRecipes;
    private String jsonURL; // TODO: get url for json data stuff
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private RecyclerView recView;
    private List<Recipe> recipes ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        mOpenCamera = findViewById(R.id.openCamera);
        addCameraButton();
//        addRecipesButton();

        recipes = new ArrayList<>() ;
        recView = findViewById(R.id.recyclerviewid);
        jsonRequest();
    }

    private void addCameraButton() {
        mOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = MainMenuActivity.this;
                boolean checkCamera = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
                if (!checkCamera) {
                    Toast.makeText(MainMenuActivity.this, "This device does not have a camera!", Toast.LENGTH_LONG).show();
                } else {
                    // TODO: pull up camera screen
                    //Intent intent = new Intent("MediaStore.ACTION_IMAGE_CAPTURE");
                    //startActivityForResult(intent,1046);
                    dispatchTakePictureIntent();

                }
            }
        });
    }
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void addRecipesButton() {
        mOpenRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: what to do when recipe opens
            }
        });
    }

    private void jsonRequest() {
        request = new JsonArrayRequest(jsonURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObj = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObj = response.getJSONObject(i);

                        JSONArray jsonArray = response.getJSONArray(i);
                        ArrayList<Ingredient> ingredients = new ArrayList<>();
                        for (int j = 0; j < jsonArray.length(); j++) {
                            String str = new Gson().toJson(jsonArray.getJSONObject(j));
                            ingredients.add(new Ingredient(str)); // TODO: FIX THIS
                        }
                        String imageURL = jsonObj.getString("imageURL");
                        String sourceURL = jsonObj.getString("sourceURL");
                        String f2fURL = jsonObj.getString("f2fURL");
                        String title = jsonObj.getString("title");
                        String publisher = jsonObj.getString("publisher");
                        String publisherURL = jsonObj.getString("publisherURL");
                        int socialRank = jsonObj.getInt("socialRank");
                        float rating = BigDecimal.valueOf(jsonObj.getDouble("rating")).floatValue();
                        recipes.add(new Recipe(ingredients, imageURL, sourceURL, f2fURL,
                                title, publisher, publisherURL, socialRank, rating));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                setUpRecyclerView(recipes);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue = Volley.newRequestQueue(MainMenuActivity.this);
        requestQueue.add(request);

    }

    private void setUpRecyclerView(List<Recipe> recipes) {
        RecyclerViewAdapter recViewAdapater = new RecyclerViewAdapter(this, recipes);
        recView.setLayoutManager(new LinearLayoutManager(this));

        recView.setAdapter(recViewAdapater);

    }

}
