package checkin.zxing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import checkin.zxing.record.ParsedNdefRecord;


/**
 * Created by Guilherme on 29-Feb-16.
 */
public class Login extends Activity {
    EditText inputUser;
    EditText inputPass;
    private String username, user_id, password, bid, name;
    private Button btnLogin;
    private ProgressDialog pDialog, pDialog2;
    JSONParser jsonParser = new JSONParser();
    private static String url_login_app, url_get_sites;
    private boolean login_status;

    JSONArray sites = null;
    ArrayList<HashMap<String, String>> siteList;

    //NFC stack
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private View tagRead;
    private LinearLayout mTagContent;
    private String tagToMySQL, login_success_user, login_success_admin, no_sites_found;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        resolveIntent(getIntent());

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mTagContent = (LinearLayout) findViewById(R.id.list);

        //Check NFC reader
        if (mAdapter == null) {
            Toast.makeText(getApplicationContext(), R.string.no_nfc, Toast.LENGTH_LONG).show();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader: OK", Locale.ENGLISH, true)});

        //Check connectivity
        if (!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Toast.makeText(getApplicationContext(), extras.getString("message"), Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.login);
        url_login_app = getResources().getString(R.string.login_php);
        url_get_sites = getResources().getString(R.string.get_sites_php);
        login_success_user = getResources().getString(R.string.login_success_user);
        login_success_admin = getResources().getString(R.string.login_success_admin);
        no_sites_found = getResources().getString(R.string.no_sites_found);

        inputUser = (EditText) findViewById(R.id.username);
        inputPass = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.button_login);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            if (!inputUser.getText().toString().isEmpty() && !inputPass.getText().toString().isEmpty()) {
                if (!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_login, Toast.LENGTH_LONG).show();
                } else {
                    username = inputUser.getText().toString();
                    password = inputPass.getText().toString();
                    new LoginTask().execute();
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.miss_login_field, Toast.LENGTH_LONG).show();
            }
            }
        });
    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
        return;
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            // Unknown tag type
            byte[] empty = new byte[0];
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tag2 = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d("NFC read DEC ", "" + getDec(tag2.getId()));
            byte[] payload = dumpTagData(tag).getBytes();
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
            msgs = new NdefMessage[]{msg};
            //}
            // Setup the views
            buildTagViews(msgs);
        }
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append(getDec(id));
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }


    void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTagContent;

        // Parse the first message in the list
        // Build views for all of the sub records
        Date now = new Date();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            tagRead = record.getView(this, inflater, content, i);
            tagToMySQL = ((TextView) tagRead).getText().toString();
            //content.removeAllViews();
            inputUser.setText(tagToMySQL);
            inputPass.setText("1234");
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }

            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        }else{
            //Toast.makeText(getApplicationContext(), R.string.no_nfc, Toast.LENGTH_LONG).show();
        }
    }

    class LoginTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }
        //@Override
        protected String doInBackground(String... args) {

            Log.d("Create username", username);
            Log.d("Create password", password);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            try {
                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(url_login_app, "GET", params);
                Log.d("Create Response", json.toString());
                // check for success tag
                try {
                    int success = json.getInt("success");
                    int all_sites = json.getInt("all_sites");
                    login_status = true;
                    if(all_sites == 0) {
                        if (success == 1 || success == 3) {
                            Log.d("Create Response", "login sucesso User");
                            Intent intent = new Intent(getApplicationContext(), TagViewer.class);
                            intent.putExtra("Level", "User");
                            intent.putExtra("User", username);
                            intent.putExtra("user_id", json.getString("user_id"));
                            intent.putExtra("Pass", password);
                            intent.putExtra("building", json.getString("site_id"));
                            intent.putExtra("building_desc", json.getString("site_desc"));
                            intent.putExtra("message", "" + login_success_user);
                            startActivity(intent);
                            finish();

                        } else {
                            // failed to create product
                            if (success == 2) {
                                Log.d("Create Response", "login sucesso Admin");
                                // Launch login activity
                                Intent intent = new Intent(getApplicationContext(), TagViewer.class);
                                intent.putExtra("Level", "Admin");
                                intent.putExtra("User", username);
                                intent.putExtra("user_id", json.getString("user_id"));
                                intent.putExtra("Pass", password);
                                intent.putExtra("building", json.getString("site_id"));
                                intent.putExtra("building_desc", json.getString("site_desc"));
                                intent.putExtra("message", "" + login_success_admin);
                                startActivity(intent);
                                finish();
                            } else {
                                login_status = false;
                            }
                        }
                    } else{
                        // failed to create product
                        if (success == 1 || success == 3) {
                            Log.d("Create Response", "login sucesso User");
                            // Launch login activity
                            Intent intent = new Intent(getApplicationContext(), SiteSelect.class);
                            intent.putExtra("Level", "User");
                            intent.putExtra("User", username);
                            intent.putExtra("user_id", json.getString("user_id"));
                            intent.putExtra("Pass", password);
                            intent.putExtra("message", "" + login_success_user);
                            startActivity(intent);
                            finish();
                        }
                        if (success == 2) {
                            Log.d("Create Response", "login sucesso Admin");
                            // Launch login activity
                            Intent intent = new Intent(getApplicationContext(), SiteSelect.class);
                            intent.putExtra("Level", "Admin");
                            intent.putExtra("User", username);
                            intent.putExtra("user_id", json.getString("user_id"));
                            intent.putExtra("Pass", password);
                            intent.putExtra("message", "" + login_success_admin);
                            startActivity(intent);
                            finish();
                        } else {
                            login_status = false;
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
                    if (login_status == false) {
                        Toast.makeText(Login.this.getApplicationContext(), R.string.login_invalid, Toast.LENGTH_LONG).show();
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

