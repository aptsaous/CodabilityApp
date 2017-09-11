package gr.inf.codabilityapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static gr.inf.codabilityapp.DiscoverIntelliJ.intelliJPort;
import static gr.inf.codabilityapp.MainScreenActivity.NOT_VALID;
import static gr.inf.codabilityapp.MainScreenActivity.TAG;
import static gr.inf.codabilityapp.MainScreenActivity.mStopListening;


class HttpRequest extends AsyncTask<String, Void, Void>
{
    private Activity mainActivity;

    HttpRequest( Activity mainActivity )
    {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground( String... params )
    {
        URL url;
        HttpURLConnection urlConnection = null;

        try
        {
            String cmd = URLEncoder.encode( params[0] );
//            url = new URL( "http://" + intelliJAddr + ":" + intelliJPort + "/?cmd=" + cmd );
            url = new URL( "http://192.168.0.131:8888" );
            urlConnection = ( HttpURLConnection ) url.openConnection();

            InputStreamReader input = new InputStreamReader( urlConnection.getInputStream() );

            int data = input.read();

//            while (data != -1) {
//                char current = (char) data;
//                data = isw.read();
//                System.out.print(current);
//            }

        }
        catch ( ConnectException e )
        {
            if ( e.getMessage().contains( "ECONNREFUSED" ) )
            {
                Snackbar.make( mainActivity.findViewById( R.id.include ), "Connection refused!! Verify that IntelliJ is running...", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();

                Log.d( TAG, "ECONNREFUSED" );

                mStopListening = true;
                intelliJPort = NOT_VALID;

            }


        }
        catch ( Exception e )
        {
            e.printStackTrace();
            Log.d( TAG, e.getMessage() );

        }
        finally
        {
            if ( urlConnection != null )
                urlConnection.disconnect();
        }

        return null;
    }
}
