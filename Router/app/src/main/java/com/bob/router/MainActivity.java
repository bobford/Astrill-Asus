package com.bob.router;

/*
        This loads the Astrill VPN firmware onto the Asus RT-AX88U Pro router.  It was developed so that the router
        firmware could be easily updated from a mobile phone when a computer is not available.  The Asus router
        must have Asuswrt-Merlin firmware already loaded.
        This has only been tested on the Asus RT-AX88U Pro router.

        The Astrill firmware can be obtained by logging into the Astrill website with your Astrill username and password:
            https://www.astrill.com/member-zone/log-in
        If you are in China: Our alternative website is https://www.getastr.com
        Scroll down to "Router set-up" and click on "Install now".  The SSH command "eval wget ..." will be in the window.
        Make a copy. It will be pasted into the params.txt file described later.

        Login to router via Chrome, or equivalent, at address 192.168.50.1 with your Asus user name and password.
        The button for the Astrill VPN will be on the left.  Click and the Astrill interface will be brought up.  It will
        be necessary to disconnect VPN to load firmware.
        [TODO]  This description is for updating existing firmware. Need to include first time use instructions!

        Parameters for username, address, password, port, and the "eval wget ..." can be altered in /com.bob.router/files/params.txt.

        Create the params.txt file in /com.bob.router/files/ similar to:

            //   parameters for Asus RT-AX88U Pro with Astrill VPN
            user = admin            // Asus username
            password = admin
            host = 192.168.50.1
            port = 22
            loadAstrill = eval `wget -q -O - http://astroutercn.com/router/install/blahblahblah'

        where "admin" is the default value for the Asus router and should be replaced by your own values.
        The "loadAstrill" parameter is obtained by logging into your Astrill account at: https://www.astrill.com/member-zone/log-in
        and downloading the String for VPN on a router.  This will be: eval `wget -q -O - http://astroutercn.com/router/install/blahblahblah'
        Do not place a comment (//) on the "loadAstrill" line as even a cursory examination of the line above will make the reason clear!

        This app uses the library, JSch, to connect via ssh.  The jar file can be found at: http://www.jcraft.com/jsch/

        Thanks for correct instructions for opening the channel to:
        https://www.tabnine.com/code/java/methods/com.jcraft.jsch.Session/openChannel


        An early example of the Logcat output when VPN was not disabled:
2023-07-04 20:19:12.673 21257-21343/com.bob.router D/bob: channel connect
2023-07-04 20:19:14.024 21257-21343/com.bob.router D/bob: read = 18
2023-07-04 20:19:14.027 21257-21343/com.bob.router D/bob: 000000:  46 65 74 63 68 69 6e 67 20 66 69 6c 65 73 2e 2e   |  Fetching files..
    000016:  2e 20                                             |  .
2023-07-04 20:19:15.215 21257-21343/com.bob.router D/bob: read = 6
2023-07-04 20:19:15.220 21257-21343/com.bob.router D/bob: 000000:  64 6f 6e 65 2e 0a                                 |  done.
2023-07-04 20:19:15.220 21257-21343/com.bob.router D/bob: read = 188
2023-07-04 20:19:15.256 21257-21343/com.bob.router D/bob: 000000:  59 6f 75 20 61 72 65 20 61 6c 72 65 61 64 79 20   |  You are already
    000016:  63 6f 6e 6e 65 63 74 65 64 20 74 6f 20 41 73 74   |  connected to Ast
    000032:  72 69 6c 6c 20 56 50 4e 2c 20 70 65 72 68 61 70   |  rill VPN, perhap
    000048:  73 20 6f 6e 20 79 6f 75 72 20 72 6f 75 74 65 72   |  s on your router
    000064:  20 6f 72 20 56 50 4e 20 69 73 20 72 75 6e 6e 69   |   or VPN is runni
    000080:  6e 67 20 69 6e 20 62 61 63 6b 67 72 6f 75 6e 64   |  ng in background
    000096:  20 6f 6e 20 74 68 69 73 20 64 65 76 69 63 65 2e   |   on this device.
    000112:  20 50 6c 65 61 73 65 20 64 69 73 63 6f 6e 6e 65   |   Please disconne
    000128:  63 74 20 56 50 4e 20 62 65 66 6f 72 65 20 79 6f   |  ct VPN before yo
    000144:  75 20 63 61 6e 20 70 72 6f 63 65 65 64 20 6f 72   |  u can proceed or
    000160:  20 74 72 79 20 74 6f 20 72 65 62 6f 6f 74 20 79   |   try to reboot y
    000176:  6f 75 72 20 64 65 76 69 63 65 2e 0a               |  our device.
2023-07-04 20:19:15.256 21257-21343/com.bob.router D/bob: 0
2023-07-04 20:19:15.256 21257-21343/com.bob.router D/bob: channel complete, bytes read = 212

 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "bob";

    private static final String     PARAMETER_FILE_NAME     = "params.txt";     // to change user, password or insert applet info

    public static final String LOAD_MSG = "eval `wget -q -O - http://astroutercn.com/router/install/2399393/11bae093bb0a4ebdf559f9c1ae6d6609`";

    private TextView tv;
    public Context context;
    private SharedPreferences sharedPreferences;
    private ProgressBar spinner;
    private AppendLog ap = null;
    public static final String EMPTY_STRING = "";
    public static String PACKAGE_NAME;

//      The following five parameters can be set or changed in the params.txt file.
    String user;
    String password;
    String host;
    int port;
    String loadAstrill;

    private static final String IPV4_REGEX =
            "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";          // https://www.techiedelight.com/validate-ip-address-java/

    //      This section for permissions        * * * * * * * * * * * * * * * * * * * * * * * *
    private boolean ready = false;                                                      // *

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };
    //      This section for permissions        * * * * * * * * * * * * * * * * * * * * * * * *

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ready = getAllPermissionsNeeded();

    }


    @Override
    public void onResume() {
        super.onResume();

        if (ready) {
            initialize();
            process();
        } else {
            Log.d(TAG, "sleeping");
        }
    }

    private void process() {

        doSomeTaskAsync();
    }

    public boolean transferAstrillAppletViaSSH() {
        int ptr = 0;
        JSch jsch = new JSch();                                             // Secure Shell Protocol
        try {
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            try {
                channel.setCommand(loadAstrill);
                channel.setInputStream(null);
                channel.setErrStream(System.err);
                InputStreamReader stream = new InputStreamReader( channel.getInputStream() );
                channel.connect();
                char[] buffer = new char[4096];

                int count;
                while ( ( count = stream.read( buffer, 0, buffer.length ) ) > 0 ) {
                    byte[] buf = new byte[count];
                    for (int i=0; i<count; i++) {
                        buf[i] = (byte)(0xff & buffer[i]);
                    }
                    String hex = formatHexDump(buf, 0, count);
                    ap.appendLog(hex);
                    tv.append(convertAscii(buf) + "\n");
                    ptr += count;
                }
                stream.close();
            } catch (NullPointerException | IOException e){
                Log.d(TAG, "Exception: " + e);
                tv.append(getResources().getString(R.string.err_7));
                ap.appendLog("exception thrown: " + e);
                ap.appendLog(getResources().getString(R.string.err_7));
                return false;
            }
            channel.disconnect();
            Log.d(TAG, "exit status = " + channel.getExitStatus());
            ap.appendLog("exit status = " + channel.getExitStatus());
            Log.d(TAG, "channel complete, bytes read = " + ptr);
            session.disconnect();
            return true;
        } catch (com.jcraft.jsch.JSchException e ) {
            Log.d(TAG, "exception thrown: " + e);
            tv.append(getResources().getString(R.string.err_2));
            ap.appendLog("exception thrown: " + e);
            ap.appendLog(getResources().getString(R.string.err_2));
            return false;
        }
    }

    public static String formatHexDump(byte[] array, int offset, int length) {
        //      https://gist.github.com/jen20/906db194bd97c14d91df
        final int width = 16;

        StringBuilder builder = new StringBuilder();

        for (int rowOffset = offset; rowOffset < offset + length; rowOffset += width) {
            builder.append(String.format(Locale.US, "%06d:  ", rowOffset));

            for (int index = 0; index < width; index++) {
                if (rowOffset + index < array.length) {
                    builder.append(String.format("%02x ", array[rowOffset + index]));
                } else {
                    builder.append("   ");
                }
            }

            if (rowOffset < array.length) {
                int asciiWidth = Math.min(width, array.length - rowOffset);
                builder.append("  |  ");
                builder.append(new String(array, rowOffset, asciiWidth, StandardCharsets.UTF_8).replaceAll("\r\n", " ").replaceAll("\n", " "));
            }

            builder.append(String.format("%n"));
        }

        return builder.toString();
    }

    private boolean isAscii(byte[] b, int n) {
        for (int i=0; i<n; i++) {
            if (((int) b[i] & 0xff) < 32 || ((int) b[i] & 0xff) > 126) {        // byte in java is signed: high bit set of b is therefore < 32
                return false;                                                   // non-printable byte
            }
        }
        return true;
    }

    private String convertAscii(byte[] b) {
        //      this converts byte data to printable ascii
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            if (((int) value & 0xff) < 32 || ((int) value & 0xff) > 126) {          // byte in java is signed: high bit set of b is therefore < 32
                sb.append('.');                                                     // non-printable byte
            } else {
                sb.append((char) (value & 0xFF));
            }
        }
        return sb.toString();
    }

    private void initialize() {
        tv = findViewById(R.id.tv);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setTextIsSelectable(true);
        tv.setText(getResources().getString(R.string.instructions));
        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.INVISIBLE);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        context = getApplicationContext();
        setupSharedPreferences(context);

        File parameters;
        try {
            File dir = getExternalFilesDir(null);                          // local directory: com.bob.simple/files/
            parameters = new File(dir, PARAMETER_FILE_NAME);
            if (!parameters.exists()) {                                         // if it exists, don't touch it!
                Log.d(TAG, "parameters file does not exists");
                if (!parameters.createNewFile()) {                              // creates empty file if it does not exists
                    Log.d(TAG, "unable to make empty parameter file");
                    tv.append(getResources().getString(R.string.err_5));
                }
            }
        } catch (NullPointerException | IOException e) {
            tv.append(getResources().getString(R.string.err_4));
            Log.d(TAG, "exception thrown " + e);
        }

        ap = new AppendLog(context, Objects.requireNonNull(getExternalFilesDir(null)).getPath());
        ap.initLog("The cast of characters\n");

        setDefaultParams();
        List<String> list = getWatermarkParams();
        processParamsInList(list);
        Log.d(TAG, user + "   " + host + "   " + password + "   " + port);
        Log.d(TAG, loadAstrill);
/*
        Log.d(TAG, getArchitecture());                          // this is the ABI
        Log.d(TAG, System.getProperty("os.arch"));              // this is the architecture
        Log.d(TAG, Build.MODEL);
*/
    }

    private void setDefaultParams() {
        user = getSecureItem("user");
        password = getSecureItem("password");
        host = "192.168.50.1";
        port = 22;
        loadAstrill = getSecureItem("astrill");
    }

    private void processParamsInList(List<String> list) {
        if (list == null) return;
        for (String item : list) {
            String[] params = item.split("=");
            if (params.length > 1) {

                if ("user".equals(params[0].trim())) {
                    user = params[1].trim();
                }
                if ("host".equals(params[0].trim())) {
                    String host = params[1].trim();
                    Log.d(TAG, host);
                    Pattern p = Pattern.compile(IPV4_REGEX);               // 192.168.50.1
                    Matcher m = p.matcher(host);
                    if (!m.matches()) {
                        tv.append(getResources().getString(R.string.err_6));
                    } else {
                        this.host = host;
                    }
                }
                if ("password".equals(params[0].trim())) {
                    password = params[1].trim();
                }
                if ("loadAstrill".equals(params[0].trim())) {
                    loadAstrill = params[1].trim();
                }
                if ("port".equals(params[0].trim())) {
                    try {
                        port = Integer.parseInt(params[1].trim());
                    } catch (NumberFormatException e) {
                        tv.append(getResources().getString(R.string.err_4));
                    }
                }
            }
        }
    }

    public void setupSharedPreferences(Context context) {

//          https://stackoverflow.com/questions/62498977/how-to-create-masterkey-after-masterkeys-deprecated-in-android
//          This puts Bob's stuff in Encrypted Shared Preferences.
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PACKAGE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (IOException | GeneralSecurityException e) {
            Log.d(TAG, "Exception getting masterKey: " + e);
            tv.setText(getResources().getString(R.string.err_3));
            return;
        }
        String key1 = "user";                                            // Asus user name
        String key2 = "password";                                        // Asus password
        String key3 = "astrill";                                         // from Astrill web site, after login

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String data1 = Objects.requireNonNull(sharedPreferences.getString(key1, EMPTY_STRING)).trim();
        String data2 = Objects.requireNonNull(sharedPreferences.getString(key2, EMPTY_STRING)).trim();
        String data3 = Objects.requireNonNull(sharedPreferences.getString(key3, EMPTY_STRING)).trim();

//      * * * * * * * remove the comment from the following line to force initialization * * * * * * *
//        data1 = EMPTY_STRING;
//        data2 = EMPTY_STRING;
//        data3 = EMPTY_STRING;

        if (Objects.equals(data1, EMPTY_STRING)) {
            data1 = "admin";                                      // if first time through, fill in user name; admin is default
//        	first time through, write router username value
            Log.d(TAG, "first time through");
            editor.putString(key1, data1);
            // Commit the edits!
            editor.apply();
        }

        if (Objects.equals(data2, EMPTY_STRING)) {
            data2 = "admin";                                      // if first time through, fill in password; admin is default
//        	first time through, write router password value
            Log.d(TAG, "first time through");
            editor.putString(key2, data2);
            // Commit the edits!
            editor.apply();
        }

        if (Objects.equals(data3, EMPTY_STRING)) {
            data3 = LOAD_MSG;                                      // if first time through, fill in load Astrill, like this!
//        	first time through, write Secure Socket Shell value from Astrill website
            Log.d(TAG, "first time through");
            editor.putString(key3, data3);
            // Commit the edits!
            editor.apply();
        }
    }
    
    private String getSecureItem(String item) {
        String key = sharedPreferences.getString(item, EMPTY_STRING);
        return key;
    }


    private void postProcess(boolean ret) {
        Log.d(TAG, "status = " + ret);
        if (ret) {
            tv.append(getResources().getString(R.string.info_1));
            ap.appendLog(getResources().getString(R.string.info_1));
        } else {
            tv.append(getResources().getString(R.string.err_1));
            ap.appendLog(getResources().getString(R.string.err_1));
        }
    }

    private void doSomeTaskAsync() {
        Activity currActivity = MainActivity.this;
        spinner.setVisibility(View.VISIBLE);
        new BackgroundTask(currActivity) {

            String allDone, error = "";
            boolean status;
            @Override
            public void doInBackground() {

                //put your background code here

                try (Socket socket = new Socket(host, port)) {
                    status = transferAstrillAppletViaSSH();
                } catch (IOException e) {
                    status = false;
                    error = getResources().getString(R.string.err_8);
                }

                allDone = "Thank you for your attention.\n";

            }

            @Override
            public void onPostExecute() {
                spinner.setVisibility(View.INVISIBLE);

                if (!error.equals("")) {
                    tv.append(error);
                    ap.appendLog(error);
                }
                tv.append(allDone);
                postProcess(status);
            }
        }.execute();
    }


    public List<String> getWatermarkParams() {
        /*
            Format of the parameter file is:
            String  =  value
            where "String" is the name of the parameter and "value" is interpreted as String or int, as appropriate, separated by an = character
            e.g.:
            user = Bactrian
            host = 192.168.50.1
            password = whatever
            loadAstrill = eval `wget -q -O - http://astroutercn.com/router/install/2399393/11bae093bb0a4ebdf559f9c1ae6d6609`

            comments (//) are ignored
            comment not permitted on the "loadAstrill" line because of the http: entry
         */
        List<String> list = new ArrayList<>();

        File parameters;
        try {
            File dir = getExternalFilesDir(null);
            parameters = new File(dir, PARAMETER_FILE_NAME);
        } catch (NullPointerException e) {
            tv.append(getResources().getString(R.string.err_4));
            return null;
        }
        if (parameters.exists()) {                     // got it
            // read it
            try {
                BufferedReader br = new BufferedReader(new FileReader(parameters));
                String line;
                boolean empty = true;
                while ((line = br.readLine()) != null) {
                    empty = false;
                    if (line.indexOf("//") != 0 && line.length() != 0) {
                        if (line.contains("http://") || line.contains("https://")) {           // no comment permitted on this type of line
                            list.add(line);
                        } else {
                            String[] data = line.split("//");                            // get rid of inline comment
                            list.add(data[0]);
                        }
                    }
                }
                if (empty) {
                    Log.d(TAG, "parameter file not initialized with required parameters, using defaults");
                    ap.appendLog(getResources().getString(R.string.err_9));
                    tv.append(getResources().getString(R.string.err_9));
                    return null;
                }
            } catch (IOException e) {
                Log.d(TAG, "impossible error");
                tv.append(getResources().getString(R.string.err_4));
                return null;
            }
        } else {
            Log.d(TAG, "parameter file missing, using defaults");
            return null;
        }

        return list;
    }

    private String getArchitecture() {
        return Build.SUPPORTED_64_BIT_ABIS[0];
    }


//      This section for permissions        * * * * * * * * * * * * * * * * * * * * * * * * start

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "failed permission " + permission);
                    return false;       // at least one permission failed
                }
            }
        }
        Log.d(TAG, "has all permissions");
        return true;
    }

    final private int PERMISSION_ALL = 1;

    private boolean getAllPermissionsNeeded() {
        if (!hasPermissions(this, PERMISSIONS)) {
            Log.d(TAG, "requesting permissions");
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "request code = " + requestCode + "   " + grantResults.length);
        boolean all = true;
        if (grantResults.length > 0 && requestCode == PERMISSION_ALL) {
            for (int i=0; i<grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission not granted for " + permissions[i] + "   " + i);
                    all = false;
                } else {
                    Log.d(TAG, "permissions being granted now " + all + "   " + i);
                }
            }
            ready = all;
        }
    }
//      This section for permissions        * * * * * * * * * * * * * * * * * * * * * * * * end

}