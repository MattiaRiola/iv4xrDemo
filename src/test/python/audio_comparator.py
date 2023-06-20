from AudioAnalyzer import *

original = AudioAnalyzer("../audio/yesterday-2009.wav", input_sr=44100, fft_size=44100)
original.plot_spectrum(min_freq=20, max_freq=1000, title="1965 Original")

