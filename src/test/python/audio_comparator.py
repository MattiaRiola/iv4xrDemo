from AudioAnalyzer import *

original = AudioAnalyzer("yesterday-original.wav", input_sr=44100, fft_size=44100)
remaster09 = AudioAnalyzer("yesterday-2009.wav", input_sr=44100, fft_size=44100)
remaster15 = AudioAnalyzer("yesterday-2015.wav", input_sr=44100, fft_size=44100)
# original.plot_spectrum(min_freq=20, max_freq=1000, title="1965 Original")
remasters = SpectrumCompare(remaster09, remaster15)
orig_to_09 = SpectrumCompare(original, remaster09)
orig_to_15 = SpectrumCompare(original, remaster15)
print(remasters)

orig_to_09.plot_spectrum_group(frange=(20,1000),
    ratio=True,
    threshold=True,
    title="Spectrograms - original vs 2009 remaster (with threshold)",
    legend=("Original", "Remaster", "Amplitude Diff.", "Threshold")
    )

orig_to_09.plot_spectrum_heatmap(
    frange=(20,1000),
    plot_spec1=False,
    title="Original vs 2009 remaster"
)

orig_to_09.plot_spectrum_heatmap(
    frange=(20,500),
    plot_spec1=False,
    title="Original vs 2009 remaster"
)