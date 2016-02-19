package timeout.slang.com.icescreen;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class ScreenManager {

    /*------------------------------------------------------------------------------------------
     * Private Members
     ------------------------------------------------------------------------------------------*/

    /**
     * Current context
     */
    private Activity mContext;

    /**
     * Current view displayed on the screen
     */
    private View mView;

    /**
     * Stack of screens
     */
    private List<Screen> mScreens = new ArrayList<>();

    /**
     * Flag to indicate whether the manager is initialised or not
     */
    private boolean mInitialised;

    /*------------------------------------------------------------------------------------------
     * Public Methods
     ------------------------------------------------------------------------------------------*/

    /**
     * Returns a SafeRef to the View requested. Means it can be safely kept as member data without
     * holding a hard reference to the view tree. The reference re-initialises itself to the new
     * view (with the same ID)
     * @param id
     * @return
     */
    public SafeRef<View> getView(int id) {
        // Throw exception if we're not on main thread
        assertMainThread("getView() - May reference an orphaned view tree if we're not on main");

        return new SafeRefView(id);
    }

    /**
     * Returns a SafeRef to the current Context
     * @return
     */
    public SafeRef<Context> getContext() {
        // Throw exception if we're not on main thread
        assertMainThread("pushScreen()");

        return new SafeRefContext();
    }

    /**
     * @return      True if we don't currently have a context
     */
    public boolean isInConfigChange() {
        return mContext == null;
    }

    /*------------------------------------------------------------------------------------------
     * Public Methods - Screen Ops
     ------------------------------------------------------------------------------------------*/

    /**
     * Add a screen to the stack and display it
     * @param screen
     */
    public void pushScreen(final Screen screen) {
        //Throw exception if we're not on main thread
        assertMainThread("pushScreen() - Not main thread would mean a call to setContentView while we may not have Activity");

        // Grab previous screen (if there is one)
        final Screen prevScreen = peekScreen();

        // Set new screen as current
        mScreens.add(screen);
        screen.setScreenManager(ScreenManager.this);

        // Create a FrameLayout as our root for animating both screens
        FrameLayout animView = new FrameLayout(mContext);

        // Remove current screen from root and add to our animating view
        final View prev = mView;
        if(prev != null) {
            ((ViewGroup) prev.getParent()).removeView(prev);
            animView.addView(prev);
        }

        // Get next screen and add it to animView
        final View next = screen.onCreateView(LayoutInflater.from(mContext));
        animView.addView(mView = next);

        // Set our animation view as the root
        mContext.setContentView(animView);

        Animation pushAnim = screen.getOnPushAnimation();
        pushAnim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                // Remove our new screen view from parent and set it to content view
                ((ViewGroup) next.getParent()).removeView(next);
                mContext.setContentView(next);
            }
        });
        next.startAnimation(pushAnim);
        if(prev != null && prevScreen != null) {
            Animation hiddenAnim = prevScreen.getOnHiddenAnimaton();
            prev.startAnimation(hiddenAnim);
        }
    }

    /**
     * @return      Currently displayed screen (top of stack)
     */
    public Screen peekScreen() {
        // TODO - Assert this as well?7

        return mScreens.isEmpty() ? null : mScreens.get(mScreens.size() - 1);
    }

    /**
     * @return      Remove the current screen and display content of the one underneath
     */
    public Screen popScreen() {
        //Throw exception if we're not on main thread
        assertMainThread("pushScreen() - Not main thread would mean a call to setContentView while we may not have Activity");

        // Grab current screen & view
        final Screen curScreen = mScreens.remove(mScreens.size() - 1);
        final View curView = mView;

        // Get screen underneath and create matching view
        final Screen underneath = peekScreen();
        final View underneathView = underneath != null ? underneath.onCreateView(LayoutInflater.from(mContext)) : null;

        // Create a FrameLayout as our root for animating both screens + add screen that'll be revealed
        FrameLayout animView = new FrameLayout(mContext);
        if(underneathView != null) {
            animView.addView(mView = underneathView);
        }

        // Remove current view and add to animation view
        ((ViewGroup) curView.getParent()).removeView(curView);
        animView.addView(curView);

        // Set our animation view as the root
        mContext.setContentView(animView);

        Animation popAnim = curScreen.getOnPopAnimation();
        popAnim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                // Remove our new screen view from parent and set it to content view
                ((ViewGroup) underneathView.getParent()).removeView(underneathView);
                mContext.setContentView(underneathView);
            }
        });
        curView.startAnimation(popAnim);
        if(underneath != null && underneathView != null) {
            Animation hiddenAnim = underneath.getOnRevealAnimation();
            underneathView.startAnimation(hiddenAnim);
        }

        return curScreen;
    }

    /**
     * @return
     */
    public int getScreenCount() {
        return mScreens.size();
    }

    /*------------------------------------------------------------------------------------------
     * Protected Methods - Called by containing Activity
     ------------------------------------------------------------------------------------------*/

    /**
     * Called from containing activity
     */
    protected void initialise() {
        // Just a flag for now so this is on;y called once
        mInitialised = true;
    }

    /**
     * @return      True if initialised has been called
     */
    protected boolean isInitialised() {
        return mInitialised;
    }

    /**
     * Called by containing class right on onPause
     */
    protected void onPreContextChange() {
        // Remove hard references to context and view tree
        mContext = null;
        mView = null;
    }

    /**
     * Called during onCreate
     * @param context   Created context
     */
    protected void onContextChange(Activity context) {
        mContext = context;

        // Display one underneath if we have one
        setContentView();
    }

    /**
     * Called by containing activity when back pressed. Passes to top screen on stack
     * @return      True if we're consuming back press
     */
    protected boolean onBackPressed() {
        return mScreens.isEmpty() ? false : peekScreen().onBackPressed();
    }

    /*------------------------------------------------------------------------------------------
     * Private Methods
     ------------------------------------------------------------------------------------------*/

    /*
     * Throw exception if we're not on main thread
     */
    private void assertMainThread(String msg) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalAccessError("Call on main thread: " + msg);
        }
    }

    private void setContentView() {
        if(!mScreens.isEmpty()) {
            mContext.setContentView(mView = peekScreen().onCreateView(LayoutInflater.from(mContext)));
        }
    }

    /*------------------------------------------------------------------------------------------
     * Interface: SafeRefView<T>
     *
     * Description:
     * Safe reference to a view, ViewHolder pattern + reinitialised when Context & View hierarchy
     * change
     ------------------------------------------------------------------------------------------*/

    public class SafeRefView implements SafeRef<View> {

        /**
         * WeakReference to context when we saved view
         */
        private WeakReference<Context> mSavedContext;

        /**
         * WeakReference to view
         */
        private WeakReference<View> mViewWeakRef;

        /**
         * ID of our view
         */
        private int mId;

        public SafeRefView(int id) {
            mId = id;
            mViewWeakRef = new WeakReference<View>(mView.findViewById(mId));
            mSavedContext = new WeakReference<Context>(mContext);
        }

        /**
         * @return      Underlying view
         */
        public View get() {
            // Can only grab this on main thread
            assertMainThread("getView() - Non main thread would you mean you could access an orphan view");

            View v  = mViewWeakRef.get();
            if(v == null || mContext != null && mSavedContext.get() != mContext) {
                mViewWeakRef = new WeakReference<View>(v = mView.findViewById(mId));
                mSavedContext = new WeakReference<Context>(mContext);
            }
            return v;
        }
    }

    /*------------------------------------------------------------------------------------------
     * Interface: SafeRefContext<T>
     *
     * Description:
     * Safe reference to current context
     ------------------------------------------------------------------------------------------*/

    public class SafeRefContext implements SafeRef<Context> {

        public Context get() {
            assertMainThread("getContext() - Non main thread would you mean you could access a destroyed Context");
            return mContext;
        }
    }
}
