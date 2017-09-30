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

import static gr.inf.codabilityapp.DiscoverIntelliJ.intelliJAddr;
import static gr.inf.codabilityapp.DiscoverIntelliJ.intelliJPort;
import static gr.inf.codabilityapp.MainScreenActivity.NOT_CONNECTED;
import static gr.inf.codabilityapp.MainScreenActivity.TAG;
import static gr.inf.codabilityapp.MainScreenActivity.isAuthorized;
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

        if ( ( intelliJPort == NOT_CONNECTED ) || ( intelliJAddr == null ) )
        {
            Snackbar.make( mainActivity.findViewById( R.id.include ), "You need to connect with IntelliJ first.\nGo to Settings -> Connect", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();

            mStopListening = true;
            isAuthorized = false;

            return null;
        }

        try
        {
            String cmd = URLEncoder.encode( params[0] );

            if ( isAuthorized )
                url = new URL( "http://" + intelliJAddr + ":" + intelliJPort + "/?cmd=" + cmd );
            else
                url = new URL( "http://" + intelliJAddr + ":" + intelliJPort + "/?auth=" + cmd );

            urlConnection = ( HttpURLConnection ) url.openConnection();

            if ( !isAuthorized && urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK )
            {
                isAuthorized = true;
            }
            else if ( !isAuthorized && urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK )
            {
                Snackbar.make( mainActivity.findViewById( R.id.include ), "Invalid activation code!! Please try again", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();

            }

            InputStreamReader input = new InputStreamReader( urlConnection.getInputStream() );
            int data = input.read();

        }
        catch ( ConnectException e )
        {
            if ( e.getMessage().contains( "ECONNREFUSED" ) )
            {
                Snackbar.make( mainActivity.findViewById( R.id.include ), "Connection refused!! Verify that IntelliJ is running...", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();

                mStopListening = true;
                intelliJPort = NOT_CONNECTED;
                isAuthorized = false;

                Log.d( TAG, "ECONNREFUSED" );

            }
            else
            {
                Log.d( TAG, "ConnectException " + e.getMessage() );
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
