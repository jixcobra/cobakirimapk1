package com.example.cobakirimapk1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private static final int READ_EXTERNAL_DATA = 24;
    private static final int PERMISSION_REQUEST_CODE = 100;

    Button btBrowse;
    Button btSend;
    TextView textView;
    String ip;
    int port;
    Thread Thread1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInit();
        clickBrowse();
    }

    private void setInit() {
        btBrowse = findViewById(R.id.btBrowse);
        btSend = findViewById(R.id.btSend);
        textView = findViewById(R.id.tv);
        ip = "192.168.1.10";
        port = 7777;
        Thread1 = new Thread(new Thread1());
        Thread1.start();
    }

    private void clickBrowse() {
        btBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (chekPermissions()) {
                        performFileSearch();
                    } else {
                        reqPermissions();
                    }
                }
            }
        });

    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, READ_EXTERNAL_DATA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_EXTERNAL_DATA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = uri.getPath();
                    int sub = path.indexOf(":");
                    String simplePath = path.substring(sub + 1);
                    Toast.makeText(this, "" + simplePath, Toast.LENGTH_SHORT).show();
                    clickSend(simplePath);
                }
            }
        }
    }


    /*Socket Handler*/

    private void clickSend(String simplePath) {
        final String path = simplePath;
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!path.isEmpty()) {
                    new Thread(new Thread3(path)).start();
                }
            }
        });

    }

    private DataOutputStream output;
    private PrintWriter outlen;
    private BufferedReader input;

    class Thread1 implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(ip, port);
                outlen = new PrintWriter(socket.getOutputStream());
                output = new DataOutputStream(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Connected\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.append("server: " + message + "\n");
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        File file;
        //        private String path;
        Thread3(String path) {
//            this.path = path;
            file = new File(path);
        }
        @Override
        public void run() {
            long lenfile =0;
            lenfile = file.length();
            byte[] myBuffer = new byte[(int)lenfile];
            int bytesRead = 0;
//            long totaleng =0;
            InputStream in = null;
            BufferedInputStream bis;
            try {
                in = new FileInputStream(file);
                String len = String.valueOf(lenfile);
                Log.w("length",""+lenfile);
                outlen.write(len);
                outlen.flush();
                bis = new BufferedInputStream(in);
                bis.read(myBuffer, 0, myBuffer.length);
                

                output.write(myBuffer,0,myBuffer.length);
                output.flush();
                Log.e("mybuffer length",""+myBuffer.length);
                Log.e("mybuffer",""+myBuffer);

//                while ((bytesRead = in.read(myBuffer, 0, myBuffer.length)) != -1)
////                while (in.read(myBuffer)>0)
//                {
//                    output.write(myBuffer,0,myBuffer.length);
//                    Log.w("buytesRead",""+bytesRead);
//                    Log.w("mybuffer length",""+myBuffer.length);
//                    Log.w("mybuffer",""+myBuffer);
//                }
                outlen.close();
                output.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.append("client: " + file + "\n");
//                    etMessage.setText("");
                }
            });
        }
    }

    /*Permission Handler*/

    private boolean chekPermissions() {
        int result = ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void reqPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            Toast.makeText(MainActivity.this, "Read External Storage permission allows us to read files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted !");
            } else {
                Log.e("value", "Permission Denied !");
            }
        }
    }
}
