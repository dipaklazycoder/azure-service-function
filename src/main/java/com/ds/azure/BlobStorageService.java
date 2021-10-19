package com.ds.azure;

import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobProperties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class BlobStorageService {
    private BlobClientBuilder client;
   public BlobStorageService() {
       String connectionString = System.getenv("AzureWebJobsStorage");
       String container = System.getenv("container");
       BlobClientBuilder client = new BlobClientBuilder();
       client.connectionString(connectionString);
       client.containerName(container);
       this.client = client;
    }

    public String upload(byte[] bytes, String fileName) {
        if(bytes != null && bytes.length > 0) {
            try {
                InputStream inputStream = new ByteArrayInputStream(bytes);
                //implement your own file name logic.
                 fileName = UUID.randomUUID().toString() + fileName;
                client.blobName(fileName).buildClient().upload(inputStream, bytes.length);
                return fileName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
