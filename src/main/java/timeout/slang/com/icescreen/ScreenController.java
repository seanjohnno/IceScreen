package timeout.slang.com.icescreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public abstract class ScreenController {

    /*------------------------------------------------------------------------------------------
     * Constants
     ------------------------------------------------------------------------------------------*/

    private static final int ANIM_DURATION = 500;

    /*------------------------------------------------------------------------------------------
     * Private Members
     ------------------------------------------------------------------------------------------*/

    /**
     * Reference to the containing screen manager
     */
    private ScreenManager mScreenManager;

    /*------------------------------------------------------------------------------------------
     * Public Methods
     ------------------------------------------------------------------------------------------*/

    public ScreenManager getScreenManager() {
        return mScreenManager;
    }

    /*------------------------------------------------------------------------------------------
     * Protected Methods - Convenience
     ------------------------------------------------------------------------------------------*/

    /**
     * An object which can be assigned without risk of retaining a handle to the view / view tree.
     * It'll re-initialised itself to a newly created view of the same ID
     * @param id    Id of the view you want to grab
     * @return      The safe reference to a view
     */
    protected SafeRef<View> getView(int id) {
        return mScreenManager.getView(id);
    }

    /**
     * An object which can be assigned without risk of retaining a handle to the Context. It'll
     * re-initialised itself to a newly created Context
     * @return      The safe reference to the Context
     */
    protected SafeRef<Context> getContext() {
        return mScreenManager.getContext();
    }

    /**
     * Called after onCreateView - setConfiguration
     */
    protected void onSetActive() { }

    /*------------------------------------------------------------------------------------------
     * Protected Methods - Called by ScreenManager
     ------------------------------------------------------------------------------------------*/

    /**
     * Called when the containing activity receives a back button press
     * @return True if the screen wants to intercept to prevent from being popped
     */
    protected boolean onBackPressed() {
        return false;
    }

    /**
     * Called when the screen is added to the stack
     * @param screenManager
     */
    protected void setScreenManager(ScreenManager screenManager) {
        mScreenManager = screenManager;
    }

    protected Animation getOnPushAnimation() {
        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(ANIM_DURATION);
        return fadeIn;
    }

    protected Animation getOnPopAnimation() {
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(ANIM_DURATION);
        return fadeOut;
    }

    protected Animation getOnHiddenAnimaton() {
        return getOnPopAnimation();
    }

    protected Animation getOnRevealAnimation() {
        return getOnPushAnimation();
    }

    /*------------------------------------------------------------------------------------------
     * Abstract Methods
     ------------------------------------------------------------------------------------------*/

    /**
     * Called by containing Activity to create the view. Don't keep a reference to this in your
     * screens!!
     * @param inflater      Inflater to use to create
     * @return              Created view
     */
    protected abstract View onCreateView(LayoutInflater inflater);
}
