package gr.inf.codabilityapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static gr.inf.codabilityapp.MainScreenActivity.NOT_CONNECTED;
import static gr.inf.codabilityapp.MainScreenActivity.TAG;
import static gr.inf.codabilityapp.MainScreenActivity.isAuthorized;
import static gr.inf.codabilityapp.MainScreenActivity.mStopListening;

public class DiscoverIntelliJ extends AsyncTask<Void, Void, Void>
{

    static int intelliJPort = NOT_CONNECTED;
    static String intelliJAddr = null;

    static MulticastSocket socket = null;

    private PopupWindow mLoadingPopup;
    private Activity mainActivity;

    DiscoverIntelliJ( Activity mainActivity, PopupWindow mLoadingPopup )
    {
        this.mainActivity = mainActivity;
        this.mLoadingPopup = mLoadingPopup;
    }

    @Override
    protected Void doInBackground( Void... params )
    {
        Log.d( TAG, "DiscoverIntelliJ.doInBackground()" );

        try
        {
            int MCAST_PORT = 6666;
            String GROUP_ADDR = "224.0.0.3";
            int DGRAM_LEN = 4;

            /* Join MultiCast Group */
            socket = new MulticastSocket( MCAST_PORT );
            socket.joinGroup( InetAddress.getByName( GROUP_ADDR ) );

            byte buffer[] = new byte[ DGRAM_LEN ];
            DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
            socket.receive( packet );

            /* Acquire IntelliJ's connection details */
            intelliJPort = Integer.parseInt( new String( packet.getData() ) );
            intelliJAddr = packet.getAddress().getHostAddress();

            socket.leaveGroup( InetAddress.getByName( GROUP_ADDR ) );
            socket.close();

            Log.d( TAG, "Acquired connection details: " + intelliJAddr + " port: " + intelliJPort );
            mStopListening = false;

            mainActivity.runOnUiThread( new Runnable() {
                @Override
                public void run()
                {
                    Snackbar.make( mainActivity.findViewById( R.id.include ), "Successfully connected with IntelliJ!", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
                    mLoadingPopup.dismiss();
                    TextView textView = mainActivity.findViewById( R.id.speechResult );
                    textView.setText( R.string.activationMsg );

                }
            });

        }
        catch ( Exception e )
        {
            Log.d( TAG, "Socket Error: " + e.getMessage() );

            intelliJAddr = null;
            intelliJPort = NOT_CONNECTED;
            mStopListening = true;
            isAuthorized = false;

            mainActivity.runOnUiThread( new Runnable() {
                @Override
                public void run()
                {
                    Snackbar.make( mainActivity.findViewById( R.id.include ), "Connection error when trying to connect with IntelliJ\nPlease try again.", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
                    mLoadingPopup.dismiss();
                }
            });
        }

        return null;
    }
}
