package gr.inf.codabilityapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainScreenActivity extends AppCompatActivity
{

    private Context mContext;
    private ConstraintLayout mLayout;
    private PopupWindow mLoadingPopup;
    private PopupWindow mHelpPopup;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_screen );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        toolbar.setTitleTextColor( Color.BLACK );
        toolbar.setTitle( "Codability" );
        setSupportActionBar( toolbar );

        mContext = getApplicationContext();

        mLayout = ( ConstraintLayout ) findViewById( R.id.include );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main_screen, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();

        if ( id == R.id.connect )
        {
            if ( mHelpPopup != null && mHelpPopup.isShowing() )
                mHelpPopup.dismiss();

            LayoutInflater inflater = ( LayoutInflater ) mContext.getSystemService( LAYOUT_INFLATER_SERVICE );
            final View customView = inflater.inflate( R.layout.progress_layout, null );

            ProgressBar progressBar = customView.findViewById( R.id.loadingBar );
            progressBar.getIndeterminateDrawable().setColorFilter( Color.RED, android.graphics.PorterDuff.Mode.SRC_IN );

            mLoadingPopup = new PopupWindow( customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );

            ImageButton closeButton = customView.findViewById( R.id.closeBtn );

            closeButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View view )
                {
                    mLoadingPopup.dismiss();

                    Snackbar.make( findViewById( R.id.include ), "Connection canceled", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
                }
            } );

            mLoadingPopup.showAtLocation( mLayout, Gravity.CENTER, 0, 0 );

            return true;
        }
        else if ( id == R.id.help )
        {
            if ( mLoadingPopup != null && mLoadingPopup.isShowing() )
                mLoadingPopup.dismiss();

            LayoutInflater inflater = ( LayoutInflater ) mContext.getSystemService( LAYOUT_INFLATER_SERVICE );
            View customView = inflater.inflate( R.layout.help_layout, null );

            mHelpPopup = new PopupWindow( customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );

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

            mHelpPopup.showAtLocation( mLayout, Gravity.CENTER, 0, 0 );

            return true;
        }

        return super.onOptionsItemSelected( item );
    }
}
