package com.lack006.busybox;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.lack006.busybox.Consistent.TIME_OUT;

/**
 * Android Hosts-L V8
 * Created by lack006 on 2018/2/20.
 */

public class Download {

    private Context mContext;
    private String mUrl;
    private String mFilePath;

    public void download(Context context, String url, String filePath) {

        mContext = context;
        mUrl = url;
        mFilePath = filePath;
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute();


    }

    private class DownloadTask extends AsyncTask<Object, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(Object... objects) {


            File file = mContext.getCacheDir();
            final String CACHE_PATH = file.getAbsolutePath();
            URL url;
            long fileSize;
            long downloadSize = 0;
            try {

                url = new URL(mUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(TIME_OUT);
                con.setRequestProperty("Accept-Encoding", "identity");
                InputStream in = con.getInputStream();
                File fileOut = new File(CACHE_PATH + Consistent.HOSTS_FILE);
                FileOutputStream out = new FileOutputStream(fileOut);
                byte[] bytes = new byte[4096];
                int len;
                fileSize = con.getContentLength();
                publishProgress(Consistent.CONNECTED);
                while ((len = in.read(bytes)) != -1) {
                    if (mStopFlag) {
                        con.disconnect();
                        break;
                    }
                    out.write(bytes, 0, len);
                    downloadSize += len;
                    publishProgress((int) (downloadSize * 100 / fileSize));

                }
                in.close();
                out.close();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (!mStopFlag) {
                if (mHosts != null) {
                    InsertClashTaskHelper insertClashTaskHelper = new InsertClashTaskHelper();
                    insertClashTaskHelper.insertClashHelper(mContext, mHosts, mCheck, mCheckBox);
                } else {

                    ApplyHostsTaskHelper applyHostsTaskHelper = new ApplyHostsTaskHelper();
                    applyHostsTaskHelper.applyHostsHelper(mContext, mCheckBox);
                }
                mProgressDialog.cancel();

            } else {
                CleanCacheHelper cleanCacheHelper = new CleanCacheHelper();
                cleanCacheHelper.cleanHosts(mContext);
            }


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == CONNECTED) {
                mProgressDialog.setMessage(mContext.getString(R.string.downloading));
            } else if (values[0] == DOWNLOAD_CANCEL) {
                mProgressDialog.cancel();
            } else {
                mProgressDialog.setProgress(values[0]);
            }
        }
    }
}
