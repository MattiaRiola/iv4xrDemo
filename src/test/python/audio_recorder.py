import getopt
import pyaudio
import sys
import wave


def argReader(argv):
    recordSize = ''
    numRecords = ''
    opts, args = getopt.getopt(argv, "hi:o:", ["recordSize=", "numRecords="])
    for opt, arg in opts:
        if opt == '-h':
            print('test.py -s <recordSize> (in seconds) -n <numRecords>')
            sys.exit()
        elif opt in ("-s", "--recordSize"):
            recordSize = arg
        elif opt in ("-n", "--numRecords"):
            numRecords = arg
    print('Record size is ', recordSize)
    print('num of records is ', numRecords)
    return recordSize, numRecords


def record(seconds, numRecords):
    p = pyaudio.PyAudio()
    #################################

    NUM_SAMPLES_TO_RECORD = 10

    chunk = 44100  # Record in chunks of 1024 samples
    sample_format = pyaudio.paInt16  # 16 bits per sample
    channels = 2
    fs = 44100  # Record at 44100 samples per second

    argReader(sys.argv)

    input_device_index = None
    for i in range(p.get_device_count()):
        dev = p.get_device_info_by_index(i)
        if dev['name'].lower().startswith("stereo mix") or dev['name'].lower().startswith("what u hear"):
            input_device_index = i
            print("input selected: ", dev['name'])
            break
    if input_device_index is None:
        raise ValueError("No 'Stereo Mix' or 'What U Hear' input device found.")

    for i in range(NUM_SAMPLES_TO_RECORD):
        filename = f"./src/test/resources/audio/game/records/output_record_{i}.wav"

        p = pyaudio.PyAudio()  # Create an interface to PortAudio

        print('Recording')

        stream = p.open(format=sample_format,
                        channels=channels,
                        rate=fs,
                        frames_per_buffer=chunk,
                        input=True,
                        input_device_index=input_device_index)

        frames = []  # Initialize array to store frames

        # Store data in chunks for 3 seconds
        for i in range(0, int(fs / chunk * seconds)):
            data = stream.read(chunk)
            frames.append(data)

        # Stop and close the stream
        stream.stop_stream()
        stream.close()
        # Terminate the PortAudio interface
        p.terminate()

        print('Finished recording')

        # Save the recorded data as a WAV file
        wf = wave.open(filename, 'wb')
        wf.setnchannels(channels)
        wf.setsampwidth(p.get_sample_size(sample_format))
        wf.setframerate(fs)
        wf.writeframes(b''.join(frames))
        wf.close()
        print('Saved file ', filename)


recordSize, numRecords = 15, 1
if __name__ == "__main__":
    recordSize, numRecords = argReader(sys.argv[1:])

record(recordSize, numRecords)
