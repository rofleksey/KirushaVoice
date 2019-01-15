package ru.rofleksey;

import javafx.util.Pair;

import javax.sound.sampled.*;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import static ru.rofleksey.Recorder.format;

public class Player {
    LinkedBlockingQueue<byte[]> queue;
    Thread thread;
    volatile boolean stopped;

    void start() {
        queue = new LinkedBlockingQueue<>(5);
        thread = new Thread(() -> {
            Mixer.Info[] arrMixerInfo = AudioSystem.getMixerInfo();
            try {
                for (int i = 0; i < arrMixerInfo.length; i++) {
                    Mixer m = AudioSystem.getMixer(arrMixerInfo[i]);
                    m.open();
                    System.out.println(arrMixerInfo[i].getName() + " : " + arrMixerInfo[i].getDescription() + " ");
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
            SourceDataLine line;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            System.out.println(info.toString());
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line is unsupported!");
                return;
            }
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                //line = AudioSystem.getSourceDataLine(format);
                line.addLineListener(event -> System.out.println(event));
                line.open();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
                return;
            }
            line.start();
            System.out.println("Started player loop");
            while(!stopped) {
                try {
                    byte[] cur = queue.take();
                    System.out.println(line.write(cur, 0, cur.length));
                    //line.flush();
                    //System.out.println("Got "+ Arrays.toString(cur));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        thread.start();
    }

    void write(byte[] data) throws InterruptedException {
        queue.put(data);
    }
}
