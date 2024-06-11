package checkin.zxing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Guilherme on 07-Mar-16.
 */
public class ShowNFCList extends Activity {

    private TableLayout mTagTable;
    private TextView tourComplete;
    private Button btnLinkToLogin;
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    private static String url_tour_tags;

    // JSON Node names
    private String user_level, username,user_id, tour_id, tour_name, password, id_ronda, bid, bid_desc;
    private boolean is_tour_complete;
    private String[][] tag_list;
    private int tal_list_length;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_tour_tags);
        mTagTable = (TableLayout) findViewById(R.id.table_tags);
        tourComplete = (TextView) findViewById(R.id.ronda_alert);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        is_tour_complete = false;
		tag_list = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user_level = extras.getString("Level");
            username = extras.getString("User");
            user_id = extras.getString("user_id");
            password = extras.getString("Pass");
            bid = extras.getString("building");
            bid_desc = extras.getString("building_desc");
            tour_id = extras.getString("tour_id");
            tour_name = extras.getString("tour_name");
            id_ronda = extras.getString("id_ronda");
			
			Object[] objectArray = (Object[]) extras.getSerializable("tag_list");
			if(objectArray!=null){
                tal_list_length = objectArray.length;
				tag_list = new String[objectArray.length][];
				for(int i=0;i<tal_list_length;i++){
					tag_list[i]=(String[]) objectArray[i];
                    //Log.d("Array tag_list ", tag_list[i][0]);
				}
			}
            Toast.makeText(getApplicationContext(), extras.getString("message"), Toast.LENGTH_LONG).show();
        }

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        TableLayout content = mTagTable;
        TextView alert = tourComplete;
        is_tour_complete = true;
        for (int i = 0; i < tal_list_length; i++) {
            TableRow row = new TableRow(ShowNFCList.this);

            TextView tv1 = new TextView(ShowNFCList.this);
            tv1.setTextColor(Color.parseColor("#009688"));
            tv1.setTextSize(14);
            tv1.setText(tag_list[i][1] + "   ");
            row.addView(tv1, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            TextView tv2 = new TextView(ShowNFCList.this);
            tv2.setTextColor(Color.parseColor("#009688"));
            tv2.setTextSize(14);
            tv2.setText(tag_list[i][2] + "   ");
            row.addView(tv2, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            TextView tv3 = new TextView(ShowNFCList.this);
            if (tag_list[i][3].equalsIgnoreCase("true")) {
                tv3.setTextColor(Color.GREEN);
                tv3.setGravity(Gravity.RIGHT);
            } else {
                tv3.setTextColor(Color.RED);
                tv3.setGravity(Gravity.RIGHT);
                is_tour_complete = false;
            }
            tv3.setTextSize(14);
            row.addView(tv3, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            TextView tv4 = new TextView(ShowNFCList.this);
            if (tag_list[i][4].equalsIgnoreCase("sent")) {
                tv4.setTextColor(Color.GREEN);
                tv4.setGravity(Gravity.RIGHT);
            } else {
                tv4.setTextColor(Color.RED);
                tv4.setGravity(Gravity.RIGHT);
            }
            tv4.setTextSize(14);
            row.addView(tv4, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            content.addView(row);

            //separador
            TableRow rowd = new TableRow(ShowNFCList.this);
            TextView divider = new TextView(ShowNFCList.this);
            divider.setTextSize(1);
            divider.setBackgroundColor(Color.parseColor("#A0A0A0"));
            divider.setText("");
            rowd.addView(divider, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            content.addView(rowd);
        }
        if (is_tour_complete == true) {
            alert.setTextColor(Color.GREEN);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onBackPressed (){
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ShowNFCList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://checkin.zxing/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ShowNFCList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://checkin.zxing/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

}
