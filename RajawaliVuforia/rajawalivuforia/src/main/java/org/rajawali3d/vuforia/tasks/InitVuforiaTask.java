package org.rajawali3d.vuforia.tasks;

import android.os.AsyncTask;

import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.vuforia.VuforiaController;

public class InitVuforiaTask implements IRajawaliVuforiaTask {
    private VuforiaController mController;

    @Override
    public void execute(VuforiaController controller) {
        mController = controller;
        new Task().execute();
    }

    private class Task extends AsyncTask<Void, Integer, Boolean> {
        private VuforiaController mController;
        private int mProgressValue = -1;

        @Override
        protected Boolean doInBackground(Void... params) {
            synchronized (mController.getShutdownLock()) {
                Vuforia.setInitParameters(mController.getActivity(), mController.getVuforiaFlags());

                do {
                    mProgressValue = Vuforia.init();

                    publishProgress(mProgressValue);
                } while(!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);

                return mProgressValue > 0;
            }
        }

        protected void onProgressUpdate(Integer... values) {
            mController.getVuforiaActivity().onInitVuforiaProgress(values[0]);
        }

        protected void onPostExecute(Boolean result) {
            if(result) {
                mController.taskComplete(InitVuforiaTask.this);
            } else {
                mController.taskFail(InitVuforiaTask.this, "Vuforia initialization failed.");
            }
        }
    }
}