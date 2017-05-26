package io.github.pkhanal;

import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.Language;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;
import jersey.repackaged.jsr166e.CompletableFuture;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class App
{
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.load(new FileInputStream("config.properties"));

        LanguageTranslator translator = new LanguageTranslator();
        translator.setUsernameAndPassword(prop.getProperty("TRANSLATION_USERNAME"), prop.getProperty("TRANSLATION_PASSWORD"));

        TextToSpeech tts = new TextToSpeech();
        tts.setUsernameAndPassword(prop.getProperty("TTS_USERNAME"), prop.getProperty("TTS_PASSWORD"));

        translator
          .translate("hello", Language.ENGLISH, Language.FRENCH)
          .rx()
          .thenApply(translationResult -> translationResult.getFirstTranslation())
          .thenApply(translation -> tts.synthesize(translation, Voice.FR_RENEE, AudioFormat.WAV).rx())
          .thenAccept(App::processSpeechSynthesis);
    }

    private static void processSpeechSynthesis(CompletableFuture<InputStream> result) {
        try {
            InputStream stream = result.get();
            stream = WaveUtils.reWriteWaveHeader(stream);
            AudioStream audioStream = new AudioStream(stream);
            AudioPlayer.player.start(audioStream);
            stream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
