package grpcjavaaudio.servidor;

import java.io.InputStream;

import com.google.protobuf.ByteString;
import com.proto.audio.AudioServiceGrpc;
import com.proto.audio.Audio.DataChunkResponse;
import com.proto.audio.Audio.DownloadFileRequest;

import io.grpc.stub.StreamObserver;

public class ServidorImpl extends AudioServiceGrpc.AudioServiceImplBase {
    @Override
    public void downloadAudio(DownloadFileRequest request, StreamObserver<DataChunkResponse> responseObserver) {
        String fileName = "/" + request.getNombre();
        System.out.println("Enviando el archivo: " + fileName);

        InputStream fileStream = ServidorImpl.class.getResourceAsStream(fileName);

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length;
        try {
            while ((length = fileStream.read(buffer, 0, bufferSize)) != -1) {
                DataChunkResponse response = DataChunkResponse
                    .newBuilder()
                    .setData(ByteString.copyFrom(buffer, 0, length))
                    .build();

                System.out.print(".");

                responseObserver.onNext(response);
            }
            
            System.out.println("\n\n");
            fileStream.close();
        } catch (Exception e) {
            System.out.println("No se pudo enviar el archivo " + fileName);
        }

        responseObserver.onCompleted();
    }
}
