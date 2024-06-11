package checkin.zxing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import checkin.zxing.record.ParsedNdefRecord;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device just discovered.
 */
public class TagViewer extends Activity implements LocationListener {

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private static String url_create_product, url_finish_tours, url_ocorrencias, url_register_tag, url_checkout_docs, logout_done, patrol_tags_list,
            portal_name, site_logon, all_rights;
    // flag for GPS status
    public boolean isGPSEnabled = false;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    //ligação a servidor PHP
    JSONParser jsonParser = new JSONParser();
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    //Location Object
    Location location;
    private LinearLayout mTagContent;
    private Button registerNFCtag, viewNFCtag, showCard, checkinCard, checkoutCard, logout, tour_end, qr_code;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private View tagRead;
    private String tagToMySQL;
    private String user_level, username, user_id, resultado_picagem, password, bid, bid_desc;
    private String url, imei, check_action, response;
    private ProgressDialog pDialog;
    private TextView nfc_desc_t, tour, ocorrenciasBtn, site_name;
    private boolean nfcMessage;
    private TelephonyManager telephonyManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.v("isGPSEnabled", "=" + isGPSEnabled);
            /*
            if(isGPSEnabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d("Coordinate","Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude());
            }*/


            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                try {
                    Log.d("Coordinate", "Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude());
                } catch (NullPointerException e) {
                    Log.d("Coordinate", "No Location");
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();

        url_ocorrencias = getResources().getString(R.string.ocorrencias_portal);
        url_create_product = getResources().getString(R.string.nfcread_php);
        url_register_tag = getResources().getString(R.string.nfcregister_php);
        url_checkout_docs = getResources().getString(R.string.checkoutdocs);
        logout_done = getResources().getString(R.string.logout_done);
        portal_name = getResources().getString(R.string.portal_name);
        site_logon = getResources().getString(R.string.footer_1);
        all_rights = getResources().getString(R.string.footer_2);
        check_action = "";

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        nfcMessage = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user_level = extras.getString("Level");
            username = extras.getString("User");
            user_id = extras.getString("user_id");
            password = extras.getString("Pass");
            bid = extras.getString("building");
            bid_desc = extras.getString("building_desc");
            Toast.makeText(getApplicationContext(), extras.getString("message"), Toast.LENGTH_LONG).show();
        }

        url = url_ocorrencias + "?user_name=" + username + "&psword=" + password + "&edificio=" + bid;


        Log.d("User Level", "User");

        setContentView(R.layout.tag_viewer_user);
        site_name = (TextView) findViewById(R.id.site_name_tag);
        nfc_desc_t = (TextView) findViewById(R.id.nfc_desc);
        Log.d("bid_desc ", "" + bid_desc);
        site_name.setText(site_logon + " " + bid_desc + "\n" + all_rights);
        Log.d("url portal ", url);

        qr_code = (Button) findViewById(R.id.qr_code);
        qr_code.setTypeface(font);

        checkinCard = (Button) findViewById(R.id.main_checkin);
        checkinCard.setTypeface(font);
        checkinCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagToMySQL.isEmpty() && tagToMySQL.matches("[0-9]+")) {
                    check_action = "checkin";

                    AlertDialog.Builder builder = new AlertDialog.Builder(TagViewer.this);

                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SendTagReading().execute();
                        }
                    });
                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.setMessage("Confirma realizar Checkin?");
                    builder.setTitle("Atenção");

                    AlertDialog d = builder.create();
                    d.show();

                } else {
                    Toast.makeText(getApplicationContext(), R.string.need_tag_read, Toast.LENGTH_LONG).show();
                }
            }
        });
        checkinCard.setVisibility(View.GONE);


        checkoutCard = (Button) findViewById(R.id.main_checkout);
        checkoutCard.setTypeface(font);
        checkoutCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagToMySQL.isEmpty() && tagToMySQL.matches("[0-9]+")) {
                    check_action = "checkout";

                    AlertDialog.Builder builder = new AlertDialog.Builder(TagViewer.this);

                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SendTagReading().execute();
                        }
                    });
                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.setMessage("Confirma realizar Checkout?");
                    builder.setTitle("Atenção");

                    AlertDialog d = builder.create();
                    d.show();

                } else {
                    Toast.makeText(getApplicationContext(), R.string.need_tag_read, Toast.LENGTH_LONG).show();
                }
            }
        });
        checkoutCard.setVisibility(View.GONE);

        showCard = (Button) findViewById(R.id.main_check);
        showCard.setTypeface(font);
        showCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagToMySQL.isEmpty()) {
                    //Check connectivity
                    if (!isNetworkAvailable(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), R.string.no_net_tag_details, Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(getApplicationContext(), ViewNFC.class);
                        i.putExtra("leituraNFC", tagToMySQL);
                        i.putExtra("Level", user_level);
                        i.putExtra("User", username);
                        i.putExtra("user_id", user_id);
                        i.putExtra("Pass", password);
                        i.putExtra("building", bid);
                        i.putExtra("building_desc", bid_desc);
                        startActivity(i);
                        finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.need_tag_read, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Register Button Click event
        registerNFCtag = (Button) findViewById(R.id.main_register);
        registerNFCtag.setTypeface(font);
        registerNFCtag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagToMySQL.isEmpty() && tagToMySQL.matches("[0-9]+")) {
                    if (!isNetworkAvailable(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), R.string.no_net_tag_details, Toast.LENGTH_LONG).show();
                    } else {
                        new RegisterNewTag().execute();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.need_tag_read, Toast.LENGTH_LONG).show();
                }
            }
        });
        if (user_level.equalsIgnoreCase("User")) {
            registerNFCtag.setVisibility(View.GONE);
        }


        ocorrenciasBtn = (TextView) findViewById(R.id.main_ocorrencias);
        ocorrenciasBtn.setTypeface(font);
        ocorrenciasBtn.setMovementMethod(LinkMovementMethod.getInstance());
        ocorrenciasBtn.setText(Html.fromHtml("<a href=\"" + url + "\">Portal<br>Ocorrências</a>"));
        ocorrenciasBtn.setLinkTextColor(Color.WHITE);

        logout = (Button) findViewById(R.id.main_logout);
        logout.setTypeface(font);
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), R.string.no_logout_net, Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TagViewer.this);
                    builder.setMessage(R.string.logout_confirmation)
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    intent.putExtra("message", logout_done);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    builder.show();
                }
            }
        });

        mTagContent = (LinearLayout) findViewById(R.id.list);
        resolveIntent(getIntent());
        tagToMySQL = new String("");

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        //Check NFC reader
        if (mAdapter == null) {
            //Toast.makeText(getApplicationContext(), R.string.no_nfc, Toast.LENGTH_LONG).show();
        }
        //Check connectivity
        if (!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader: OK", Locale.ENGLISH, true)});

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("Coordinate", "Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void scanCustomScanner(View view) {
        new IntentIntegrator(this).setOrientationLocked(false).setCaptureActivity(CustomScannerActivity.class).initiateScan();
    }

    public void scanBarcode(View view) {
        new IntentIntegrator((Activity) this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, R.string.reading_error, Toast.LENGTH_LONG).show();
            } else {
                LinearLayout content = mTagContent;
                content.removeAllViews();
                Date now = new Date();
                TextView timeView = new TextView(this);
                TextView qrcode = new TextView(this);
                timeView.setTextColor(Color.parseColor("#878585"));
                timeView.setText(TIME_FORMAT.format(now));
                tagToMySQL = result.getContents();

                if (tagToMySQL.matches("[0-9]+")) {

                    Log.d("QR Lido", tagToMySQL);

                    content.addView(timeView, 0);
                    qrcode.setTextColor(Color.parseColor("#009688"));
                    qrcode.setText(tagToMySQL);
                    content.addView(qrcode, 1);
                    if (!isNetworkAvailable(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), R.string.no_send_tag_net, Toast.LENGTH_LONG).show();
                    } else {
                        check_action = "";
                        new SendTagReading().execute();
                    }
                } else {
                    Toast.makeText(TagViewer.this, R.string.not_numeric, Toast.LENGTH_LONG).show();
                }
            }

        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (!user_level.equalsIgnoreCase("User")) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.putExtra("message", logout_done);
            startActivity(intent);
            finish();
        }
        if (user_level.equalsIgnoreCase("User")) {
            if (!isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), R.string.no_logout_net, Toast.LENGTH_LONG).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(TagViewer.this);
                builder.setMessage(R.string.logout_confirmation)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                intent.putExtra("message", logout_done);
                                startActivity(intent);
                                finish();
                            }
                        });
                builder.show();
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                //showWirelessSettingsDialog();
            }

            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        } else {
            //Toast.makeText(getApplicationContext(), R.string.no_nfc, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }

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
        content.removeAllViews();
        Date now = new Date();
        TextView timeView = new TextView(this);
        timeView.setTextColor(Color.parseColor("#878585"));
        timeView.setText(TIME_FORMAT.format(now));

        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            content.addView(timeView, 0);
            ParsedNdefRecord record = records.get(i);
            tagRead = record.getView(this, inflater, content, i);
            tagToMySQL = ((TextView) tagRead).getText().toString();
            if (tagToMySQL.matches("[0-9]+")) {
                content.addView(tagRead, 1 + i);
                if (!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), R.string.no_send_tag_net, Toast.LENGTH_LONG).show();
                } else {
                    check_action = "";
                    new SendTagReading().execute();
                }
            } else {
                Toast.makeText(TagViewer.this, R.string.not_numeric, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("TagViewer Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    class SendTagReading extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TagViewer.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", user_id));
            params.add(new BasicNameValuePair("tag_id", tagToMySQL));
            params.add(new BasicNameValuePair("edificio", bid));
            params.add(new BasicNameValuePair("imei", imei));
            params.add(new BasicNameValuePair("check_action", check_action));
            //params.add(new BasicNameValuePair("gps_lat", ""+location.getLatitude()));
            //params.add(new BasicNameValuePair("gps_log", ""+location.getLongitude()));
            Log.d("to create_nfc_reading.php ", params.toString());
            try {
                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json3 = jsonParser.makeHttpRequest(url_create_product, "GET", params);
                Log.d("create_nfc_reading.php ", json3.toString());
                // check for success tag
                try {
                    String success = json3.getString("success");
                    Log.d("create_nfc_reading.php result ", success);
                    resultado_picagem = success;
                    response = json3.getString("message");
                    return success;
                } catch (JSONException e) {
                    Log.d("create_nfc_reading.php failure ", "Failed");
                    e.printStackTrace();
                    return "fail";
                }
            } catch (Exception e) {
                Log.d("create_nfc_reading.php failure ", "Failed");
                e.printStackTrace();
                return "fail";
            }
        }

        protected void onPostExecute(String resultado) {
            // dismiss the dialog once done
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("create_nfc_reading.php result_picagem ", resultado_picagem + ' ' + response);
                    int resultado = Integer.parseInt(resultado_picagem);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TagViewer.this);
                    LayoutInflater inflater = (TagViewer.this).getLayoutInflater();
                    View alertView = inflater.inflate(R.layout.layout_message, null);
                    TextView portal = (TextView) alertView.findViewById(R.id.main_message);
                    portal.setMovementMethod(LinkMovementMethod.getInstance());
                    final AlertDialog show;
                    Button back = (Button) alertView.findViewById(R.id.back);

                    switch (resultado) {
                        case 1:
                            checkinCard.setVisibility(View.VISIBLE);
                            checkoutCard.setVisibility(View.GONE);
                            nfc_desc_t.setText(R.string.checkpoint_success);
                            nfc_desc_t.setTextColor(Color.GREEN);
                            Toast.makeText(TagViewer.this, R.string.checkpoint_success, Toast.LENGTH_LONG).show();
                            break;

                        case 4:
                            checkinCard.setVisibility(View.GONE);
                            checkoutCard.setVisibility(View.VISIBLE);
                            nfc_desc_t.setText(R.string.tag_checkin_success);
                            nfc_desc_t.setTextColor(Color.GREEN);
                            Toast.makeText(TagViewer.this, R.string.tag_checkin_success, Toast.LENGTH_LONG).show();
                            break;
                        case 5:
                            checkinCard.setVisibility(View.VISIBLE);
                            checkoutCard.setVisibility(View.GONE);
                            nfc_desc_t.setText(R.string.tag_checkout_success);
                            nfc_desc_t.setTextColor(Color.GREEN);
                            Toast.makeText(TagViewer.this, R.string.tag_checkout_success, Toast.LENGTH_LONG).show();
                            break;
                        case 6:
                            checkinCard.setVisibility(View.GONE);
                            checkoutCard.setVisibility(View.VISIBLE);
                            nfc_desc_t.setText(R.string.checkpoint_success);
                            nfc_desc_t.setTextColor(Color.GREEN);
                            Toast.makeText(TagViewer.this, R.string.checkpoint_success, Toast.LENGTH_LONG).show();
                            break;
                        case 7:
                            checkinCard.setVisibility(View.VISIBLE);
                            checkoutCard.setVisibility(View.GONE);
                            nfc_desc_t.setText(R.string.checkpoint_success);
                            nfc_desc_t.setTextColor(Color.GREEN);
                            Toast.makeText(TagViewer.this, R.string.checkpoint_success, Toast.LENGTH_LONG).show();
                            break;
                        default:
                    }
                    if (resultado >= 400) {
                        checkinCard.setVisibility(View.GONE);
                        checkoutCard.setVisibility(View.GONE);
                        nfc_desc_t.setText(response);
                        nfc_desc_t.setTextColor(Color.RED);
                        portal.setText(response);
                        portal.setTextColor(Color.RED);
                        alertDialog.setView(alertView);
                        show = alertDialog.show();
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                show.dismiss();
                            }
                        });
                    }
                }
            });
        }
    }

    class RegisterNewTag extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TagViewer.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", user_id));
            params.add(new BasicNameValuePair("tag_id", tagToMySQL));
            params.add(new BasicNameValuePair("edificio", bid));
            params.add(new BasicNameValuePair("imei", imei));
            Log.d("to register_nfc.php ", params.toString());
            try {
                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json4 = jsonParser.makeHttpRequest(url_register_tag, "GET", params);
                Log.d("create_nfc_reading.php ", json4.toString());
                // check for success tag
                try {
                    String success = json4.getString("success");
                    String message = json4.getString("message");
                    Log.d("create_nfc_reading.php result ", message);
                    resultado_picagem = success;
                    return success;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "fail";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "fail";
            }
        }

        protected void onPostExecute(String resultado) {
            // dismiss the dialog once done
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TagViewer.this);
                    LayoutInflater inflater = (TagViewer.this).getLayoutInflater();
                    View alertView = inflater.inflate(R.layout.layout_message, null);
                    TextView portal = (TextView) alertView.findViewById(R.id.main_message);
                    portal.setMovementMethod(LinkMovementMethod.getInstance());
                    final AlertDialog show;
                    Button back = (Button) alertView.findViewById(R.id.back);
                    Log.d("create_nfc_reading.php result_picagem ", resultado_picagem);
                    if (resultado_picagem.equalsIgnoreCase("1")) {
                        portal.setText(R.string.tag_registration_success);
                        portal.setTextColor(Color.GREEN);
                        alertDialog.setView(alertView);
                        show = alertDialog.show();
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                show.dismiss();
                            }
                        });
                    } else {
                        portal.setText(R.string.no_tag_registered);
                        portal.setTextColor(Color.RED);
                        alertDialog.setView(alertView);
                        show = alertDialog.show();
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                show.dismiss();
                            }
                        });
                    }
                }
            });
        }
    }
}