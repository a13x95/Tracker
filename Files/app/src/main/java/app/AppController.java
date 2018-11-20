package app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue requestQueue;

    private static AppController instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue (Request <T> request, String tag){
        if(TextUtils.isEmpty(tag))
            request.setTag(TAG);
        else
            request.setTag(tag);
        request.setTag(TextUtils.isEmpty(tag));
    }

    public <T> void addToRequestQueue(Request<T> request){
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest( Object tag){
        if(requestQueue != null)
            requestQueue.cancelAll(tag);
    }
}
