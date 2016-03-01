package locationmanager.com.example;

/**
 * Created by lenovo on 2016/2/29.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
