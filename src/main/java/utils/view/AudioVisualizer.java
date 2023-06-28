package utils.view;

import entity.audio.AudioSignal;

import java.awt.*;
import java.util.List;

public class AudioVisualizer {

    public static void showGraphs(List<AudioSignal> audiosToBePlotted, boolean showSpectrum, boolean showTimeDomain) throws InterruptedException {
        for (AudioSignal audio : audiosToBePlotted) {
            if (showTimeDomain)
                Plot2D.plotArray(audio.getSamples(), Color.RED, "Time Domain\t" + audio.getName());
            if (showSpectrum)
                PlotSpectrum2D.plotSpectrum(audio.getSpectrogram(), "Spectrum\t" + audio.getName());
        }
        if (audiosToBePlotted.size() > 0)
            Thread.sleep(100000);
    }
}
