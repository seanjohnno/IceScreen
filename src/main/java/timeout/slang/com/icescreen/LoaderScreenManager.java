package timeout.slang.com.icescreen;

import android.content.Context;
import android.content.Loader;

/**
 * Simple Loader class used to create and serve ScreenManager
 */
public class LoaderScreenManager extends Loader<ScreenManager> {

    public LoaderScreenManager(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        deliverResult(new ScreenManager());
    }
}
