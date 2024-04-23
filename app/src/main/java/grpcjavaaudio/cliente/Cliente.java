package grpcjavaaudio.cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.proto.audio.AudioServiceGrpc;
import com.proto.audio.Audio.DownloadFileRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Cliente {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        String name;

        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build();

        // name = "anyma.wav";
        // streamWav(channel, name, 44100F);

        // name = "tiesto.mp3";
        // ByteArrayInputStream streamMP3 = downloadFile(channel, name);
        // playMp3(streamMP3, name);
        // try {
        //     streamMP3.close();
        // } catch (IOException e) {
        // }

        // System.out.println("Apagando...");
        // channel.shutdown();

        name = "sample.wav";
        ByteArrayInputStream streamWAV = downloadFile(channel, name);
        playWav(streamWAV, name);
        try {
            streamWAV.close();
        } catch (IOException e) {
        }

        System.out.println("Apagando...");
        channel.shutdown();
    }

    public static void streamWav(ManagedChannel ch, String name, float sampleRate) {
        try {
            AudioFormat newFormat = new AudioFormat(sampleRate, 16, 2, true, false);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(newFormat);
            sourceDataLine.open(newFormat);
            sourceDataLine.start();

            AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(ch);
            DownloadFileRequest request = DownloadFileRequest
                .newBuilder()
                .setNombre(name)
                .build();

            int bufferSize = 1024;
            System.out.println("Reproduciendo archivo: " + name);

            stub.downloadAudio(request).forEachRemaining(response -> {
                try {
                    sourceDataLine.write(response.getData().toByteArray(), 0, bufferSize);
                    System.out.print(".");
                } catch (Exception e) {
                }
            });
            System.out.println("\n\nRecepcion de datos correcta.");
            System.out.println("Reproduccion terminada.");

            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ByteArrayInputStream downloadFile(ManagedChannel ch, String name) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(ch);
        DownloadFileRequest request = DownloadFileRequest
            .newBuilder()
            .setNombre(name)
            .build();

        System.out.println("Recibiendo el archivo: " + name);
        stub.downloadAudio(request).forEachRemaining(response -> {
            try {
                stream.write(response.getData().toByteArray());
                System.out.print(".");
            } catch (Exception e) {
                System.out.println("No se pudo obtener el archivo de audio. " + e.getMessage());
            }
        });
        System.out.println("\n\nRecepcion de datos correcta.");

        return new ByteArrayInputStream(stream.toByteArray());
    }

    public static void playWav(ByteArrayInputStream inStream, String name) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(inStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Reproduciendo el archivo: " + name);
            clip.start();

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
            clip.stop();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } catch(LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void playMp3(ByteArrayInputStream inStream, String name) {
        try {
            System.out.println("Reproduciendo el archivo: " + name);
            Player player = new Player(inStream);
            player.play();
        } catch (JavaLayerException e) {
        }
    }
}
