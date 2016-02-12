package com.example.air.pianoprism;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.os.Environment;
import android.widget.TextView;

import com.example.air.pianoprism.examples.PianoRollExample;
import com.example.air.pianoprism.examples.PianoRollExampleDoubles;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {

    public final String LOG_TAG = "Piano Prism";
    public Button recordButton;
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    private TextView txtView = null;


    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int bufferSize = 2048;
    private int sampleNumBits = 16;
    private int numChannels = 1;

    private boolean isRecording = false;
    private AudioTrack audioPlayer;
    private  AudioRecord recorder;

    public static final int ARROW_MESSAGES = 0;
    private double res = samplingRate/(bufferSize);


    boolean needZeropadding = false;

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    UpdateUIThread uuit = null;

   /* Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            Bundle containerForInfoToSend = msg.getData();
            String message = containerForInfoToSend.getString("msg_s");
            StringTokenizer st = new StringTokenizer(message);
            String mes = st.nextToken();
            TextView v = (TextView) findViewById(R.id.txtField);
            v.setText(message);

        }
    };*/



    int lightgreen ;//= this.getResources().getColor(R.color.lightgreen);
    int blue ;//== this.getResources().getColor(R.color.blue);
    int lightblue ;//== this.getResources().getColor(R.color.lightblue);
    int green ;//== this.getResources().getColor(R.color.green);
    int yellow ;//== this.getResources().getColor(R.color.yellow);
    int lightyellow ;//== this.getResources().getColor(R.color.lightyellow);
    int orange;
    int lightorange ;//== this.getResources().getColor(R.color.lightorange);
    int red;
    int darkred;
    int cyan;


    Handler handle = new Handler() {
        public void handleMessage(Message msg) {

            /**************************************************************************************************************/


        /*    Bundle containerForInfoToSend = msg.getData();

            fft_arr = containerForInfoToSend.getDoubleArray("data");

            //Log.d("handle", arrayToString(fft_arr));
            for (int i = 0; i < bufferSize; i++) {
                dp_arr[i].setX(i*res);
                dp_arr[i].setY(fft_arr[i]);
            }
            series.resetData(dp_arr);*/

            //updateUImethod();


            Bundle b = msg.getData();

            received_arr = b.getDoubleArray("data");

            new CalculateColorsTask().execute();


        }
    };


///////////////////////////////////////////////////////////////
    GraphView graph = null;
    LineGraphSeries<DataPointFaster> series;

    DataPointFaster[] dp_arr;
    double[] fft_arr;

    UpdateGraphTask ugt = null;


    GridView gridView;
    String[] out_array;

    double[] received_arr;

    static final int column_number = 10;
    ArrayAdapter<String> adapter;
    ArrayList<String> cells;

//////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        isRecording = true;

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.mp4";

        setContentView(R.layout.activity_main);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.RelativeLayout);

        setContentView(rl);

        mRecordButton = new RecordButton(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        rl.addView(mRecordButton, params);


        mPlayButton = new PlayButton(this);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        rl.addView(mPlayButton, params);

    //    txtView = (TextView) findViewById(R.id.txtField);
        this.uuit = new UpdateUIThread(recorder, bufferSize, handle, res, needZeropadding, column_number);


       // gridView = new GridView(this);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setColumnWidth(1);
        gridView.setNumColumns(column_number);

        gridView.setPadding(1, 1, 1, 1);


        out_array = new String[120];

        Arrays.fill(out_array, "");


        cells = new ArrayList<>(Arrays.asList(out_array));


       /* for (int i = 0; i < 60; i++) {
            cells.add("");
        }*/

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, out_array) {


            @Override
            public long getItemId(final int position) {
                return position;
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                if (convertView == null){
                    // No view passed, create one !
                    LayoutInflater li = getLayoutInflater();
                    convertView = li.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
                }


                View view = super.getView(position, convertView, parent);
                view.setLayoutParams(new AbsListView.LayoutParams(70, 70));



                int color = Color.BLACK; // Transparent

                final TextView text = (TextView) view.findViewById(android.R.id.text1);

                text.setText(out_array[position]);

                view.setBackgroundColor(color);


                return view;
            }
        };


        GridAdapter gridAdapter = new GridAdapter(cells);

        gridView.setAdapter(adapter);


     /*   params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        params.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);


        params.addRule(RelativeLayout.ALIGN_BOTTOM, RelativeLayout.TRUE);*/



//        rl.addView(gridView, params);

        /**************************************************************************************************************/
