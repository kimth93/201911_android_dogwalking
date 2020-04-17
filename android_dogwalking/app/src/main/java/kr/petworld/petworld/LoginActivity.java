package kr.petworld.petworld;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = "phpquerytest";
    private static final String TAG_JSON="webnautes";
    //private static final String TAG_ID = "id";
    private static final String TAG_ID = "userid";
    private static final String TAG_PASSWORD ="password";


    ArrayList<HashMap<String, String>> mArrayList;
    ListView mListViewList;
    private TextView mTextViewResult;

    String mJsonString;


    private EditText mEditTextId;
    private EditText mEditTextPassword;

    CheckBox autologin;
    Boolean loginChecked;
    public SharedPreferences settings;

    String userID;
    String userPW;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //로그인 체크
        mEditTextId = (EditText)findViewById(R.id.editText_main_userId);
        mEditTextPassword = (EditText)findViewById(R.id.editText_main_password);

        settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        loginChecked = settings.getBoolean("LoginChecked", false);

        //if (loginChecked) {
            //mEditTextId.setText(settings.getString("userid", ""));
            //mEditTextPassword.setText(settings.getString("password", ""));
            //autologin.setChecked(true);
       // }
        //if(!settings.getString("userid", "").equals("")) mEditTextPassword.requestFocus();

        Button submit = findViewById(R.id.login_btn);
        submit.setOnClickListener(view -> {

            GetData task = new GetData();
            task.execute( mEditTextId.getText().toString(), mEditTextPassword.getText().toString());

            /*userID = mEditTextId.getText().toString().trim();
            userPW = mEditTextPassword.getText().toString().trim();*/

            /*if(userID != null && !userID.isEmpty() && userPW != null && !userPW.isEmpty()){
                login(userID, userPW);
            }*/
        });

        Button btnRegisterScreen =  findViewById(R.id.btnRegisterScreen);
        btnRegisterScreen.setOnClickListener(v -> startActivity(new Intent(getApplication(), SignUpActivity.class)));
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;
        String data = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please Wait", null, true, true);
        }





        //값 넘겨주기
        @Override
        protected String doInBackground(String... params) {

            String loginUserid = params[0];
            String loginPassword= params[1];

            String serverURL = "http://192.168.219.117/Login.php";
            String postParameters = "userid=" + loginUserid + "&password=" + loginPassword;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                data = bufferedReader.toString().trim();
                bufferedReader.close();


                return data;


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }



        }

        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            // Log.d(TAG, "response - " + result);

            if (data.equals("0")){
                //mTextViewResult.setText(errorString);
                Toast myToast = Toast.makeText(getApplicationContext(),"잘못 입력했습니다.", Toast.LENGTH_LONG);
                myToast.show();


            }
            else {

                startActivity(new Intent(getApplication(), MainActivity.class));
            }
        }

        /*@Override
        protected void onPostExecute(Void aVoid) {
            if(data.equals("1"))
            {
                Log.e("RESULT","성공적으로 처리되었습니다!");
                alertBuilder
                        .setTitle("알림")
                        .setMessage("성공적으로 등록되었습니다!")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, MainMenu.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
            else if(data.equals("0"))
            {
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                alertBuilder
                        .setTitle("알림")
                        .setMessage("비밀번호가 일치하지 않습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }


        }*/
    }
    /*private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);
                String password = item.getString(TAG_PASSWORD);

                HashMap<String,String> hashMap = new HashMap<>();

                hashMap.put(TAG_ID, id);
                hashMap.put(TAG_PASSWORD, password);

                mArrayList.add(hashMap);
            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }*/


}



