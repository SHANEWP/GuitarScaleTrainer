package com.example.guitarscaletrainer;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> scales;
    private ArrayList<String> twoOctaveOnlyScales;
    private ArrayList<String> RHFingerings;
    private ArrayList<String> RHStyles;
    private TextView scale;
    private TextView RHFingering;
    private TextView RHStyle;
    private TextView octaves;

    private boolean tunerIsOff;
    private AudioTrack tunerTrack;

    private boolean metronomeIsOff;
    private AudioTrack metronomeTrack;
    private int currentTempo;

    private final int startingTempo = 100;
    private final int maximumTempo = 300;
    private SeekBar tempoSeekBar;

    private TextToSpeech textToSpeech;
    private final float TEXT_TO_SPEECH_PITCH = (float) 1;
    private final float TEXT_TO_SPEECH_SPEED = (float) 1;


    /**
     * Sets up the variables used to manipulate the text of the various labels and gets the scales.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS", "Language not supported");
                } else Log.e("TTS", "Initialization failed");
            }
        });

        setScales();
        setRightHandFingerings();
        setRightHandStyles();

        tunerIsOff = true;
        metronomeIsOff = true;

        setContentView(R.layout.activity_main);
        scale = findViewById(R.id.scaleLabel);
        RHFingering = findViewById(R.id.rightHandFingeringLabel);
        RHStyle = findViewById(R.id.rightHandStyleLabel);
        octaves = findViewById(R.id.octaveNumberLabel);

        createLabelValues();
        tempoSeekBar();
    }

    /**
     * Stops the text to speech on destroy
     */
    @Override
    protected void onDestroy() {
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
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


    /**
     * Generates new random elements and changes the label text accordingly.
     *
     * @param view
     */
    public void generateNext(View view) {
        createLabelValues();
    }

    /**
     * Creates the initial random content of the labels
     */
    private void createLabelValues() {
        int temp = ThreadLocalRandom.current().nextInt(scales.size()); //used for scales array index
        boolean hasThumb = false;//referring to right hand fingering

        scale.setText(scales.get(temp));
        octaves.setText(getOctaves(scales.get(temp)));

        String fingering = RHFingerings.get(ThreadLocalRandom.current().nextInt(RHFingerings.size()));
        RHFingering.setText(fingering);

        //checks if the right hand fingering includes the thumb
        for(int i = 0; i < fingering.length(); i++) {
            if(fingering.charAt(i) == 'p')
                hasThumb = true;
        }

        //can not play rest stroke alternating with thumb
        if(hasThumb)
            RHStyle.setText("Free Stroke");
        else
            RHStyle.setText(RHStyles.get(ThreadLocalRandom.current().nextInt(RHStyles.size())));
    }

    /**
     * Checks the given scale with the twoOctaveOnlyScales list to determine if it can be played in
     * three octaves.
     * If it can, 2 of 3 octaves will be returned at random.
     * If it cannot, only 2 octave will be returned.
     *
     * @param scale
     * @return
     */
    private String getOctaves(String scale) {
        boolean good = true; //true if three octaves is available
        for (int i = 0; i < twoOctaveOnlyScales.size(); i++)
            if (scale.equals(twoOctaveOnlyScales.get(i))) {
                good = false;
                break;
            }
        if (good) {
            if (ThreadLocalRandom.current().nextBoolean())
                return "Three Octaves";
            else
                return "Two Octaves";
        } else
            return "Two Octaves";
    }

    /**
     * Sets the RHFingerings arrayList to right hand fingerings.
     */
    private void setRightHandFingerings() {
        RHFingerings = new ArrayList<>();

        RHFingerings.add("i-m");
        RHFingerings.add("m-i");
        RHFingerings.add("m-a");
        RHFingerings.add("a-m");
        RHFingerings.add("i-a");
        RHFingerings.add("a-i");
        RHFingerings.add("p-i");
        RHFingerings.add("i-p");
    }

    /**
     * Sets the RHStyles arrayList to right hand plucking styles.
     */
    private void setRightHandStyles() {
        RHStyles = new ArrayList<>();

        RHStyles.add("Free Stroke");
        RHStyles.add("Rest Stroke");
    }

    /**
     * Sets the scales arrayList to scales.
     * If the scale can only be played in two octaves, it is also added to the twoOctaveOnlyScales
     * array.
     */
    private void setScales() {
        scales = new ArrayList<>();
        twoOctaveOnlyScales = new ArrayList<>();

        scales.add("C Major");
        twoOctaveOnlyScales.add("C Major");
        scales.add("G Major");
        scales.add("D Major");
        twoOctaveOnlyScales.add("D Major");
        scales.add("A Major");
        scales.add("E Major");
        scales.add("B Major");
        scales.add("Gb Major");
        scales.add("Db Major");
        twoOctaveOnlyScales.add("Db Major");
        scales.add("Ab Major");
        scales.add("Eb Major");
        twoOctaveOnlyScales.add("Eb Major");
        scales.add("Bb Major");
        scales.add("F Major");
        scales.add("C# Major");
        twoOctaveOnlyScales.add("C# Major");
        scales.add("F# Major");
        scales.add("Cb Major");
        scales.add("C Harmonic Minor");
        twoOctaveOnlyScales.add("C Harmonic Minor");
        scales.add("G Harmonic Minor");
        scales.add("D Harmonic Minor");
        twoOctaveOnlyScales.add("D Harmonic Minor");
        scales.add("A Harmonic Minor");
        scales.add("E Harmonic Minor");
        scales.add("B Harmonic Minor");
        scales.add("F# Harmonic Minor");
        scales.add("C# Harmonic Minor");
        twoOctaveOnlyScales.add("C# Harmonic Minor");
        scales.add("G# Harmonic Minor");
        scales.add("Eb Harmonic Minor");
        twoOctaveOnlyScales.add("Eb Harmonic Minor");
        scales.add("Bb Harmonic Minor");
        scales.add("F Harmonic Minor");
        scales.add("C Melodic Minor");
        twoOctaveOnlyScales.add("C Melodic Minor");
        scales.add("G Melodic Minor");
        scales.add("D Melodic Minor");
        twoOctaveOnlyScales.add("D Melodic Minor");
        scales.add("A Melodic Minor");
        scales.add("E Melodic Minor");
        scales.add("B Melodic Minor");
        scales.add("F# Melodic Minor");
        scales.add("C# Melodic Minor");
        twoOctaveOnlyScales.add("C# Melodic Minor");
        scales.add("G# Melodic Minor");
        scales.add("Eb Melodic Minor");
        twoOctaveOnlyScales.add("Eb Melodic Minor");
        scales.add("Bb Melodic Minor");
        scales.add("F Melodic Minor");
    }

    /**
     * Turns the tuning pitch on or off.
     *
     * @param view
     */
    public void playTuningPitch(View view) {
        try {

            Button button = findViewById(R.id.tuningButton);

            if (tunerIsOff) {
                tunerIsOff = false;

                String string = button.getText().toString();

                //retreives the integer from the tuning button's text to determine the frequency
                int frequency = Integer.parseInt(string.split(" ", 0)[0]);

                generateSound(frequency, 3);
                tunerTrack.play();
                button.setBackgroundColor(Color.rgb(0,255,0));

            } else {
                tunerIsOff = true;
                tunerTrack.stop();
                button.setBackgroundResource(android.R.drawable.btn_default);
            }

        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an AudioTrack that produces the pitch used for the tuner and assigns it to the
     * private global variable track.
     * @param frequency The frequency of the desired pitch
     * @param duration The length of the track in seconds
     */
    private void generateSound(int frequency, int duration) {
        int sampleRate = 44100; //Standard sample rate
        byte[] soundData = new byte[sampleRate * duration];

        for(int i = 0; i < soundData.length; i++) {
            double fundamental = Math.sin((2 * Math.PI * frequency * i) / sampleRate);
            soundData[i] = (byte)(fundamental * 255);
        }

        tunerTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_DEFAULT,
                AudioFormat.ENCODING_PCM_8BIT, soundData.length, AudioTrack.MODE_STATIC);

        tunerTrack.write(soundData, 0, soundData.length);
        tunerTrack.setLoopPoints(0, tunerTrack.getBufferSizeInFrames(), -1);
    }

    /**
     * Plays a short sound on each beat for the desired BPM
     *
     * @param view
     */
    public void metronomeOnOff(View view) {
        try {

            Button button = findViewById(R.id.metronomeButton);

            if (metronomeIsOff) {
                metronomeIsOff = false;

                generateMetronome(BPM(currentTempo));
                metronomeTrack.play();
                button.setBackgroundColor(Color.rgb(0, 255, 0));

            } else {
                metronomeIsOff = true;
                metronomeTrack.stop();
                button.setBackgroundResource(android.R.drawable.btn_default);
            }

        } catch(NumberFormatException e){
            e.printStackTrace();
        }
    }

    /**
     * creates a 60 second track of even beats of sound and silence to loop for the metronome.
     * @param bpm the desired beats per minute.
     */
    private void generateMetronome(int bpm) {
        int sampleRate = 44100;//standard sample rate
        int duration = 60;//Length of the track in seconds
        byte[] soundData = new byte[sampleRate * duration];//total samples
        int samplesPerBeat = soundData.length/bpm;
        int tickFrequency = 14;
        int tickLength = 1000;//number of samples for the tick sound
        int silenceLength = samplesPerBeat - tickLength;//length in samples
        double[] tick = new double[tickLength];//Stores the sound data for the tick sound

        //Creates a sine wave for the tick sound
        for(int i=0; i < tickLength; i++){
            tick[i] = Math.sin((2 * Math.PI * i * tickFrequency) / tickLength);
        }

        for(int tickCounter = 0, silenceCounter = 0, soundDataCounter = 0; soundDataCounter < soundData.length; soundDataCounter++){
            if(tickCounter < tickLength)//fills up the array with the tick data
            {
                soundData[soundDataCounter] = (byte) (tick[tickCounter] * 255);
                tickCounter++;
            }
            else if(silenceCounter < silenceLength)//when tick data is finished entering fill remaining space with silence before next beat
            {
                soundData[soundDataCounter] = (byte) 0;
                silenceCounter++;
            }
            else//resets the counters for tick and silence
            {
                tickCounter = 0;
                silenceCounter = 0;
            }
        }
        metronomeTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_DEFAULT,
                AudioFormat.ENCODING_PCM_8BIT, soundData.length, AudioTrack.MODE_STATIC);

        metronomeTrack.write(soundData, 0, soundData.length);
        metronomeTrack.setLoopPoints(0, metronomeTrack.getBufferSizeInFrames(), -1);
    }

    /**
     * Creates a Listener for the metronome's seek bar. It updates the current tempo as it changes.
     */
    private void tempoSeekBar() {
        tempoSeekBar = findViewById(R.id.tempoSeekBar);
        tempoSeekBar.setProgress(startingTempo);
        tempoSeekBar.setMax(maximumTempo);
        currentTempo = tempoSeekBar.getProgress();

        Button button = findViewById(R.id.metronomeButton);
        button.setText(BPM(currentTempo) + " bpm");

        tempoSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        currentTempo = tempoSeekBar.getProgress();

                        Button button = findViewById(R.id.metronomeButton);
                        button.setText(BPM(currentTempo) + " bpm");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if(!metronomeIsOff)
                            metronomeTrack.stop();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        generateMetronome(BPM(currentTempo));

                        if(!metronomeIsOff)
                            metronomeTrack.play();
                    }
                }
        );
    }

    /**
     * Speaks the text inside the scale label
     */
    public void speakScale(View view) {
        textToSpeech.setPitch(TEXT_TO_SPEECH_PITCH);
        textToSpeech.setSpeechRate(TEXT_TO_SPEECH_SPEED);
        textToSpeech.speak(convertMusicSymbols(scale.getText().toString()), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * If there is a '#' it is replaced with the word sharp
     * If there is a 'b' it is replaced with the word flat etc.
     * @param text text that will be passed to textToSpeech
     * @return the string with the proper words in it
     */
    private String convertMusicSymbols(String text) {
        ArrayList<String> newText = new ArrayList<>();
        for(int i=0; i<text.length(); i++) {
            switch (text.charAt(i)) {
                case 'A':
                    newText.add("ey");
                    break;
                case '#':
                    newText.add(" sharp");
                    break;
                case 'b':
                    newText.add(" flat");
                    break;
                default:
                    newText.add(Character.toString(text.charAt(i)));
            }
        }
        String s = "";
        for(int i=0; i<newText.size(); i++)
            s += newText.get(i);
        return s;
    }

    /**
     * Makes sure the BPM is usable (not zero)
     * @param bpm the beats per minute to test
     * @return a valid bpm
     */
    private int BPM(int bpm) {
        return bpm == 0 ? 1 : bpm;
    }
}
