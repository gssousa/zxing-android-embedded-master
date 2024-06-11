
package checkin.zxing;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;



/**
 * Created by Guilherme on 29-Feb-16.
 */
public class ViewNFC extends Activity  {
    private Button btnLinkToLogin;
    private TextView inputNFCData,inputNFCTag;
    private ImageView imageNFCTag;
    private String nfc_data,nfc_tag;
    private ProgressDialog pDialog;
    private JSONParser jsonParser = new JSONParser();
    private static String url_view_nfc, read_checkpoint;
    private String user_level,username,user_id,password,bid,bid_desc;
    private JSONArray nfc_data_array;
    private Integer resultado;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_nfc);
        url_view_nfc = getResources().getString(R.string.nfcview_php);
        read_checkpoint = getResources().getString(R.string.read_checkpoint);
        inputNFCData = (TextView) findViewById(R.id.nfcdata);
        inputNFCTag  = (TextView)findViewById(R.id.nfc_tag);
        imageNFCTag  = (ImageView)findViewById(R.id.nfcimage);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nfc_tag = extras.getString("leituraNFC");
            user_level = extras.getString("Level");
            username = extras.getString("User");
            user_id = extras.getString("user_id");
            password = extras.getString("Pass");
            bid = extras.getString("building");
            bid_desc = extras.getString("building_desc");
            inputNFCTag.setText(read_checkpoint+nfc_tag);
        }
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TagViewer.class);
                intent.putExtra("NFCregister","sucesso");
                intent.putExtra("Level", user_level);
                intent.putExtra("User", username);
                intent.putExtra("user_id", user_id);
                intent.putExtra("Pass", password);
                intent.putExtra("building", bid);
                intent.putExtra("building_desc", bid_desc);
                intent.putExtra("message","");
                startActivity(intent);
                finish();
            }
        });

        //Check connectivity
        if (!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        new ViewTagNFC().execute();

    }

    @Override
    public void onStart(){
        super.onStart();
        inputNFCTag.setText(nfc_tag);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void onBackPressed (){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), TagViewer.class);
        intent.putExtra("NFCregister","sucesso");
        intent.putExtra("Level", user_level);
        intent.putExtra("User", username);
        intent.putExtra("user_id", user_id);
        intent.putExtra("Pass", password);
        intent.putExtra("building",bid);
        intent.putExtra("building_desc", bid_desc);
        intent.putExtra("message","");
        startActivity(intent);
        finish();
    }

    class ViewTagNFC extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewNFC.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }
        //@Override
        protected String doInBackground(String... args) {
            Log.d("View nfc_tag", nfc_tag);
            List < NameValuePair > params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nfc_tag", nfc_tag));
            params.add(new BasicNameValuePair("edificio", bid));
            try {
                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(url_view_nfc, "GET", params);
                Log.d("Create Response", json.toString());
                // check for success tag
                try {
                    resultado = json.getInt("success");
                    if (resultado == 1) {
                        nfc_data="";
                        nfc_data_array = json.getJSONArray("data");
                    } else {
                        if (resultado == 2) {
                            nfc_data = "Ponto não registado.";
                        }else{
                            nfc_data = "Falha na leitura.";
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "ok";
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        inputNFCData.setText("");
                        if (resultado == 1) {
                            for (int i = 0; i < nfc_data_array.length(); i++) {
                                JSONObject innerObj = nfc_data_array.getJSONObject(i);

                                //decode base64 string to image
                                String imageString = innerObj.getString("foto");
                                byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                imageNFCTag.setImageBitmap(decodedImage);

                                inputNFCData.append("Nome" + ": " + innerObj.get("nome") + "\n");
                                inputNFCData.append("Empresa/Função" + ": " + innerObj.get("funcao") + "\n");
                                inputNFCData.append("Validade" + ": " + innerObj.get("validade_data_fim") + "\n");

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}

