package timeout.slang.com.icescreen;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;

/**
 * Container activity to hold the ScreenManager
 */
public abstract class ScreenActivity extends Activity {

    /*------------------------------------------------------------------------------------------
     * Constants
     ------------------------------------------------------------------------------------------*/

    /**
     * Used to init and access the loader that contains the ScreenManager
     */
    public static final int LOADER_SCREEN_MANAGER = "screen_manager".hashCode();

    /*------------------------------------------------------------------------------------------
     * Private Members
     ------------------------------------------------------------------------------------------*/

    /**
     * Reference to the screen manager, re-assigned each time an instance of this Activity is created
     */
    private ScreenManager mScreenManager;

    /*------------------------------------------------------------------------------------------
     * From AppCompatActivity
     ------------------------------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create or get loader
        getLoaderManager().initLoader(LOADER_SCREEN_MANAGER, null, new LoaderManager.LoaderCallbacks<ScreenManager>() {

            @Override
            public Loader<ScreenManager> onCreateLoader(int id, Bundle args) {
                return createLoader();
            }

            @Override
            public void onLoadFinished(Loader<ScreenManager> loader, ScreenManager manager) {
                setScreenManager(manager);
            }

            @Override
            public void onLoaderReset(Loader<ScreenManager> loader) {
            }
        });
    }

    /**
     * Capture back press
     */
    @Override
    public void onBackPressed() {
        // See if ScreenManager/Screen wants to intercept, if not...
        if(!mScreenManager.onBackPressed()) {
            // Pop a screen, if count goes to 0 then pass to super to exit Activity
            if (mScreenManager.popScreen() == null || mScreenManager.getScreenCount() == 0) {
                super.onBackPressed();
            }
        }
    }

    /**
     * Called by framework when Activity is about to be destroyed, inform screen manager
     */
    @Override
    protected void onPause() {
        mScreenManager.onPreContextChange();
        super.onPause();
    }

    /*------------------------------------------------------------------------------------------
     * Protected Methods
     ------------------------------------------------------------------------------------------*/

    protected Loader<ScreenManager> createLoader() {
        return new LoaderScreenManager(this);
    }

    protected void setScreenManager(ScreenManager screenMan) {
        mScreenManager = screenMan;

        // Pass this context onto screen manager
        mScreenManager.onContextChange(this);

        // Initialise the ScreenManager (if it hasn't already been)
        if(!mScreenManager.isInitialised()) {
            onInitialiseScreenManager(mScreenManager);
            mScreenManager.initialise();
        }
    }

    /*------------------------------------------------------------------------------------------
     * Abstract Methods
     ------------------------------------------------------------------------------------------*/

    /**
     * Call made to extending subclass to tell it to populate ScreenManager with Screen (it's only
     * called once) not each time an instance is created
     * @param screenMan
     */
    protected abstract void onInitialiseScreenManager(ScreenManager screenMan);
}