/*
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(80000);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(22050);


        dp_arr = new DataPointFaster[bufferSize];

        for (int i = 0; i< bufferSize; i++) {
            dp_arr[i] = new DataPointFaster(i, 0);
        }
        series = new LineGraphSeries<DataPointFaster>(dp_arr);
        graph.addSeries(series);
        ugt = new UpdateGraphTask();*/

        lightgreen = this.getResources().getColor(R.color.lightgreen);
        blue = this.getResources().getColor(R.color.blue);
        lightblue = this.getResources().getColor(R.color.lightblue);
        green = this.getResources().getColor(R.color.green);
        yellow = this.getResources().getColor(R.color.yellow);
        lightyellow = this.getResources().getColor(R.color.lightyellow);
        orange = this.getResources().getColor(R.color.orange);
        red = this.getResources().getColor(R.color.red);
        lightorange = this.getResources().getColor(R.color.lightorange);
        darkred = this.getResources().getColor(R.color.darkred);
        cyan = this.getResources().getColor(R.color.cyan);









        PianoRollExample pre = new PianoRollExample();

    /*    Uri auri = Uri.parse("android.resource://" + getPackageName() +R.raw.chopin);
        URI juri = null;
        try {
            juri = new URI(auri.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
*/
        Resources res = getResources();
        AssetManager am = res.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open("test.mid");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("EEEEEERYBOOOOODY");

        try {
            System.out.println(inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = createFileFromInputStream(inputStream);

        //       pre.doSmth(file);


        PianoRollExampleDoubles prd = new PianoRollExampleDoubles();
        PianoRollExample pr = new PianoRollExample();

        //  pr.doSmth(file);

        MidiSegObject result = new FindMidiSeg(prd.doSmth(file)).findMidiSeg();

        System.out.println("[");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < result.scoreMP[0].length; j++) {
                DecimalFormat df = new DecimalFormat("00.00");
                System.out.print(df.format(result.scoreMP[i][j]) + " ");
            }
            System.out.print("\n");
        }
        System.out.println("]\n\n\n");
        System.out.println("]\n\n\n");
        System.out.println("]\n\n\n");


        for (int i = 0; i < result.scoreSeg.length; i++) {
            for (int j = 0; j < result.scoreSeg[0].length; j++) {
                DecimalFormat df = new DecimalFormat("00.00");
                System.out.print(df.format(result.scoreSeg[i][j]) + " ");
            }
            System.out.print("\n");

        }

        System.out.print("\n");
        System.out.print("\n");
        System.out.print("\n");
        System.out.println(result.segIdx.size());

        Iterator<Integer> iter = result.segIdx.iterator();
        System.out.print("[[");
        while(iter.hasNext()) {
            System.out.print("" + iter.next() + " ");
        }
        System.out.print("]]\n");


        for (int i = 0; i < result.segIdx.size(); i++) {
            System.out.print((int) result.segIdx.toArray()[i] + " ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUImethod()  {
        new UpdateGraphTask().execute();
    }


    @Override

    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
      /*  mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }*/

         if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            audioPlayer.play();

    }

    private void stopPlaying()
    {
       if (audioPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
           audioPlayer.stop();
    }

    private void startRecording() {

        if (recorder == null) {
            recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        }


        if (recorder.getState() == AudioRecord.RECORDSTATE_STOPPED) {
            if (uuit == null) {
                uuit =  new UpdateUIThread(recorder, bufferSize, handle, res, needZeropadding, column_number);
            }
            recorder.startRecording();
            pool.execute(uuit);
        }

    }

    private void stopRecording() {
       /* if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING)
            recorder.stop();
        if (uuit != null) {
            series = null;
            uuit.setRunning(false);
            pool.remove(uuit);

        //    pool.shutdownNow();

            recorder.release();
        }
        recorder = null;
        uuit = null;*/


        new StopRecordTask().execute();
    }



    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }




    class StopRecordTask extends AsyncTask {
        protected String doInBackground(Object... params) {
            if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING)
                recorder.stop();

            if (uuit != null) {
                uuit.setRunning(false);
                pool.remove(uuit);
            }

            recorder.release();

            recorder = null;
            uuit = null;

            return "message sent";
        }
    }



    class UpdateGraphTask extends AsyncTask <Void, Void, DataPointFaster[]> {


        @Override
        protected DataPointFaster[] doInBackground(Void... params) {
            for (int i = 0; i < bufferSize; i++) {
                dp_arr[i].setX(i);
                dp_arr[i].setY(fft_arr[i]);
            }



          /*  runOnUiThread(new Runnable() {
                public void run() {
                    series.resetData(dp_arr);
                }
                });*/
            return dp_arr;
        }

        protected void onPostExecute(DataPointFaster[] result) {
            series.resetData(result);
        }
    }

    public String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            sb.append(" ");
        }

        return sb.toString();
    }




    class CalculateColorsTask extends AsyncTask <Object, Object, int[]> {


        @Override
        protected int[] doInBackground(Object... params) {

            int[] colors = new int[received_arr.length];


            for (int i = 0; i < colors.length - 1; i++) {
                colors[i] = calculateColor(received_arr[i]);
            }

            colors[colors.length - 1] = (int) received_arr[colors.length - 1];

            return colors;
        }

        protected void onPostExecute(int[] result) {


            int j  = result[result.length - 1];
            out_array[result.length - 2 + j] = "->";


          /*  adapter.insert("->", result.length - 3 + j);
            adapter.notifyDataSetChanged();
*/
            for (int i = 0; i < result.length - 2; i++) {
                int index = j + i * column_number;
         //       Log.d("index", "" + index + " " + result[i]);
                gridView.getChildAt( index ).setBackgroundColor(result[i]);
                Drawable gradientDrawable = gridView.getChildAt( index ).getBackground();
                gradientDrawable.mutate(); // needed line
                gradientDrawable.setColorFilter(result[i], PorterDuff.Mode.MULTIPLY);
              //  gradientDrawable.setColor(result[i]);
                gradientDrawable.invalidateSelf();

            }
        }
        int[] colors = {darkred,red, orange, lightorange, yellow, lightyellow, green,
                lightgreen, cyan, lightblue,
                blue, Color.BLACK};
        // 8 - grey 10 blue
        // 7, 6, 5 - grey
        //

        public int calculateColor(double val) {

            double max = 1000000;
            max *= max;


          //  max = Double.MAX_VALUE / 10000;

            int result = 0;
            if (val > max / 2) {
                if (val > max / 2 + max / 4) {
                    if (val > 2 * (max - max / 2) / 3) {
                        result = colors[0];
      ///                  Log.d("colors","point 0");
                    } else if (val >= (2/3)*max  && val <= (5/6)*max) {
                        result = colors[1];
    //                    Log.d("colors","point 1");

                    } else if ( val >= max/2 && val <= (2/3)*max) {
                        result = colors[2];
  //                      Log.d("colors","point 2");

                    }
                } else {
                    if (val > max / 2 + 2 * (max / 4) / 3) {
                        result = colors[3];
//                        Log.d("colors","point 3");


                    } else if (val > (max / 4) / 3) {
                        result = colors[4];
            //            Log.d("colors","point 4");


                    } else if ( val  <= (max / 4) / 3) {
                        result = colors[5];
          //              Log.d("colors","point 5");

                    }
                }
            } else {
                if (val > max / 4) {
                    if (val > max / 4 + 2 * (max / 2 - max / 4) / 3) {
                        result = colors[6];
         //               Log.d("colors","point 6");


                    } else if (val > max / 4 + (max / 2 - max / 4) / 3) {
                        result = colors[7];
         //               Log.d("colors","point 7");


                    } else {
                        result = colors[8];
           //             Log.d("colors","point 8");


                    }
                } else {
                    if (val > 2 * max / 12) {
                        result = colors[9];
             //           Log.d("colors","point 9");


                    } else if (val > max / 12) {
                        result = colors[10];
               ///         Log.d("colors","point 10");


                    } else {
                        result = colors[11];
                  //      Log.d("colors","point 11");


                    }
                }
            }


            return result;
        }
    }


    private static final class GridAdapter extends BaseAdapter {

        final ArrayList<String> mItems;
        final int mCount;

        /**
         * Default constructor
         * @param items to fill data to
         */
        private GridAdapter(final ArrayList<String> items) {

            mCount = items.size() * column_number;
            mItems = new ArrayList<String>(mCount);

            // for small size of items it's ok to do it here, sync way
            for (String item : items) {
                // get separate string parts, divided by ,
                final String[] parts = item.split(",");

                // remove spaces from parts
                for (String part : parts) {
                    part.replace(" ", "");
                    mItems.add(part);
                }
            }
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(final int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            view.setLayoutParams(new AbsListView.LayoutParams(100, 100));



            int color = 0x00FFFFFF; // Transparent
            if (true) {
                Random rnd = new Random();
                color = 0x00FFFFFF; // Opaque Blue
            }
            final TextView text = (TextView) view.findViewById(android.R.id.text1);

            text.setText(mItems.get(position));

            view.setBackgroundColor(color);


            return view;
        }
    }

    private File createFileFromInputStream(InputStream inputStream) {

        try{
            File f = new File(getCacheDir()+"/test.mid");

            OutputStream outputStream = new FileOutputStream(f);

            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
        }
        return null;
    }
}

