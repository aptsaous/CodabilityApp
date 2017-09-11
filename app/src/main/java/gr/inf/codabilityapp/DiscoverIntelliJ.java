package gr.inf.codabilityapp;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static gr.inf.codabilityapp.MainScreenActivity.NOT_VALID;
import static gr.inf.codabilityapp.MainScreenActivity.TAG;
import static gr.inf.codabilityapp.MainScreenActivity.mStopListening;

public class DiscoverIntelliJ extends AsyncTask<Void, Void, Void>
{

    static int intelliJPort = NOT_VALID;
    static String intelliJAddr = null;

    static MulticastSocket socket = null;

    @Override
    protected Void doInBackground( Void... params )
    {
        Log.d( TAG, "doInBackground" );

        try
        {
            int MCAST_PORT = 6666;
            String GROUP_ADDR = "224.0.0.3";
            int DGRAM_LEN = 4;

            socket = new MulticastSocket( MCAST_PORT );
            socket.joinGroup( InetAddress.getByName( GROUP_ADDR ) );

            byte buffer[] = new byte[ DGRAM_LEN ];
            DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
            socket.receive( packet );

            intelliJPort = Integer.parseInt( new String( packet.getData() ) );
            intelliJAddr = packet.getAddress().getHostAddress();

            socket.leaveGroup( InetAddress.getByName( GROUP_ADDR ) );
            socket.close();

            Log.d( TAG, "Acquired connection details: " + intelliJAddr + " port: " + intelliJPort );
            mStopListening = false;

        }
        catch ( Exception e )
        {
            Log.d( TAG, "Socket Error: " + e.getMessage() );

            intelliJAddr = null;
            intelliJPort = NOT_VALID;
        }

        return null;
    }
}
