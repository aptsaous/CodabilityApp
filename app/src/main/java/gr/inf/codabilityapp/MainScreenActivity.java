package gr.inf.codabilityapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static android.speech.SpeechRecognizer.ERROR_CLIENT;
import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
import static gr.inf.codabilityapp.DiscoverIntelliJ.intelliJAddr;
import static gr.inf.codabilityapp.DiscoverIntelliJ.intelliJPort;
import static gr.inf.codabilityapp.DiscoverIntelliJ.socket;

public class MainScreenActivity extends AppCompatActivity
{
    private Context mContext;
    private ConstraintLayout mLayout;
    private PopupWindow mLoadingPopup;
    private PopupWindow mHelpPopup;
    private SpeechRecognizer mSpeechRecognizer;
    private TextView mSpeechResult;
    private ImageButton mMicBtn;
    private Activity mActivity;

    final static String TAG = "<<Codability>>";
    final long SPEECH_TIMER = 1000;
    public static boolean mStopListening = false;
    public static final int NOT_CONNECTED = -1;
    static boolean isAuthorized = false;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_screen );

        /* Customize toolbar */
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        toolbar.setTitleTextColor( Color.BLACK );
        toolbar.setTitle( "Codability" );
        setSupportActionBar( toolbar );

        /* Initialize instance variables */
        mContext = getApplicationContext();
        mLayout = ( ConstraintLayout ) findViewById( R.id.include );
        mSpeechResult = ( TextView ) findViewById( R.id.speechResult );
        mMicBtn = ( ImageButton ) findViewById( R.id.micBtn );
        mActivity = this;

        /* Create a SpeechRecognizer Listener */
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer( this );
        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener( listener );

        Log.d( TAG, "onCreate()" );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate( R.menu.menu_main_screen, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();

        if ( id == R.id.connect )
        {
            connectMenu();
            return true;
        }
        else if ( id == R.id.help )
        {

            helpMenu();
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        if ( intelliJAddr != null )
            menu.findItem( R.id.connect ).setEnabled( false );
        else
            menu.findItem( R.id.connect ).setEnabled( true );


        return true;
    }

    public void connectMenu()
    {
        if ( mHelpPopup != null && mHelpPopup.isShowing() )
            mHelpPopup.dismiss();

        mStopListening = true;

        LayoutInflater inflater = ( LayoutInflater ) mContext.getSystemService( LAYOUT_INFLATER_SERVICE );
        final View customView = inflater.inflate( R.layout.progress_layout, null );

        ProgressBar progressBar = customView.findViewById( R.id.loadingBar );
        progressBar.getIndeterminateDrawable().setColorFilter( Color.RED, android.graphics.PorterDuff.Mode.SRC_IN );

        mLoadingPopup = new PopupWindow( customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        mLoadingPopup.showAtLocation( mLayout, Gravity.CENTER, 0, 0 );

        final DiscoverIntelliJ discoverIntelliJ = new DiscoverIntelliJ( mActivity, mLoadingPopup );
        discoverIntelliJ.execute();

        ImageButton closeButton = customView.findViewById( R.id.closeBtn );
        closeButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view )
            {
                mLoadingPopup.dismiss();
                socket.close();
                Snackbar.make( findViewById( R.id.include ), "Connection canceled by user", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
            }
        } );
    }

    public void helpMenu()
    {
        if ( mLoadingPopup != null && mLoadingPopup.isShowing() )
            mLoadingPopup.dismiss();

        LayoutInflater inflater = ( LayoutInflater ) mContext.getSystemService( LAYOUT_INFLATER_SERVICE );
        View customView = inflater.inflate( R.layout.help_layout, null );

        mHelpPopup = new PopupWindow( customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        mHelpPopup.showAtLocation( mLayout, Gravity.CENTER, 0, 0 );

        TextView textView = customView.findViewById( R.id.helpPage );
        textView.setMovementMethod( new ScrollingMovementMethod() );

        ImageButton closeButton = customView.findViewById( R.id.closeBtn );

        closeButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view )
            {
                mHelpPopup.dismiss();
            }
        } );
    }

    /* Triggered by clicking on the mic icon */
    public void getSpeechInput( View view )
    {
        if ( intelliJPort == NOT_CONNECTED )
        {
            Snackbar.make( findViewById( R.id.include ), "You need to connect with IntelliJ first.\nGo to Settings -> Connect", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();

            return;
        }

        if ( mStopListening )
            return;

        Intent mSpeechRecognizerIntent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
        mSpeechRecognizerIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        mSpeechRecognizerIntent.putExtra( RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName() );

        mSpeechRecognizer.startListening( mSpeechRecognizerIntent );

        Log.d( TAG, "getSpeechInput()" );

    }

    /* Customized RecognitionListener
     * Enables continuous recognition
     * Disables the need of human interaction
     * Sends on each speech result the data to the IntelliJ server */
    private class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onReadyForSpeech( Bundle bundle )
        {

        }

        @Override
        public void onBeginningOfSpeech()
        {

        }

        @Override
        public void onRmsChanged( float v )
        {

        }

        @Override
        public void onBufferReceived( byte[] bytes )
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d( TAG, "onEndOfSpeech()" );

        }

        @Override
        public void onError( int i )
        {
            switch ( i )
            {
                /* In case the speech isn't recognizable or no speech is detected at all */
                case ERROR_SPEECH_TIMEOUT:
                case ERROR_NO_MATCH:
                    Log.d( TAG, "OnError(): " + i );

                    try
                    {
                        Thread.sleep( SPEECH_TIMER );
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace();
                    }

                    mMicBtn.performClick();
                    break;

                case ERROR_RECOGNIZER_BUSY:
                    Log.d( TAG, "OnError(): ERROR_RECOGNIZER_BUSY" );
                    break;
                case ERROR_CLIENT:
                    Log.d( TAG, "OnError(): ERROR_CLIENT" );
                    break;
                default:
                    Log.d( TAG, "OnError(): " + i );
                    break;

            }
        }

        @Override
        public void onResults( Bundle results )
        {
            ArrayList<String> matches = results.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION );

            mSpeechResult.setText( matches.get( 0 ) ); /* Update text view to match the returned speech results */

            /* Send results to the IntelliJ server */
            HttpRequest httpRequest = new HttpRequest( mActivity );
            httpRequest.execute( matches.get( 0 ) );

            mMicBtn.performClick();

            Log.d( TAG, "onResults()" );

        }

        @Override
        public void onPartialResults( Bundle bundle )
        {

        }

        @Override
        public void onEvent( int i, Bundle bundle )
        {

        }
    }
}
