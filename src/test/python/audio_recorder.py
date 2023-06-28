import getopt
import pyaudio
import signal
import sys
import wave

# Declare global variables
p = pyaudio.PyAudio()
stream = None
frames = []
fs = 44100  # Record at 44100 samples per second
channels = 1
chunk = 44100  # Record in chunks of 1024 samples
sample_format = pyaudio.paInt16  # 16 bits per sample
NUM_SAMPLES_TO_RECORD = 10
filename = f"./src/test/resources/audio/game/records/output_record.wav"


#################################


def signal_handler(sig, frame):
    # This function will be called when the script is being terminated
    global p, stream, frames, channels, sample_format, fs, filename
    # This function will be called when the script is being terminated
    # Stop and close the stream
    if stream is not None:
        stream.stop_stream()
        stream.close()

    # Terminate the PortAudio interface
    if p is not None:
        p.terminate()

    print('Finished recording')

    # Save the recorded data as a WAV file
    if frames:
        wf = wave.open(filename, 'wb')
        wf.setnchannels(channels)
        wf.setsampwidth(p.get_sample_size(sample_format))
        wf.setframerate(fs)
        wf.writeframes(b''.join(frames))
        wf.close()
        print('Saved file ', filename)

    sys.exit(0)  # terminate the process


# These lines set up the signal handler
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


def argReader(argv):
    print("Python argv: ", argv)
    recordSize = 15
    numRecords = 1
    opts, args = getopt.getopt(argv, "hi:o:", ["recordSize=", "numRecords="])
    try:

        for opt, arg in opts:
            if opt == '-h':
                print('test.py -s <recordSize> (in seconds) -n <numRecords>')
                sys.exit()
            elif opt in ("-s", "--recordSize"):
                recordSize = float(arg)
            elif opt in ("-n", "--numRecords"):
                numRecords = int(arg)
        print('Record size is ', recordSize)
        print('num of records is ', numRecords)
    except ValueError:
        print("The 'seconds' variable does not contain a valid number.")
    return recordSize, numRecords


recordSize, numRecords = 25, 1
# if __name__ == "__main__":
#    recordSize, numRecords = argReader(sys.argv[1:])

# argReader(sys.argv)

input_device_index = None
for i in range(p.get_device_count()):
    dev = p.get_device_info_by_index(i)
    if dev['name'].lower().startswith("stereo mix") or dev['name'].lower().startswith("what u hear"):
        input_device_index = i
        print("input selected: ", dev['name'])
        break
if input_device_index is None:
    raise ValueError("No 'Stereo Mix' or 'What U Hear' input device found.")

p = pyaudio.PyAudio()  # Create an interface to PortAudio

print('Recording ',
      #    numRecords,
      #   ' long ',
      #   recordSize,
      #   ' seconds'
      '...')

stream = p.open(format=sample_format,
                channels=channels,
                rate=fs,
                frames_per_buffer=chunk,
                input=True,
                input_device_index=input_device_index)

frames = []  # Initialize array to store frames
# Store data in chunks for 3 seconds
t = 0
for t in range(0, int(fs / chunk * recordSize)):
    data = stream.read(chunk)
    frames.append(data)


def close_and_save_record():
    # Stop and close the stream
    stream.stop_stream()
    stream.close()
    # Terminate the PortAudio interface
    p.terminate()
    print('Finished recording')
    print('Saving file ', filename, ' ... ')
    # Save the recorded data as a WAV file
    wf = wave.open(filename, 'wb')
    wf.setnchannels(channels)
    wf.setsampwidth(p.get_sample_size(sample_format))
    wf.setframerate(fs)
    wf.writeframes(b''.join(frames))
    wf.close()
    print('Saved file ', filename)


close_and_save_record()
