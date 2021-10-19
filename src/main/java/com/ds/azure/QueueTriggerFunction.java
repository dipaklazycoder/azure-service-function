package com.ds.azure;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QueueTriggerFunction {
    /**
     * This function will be invoked when a new message is received at the specified path. The message contents are provided as input to this function.
     */
    @FunctionName("queue-thumbnail-function")
    public void run(
        @QueueTrigger(name = "message", queueName = "thumbnail", connection = "AzureWebJobsStorage") Message message,
        final ExecutionContext context
    ) {
        System.out.println("Processed Message " + message);
        try {
            convertToThumbnail(message);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        context.getLogger().info("Java Queue trigger function processed a message: " + message);
    }

    private void convertToThumbnail(Message message) throws IOException {
        // Image image = ImageIO.read(message.getImageUrl().toURL());
        var outputStream = new ByteArrayOutputStream();
        //File output = new File("thumbnail.jpg");
        Thumbnails.of(message.getImageUrl().toURL())
                .size(50, 50).outputFormat("jpg").toOutputStream(outputStream);
        var bytes = outputStream.toByteArray();
        BlobStorageService blobStorageService = new BlobStorageService();
        var thumbnailPath = blobStorageService.upload(bytes, "thumbnail");
        updateProduct(thumbnailPath, message.getId());

    }

    private void updateProduct(String thumbnailPath, long id) {
        String productApi = System.getenv("productApi");
        // create a client
        var client = HttpClient.newHttpClient();

// create a request
        var request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.noBody()).
                 uri(URI.create(productApi + id + "?thumbnailPath="+ thumbnailPath))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(response.body());
    }


}
