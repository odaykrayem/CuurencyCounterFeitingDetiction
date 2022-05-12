package com.mrmindteam.cuurencycounterfeitingdetiction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private Button takePicBtn,sendToDBBtn;
    private TextView resultTV;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int EXTERNAL_STORAGE_CODE = 101;
    File finalFile;

    ProgressDialog pDialog;
    Timer getResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        takePicBtn = findViewById(R.id.btn_take_pic);
        sendToDBBtn = findViewById(R.id.btn_send);
        resultTV = findViewById(R.id.result);

        takePicBtn.setEnabled(true);
        sendToDBBtn.setEnabled(true);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        // And From your main() method or any other method
        takePicBtn.setOnClickListener(v->{
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
            else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_CODE);
            }
            else
            {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        sendToDBBtn.setOnClickListener(v->{
            if(finalFile != null){
                sendToDB();

            }else{
                Toast.makeText(this, "Please take a picture first" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == EXTERNAL_STORAGE_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "external storage permission granted", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "external storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            Uri tempUri = getImageUri(getApplicationContext(), photo);
            // CALL THIS METHOD TO GET THE ACTUAL PATH
            finalFile = new File(getRealPathFromURI(tempUri));
            Toast.makeText(this, "Image saved to device successfully", Toast.LENGTH_SHORT).show();
            takePicBtn.setEnabled(false);
            System.out.println(tempUri);

        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private void sendToDB(){
        pDialog.show();
        sendToDBBtn.setEnabled(false);
        String url = "http://192.168.43.130/CurrenciesAPI/saveImage.php";
        AndroidNetworking.upload(url)
                .addMultipartFile("fileToUpload", finalFile)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("resp", response.toString());
                            //converting response to json object
                            JSONObject obj = response;
                            String status = obj.getString("status");
                            String message = obj.getString("message");
                            if(status.equals("success")){
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                ImageModel model = new ImageModel(
                                        Integer.parseInt(obj.getString("id")),
                                obj.getString("path")

                                );
                                resultTV.setText("");
                                getResult = new Timer();
                                getResult.schedule(new GetResult(), 1000, 5000);
                                SharedPrefManager.getInstance(MainActivity.this).setImageId(model.getId());
                                Log.e("image", model.toString());
                            }else{
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            pDialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("catch", e.getMessage());
                            pDialog.dismiss();
                            sendToDBBtn.setEnabled(true);
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        sendToDBBtn.setEnabled(true);
                        Log.e("uploadErr", anError.getErrorDetail());
                        Log.e("uploadErr", anError.getMessage());
                        Log.e("uploadErr", anError.getLocalizedMessage());
                        Log.e("uploadErr", anError.getErrorBody().toString());
                    }
                });
    }

    private void getResult(){
        String url = "http://192.168.43.130/CurrenciesAPI/getResult.php";
        String imageId = String.valueOf(SharedPrefManager.getInstance(MainActivity.this).getImageId());
        AndroidNetworking.post(url)
                .addBodyParameter("image_id", imageId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("resp", response.toString());
                            //converting response to json object
                            JSONObject obj = response;
                            String status = obj.getString("status");
                            String message = obj.getString("message");
                            if(status.equals("success")){
                                String result = obj.getString("result");
                                resultTV.setText(result);
                                sendToDBBtn.setEnabled(true);
                                takePicBtn.setEnabled(true);
                                getResult.cancel();
                                finalFile = null;
                                SharedPrefManager.getInstance(MainActivity.this).deleteImageId();
                            }
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("catch", e.getMessage());
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        sendToDBBtn.setEnabled(true);
                        Log.e("uploadErr", anError.getErrorDetail());
                        Log.e("uploadErr", anError.getMessage());
                        Log.e("uploadErr", anError.getLocalizedMessage());
                        Log.e("uploadErr", anError.getErrorBody().toString());
                    }
                });

    }

    class GetResult extends TimerTask {
        public void run() {
            if(SharedPrefManager.getInstance(MainActivity.this).getImageId() != -1){
                getResult();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getResult.cancel();
    }
}