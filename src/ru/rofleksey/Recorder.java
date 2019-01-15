package ru.rofleksey;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;

public class Recorder {
    volatile boolean stopped;
    Thread thread;
    static final AudioFormat format = new AudioFormat(44100, 8, 1, true, true);

    void start(Supplier<Player> onOpen) {
        thread = new Thread(()->{
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line is unsupported!");
                return;
            }
            try {
                //line = (TargetDataLine) AudioSystem.getLine(info);
                //System.out.println("Recorder: "+line.getLineInfo().toString());
                line = AudioSystem.getTargetDataLine(format);
                line.open();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
                return;
            }
            line.start();
            Player player = onOpen.get();
            player.start();
            int numBytesRead;
            byte[] data = new byte[line.getBufferSize()];
            System.out.println("Started recorder loop");
            while (!stopped) {
                for(int i = 0; i < data.length; i++) {
                    data[i] = (byte)(255*Math.random());
                }
                numBytesRead = data.length;
                //numBytesRead = line.read(data, 0, data.length);
                byte[] n = copyArr(data, numBytesRead);
                try {
                    player.write(n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    byte[] copyArr(byte[] data, int up) {
        byte[] n = new byte[up];
        System.arraycopy(data, 0, n, 0, up);
        return n;
    }
}
