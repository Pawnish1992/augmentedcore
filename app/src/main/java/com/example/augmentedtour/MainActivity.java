package com.example.augmentedtour;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button animationButton;
    private ModelAnimator modelAnimator;
    private int count = 0;
    private TextToSpeech textToSpeech;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            init_text_to_speech();
            createModel(hitResult.createAnchor(), arFragment);
        });
    }

    public void init_text_to_speech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int ttsLang = textToSpeech.setLanguage(Locale.ENGLISH);

                if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                        || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!");
                } else {
                    Log.i("TTS", "Language Supported.");
                }
                Log.i("TTS", "Initialization success.");
            } else {
                Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createModel(Anchor anchor, ArFragment arFragment) {

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("rendered.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    AnchorNode anchorNode = new AnchorNode(anchor);

                    SkeletonNode skeletonNode = new SkeletonNode();
                    skeletonNode.setParent(anchorNode);
                    skeletonNode.setRenderable(modelRenderable);

                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    animationButton = findViewById(R.id.bt_animate);
                    animationButton.setOnClickListener(view -> animateModel(modelRenderable));

                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void animateModel(ModelRenderable modelRenderable) {
        if (modelAnimator != null && modelAnimator.isRunning()) {
            modelAnimator.end();
        }
        int animationCount = modelRenderable.getAnimationDataCount();

        if (count == animationCount) {
            count = 0;
        }

        AnimationData animationData = modelRenderable.getAnimationData(count);
        modelAnimator = new ModelAnimator(animationData, modelRenderable);
        modelAnimator.start();
        speakText();
        count++;
    }

    private void speakText() {
        int speechStatus = textToSpeech.speak("Hi Kalyan, Let's go party", TextToSpeech.QUEUE_FLUSH, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
