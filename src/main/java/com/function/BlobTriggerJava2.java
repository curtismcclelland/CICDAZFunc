package com.function;

import com.microsoft.azure.functions.annotation.*;

import com.microsoft.azure.functions.*;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
//import com.azure.storage.blob.BlobClientBuilder;

import java.util.Optional;

/**
 * Azure Functions with Azure Blob trigger.
 */
public class BlobTriggerJava2 {
    /**
     * This function will be invoked when a new or updated blob is detected at the
     * specified path. The blob contents are provided as input to this function.
     */
    @FunctionName("BlobTriggerJava2")
    @StorageAccount("blobtriggercurtistest_STORAGE")
    public void run(
            @BlobTrigger(name = "content", path = "samples-workitems/{name}", dataType = "binary") byte[] content,
            @BindingName("name") String name,
            @BlobOutput(name = "destinationBlob", dataType = "binary", path = "desintation-container/{name}", connection = "blobtriggercurtistest_STORAGE") OutputBinding<byte[]> destinationBlob,
            final ExecutionContext context) {
        context.getLogger().info("Java Blob trigger function processed a blob. Name: " + name + "\n  Size: "
                + content.length + " Bytes");

        // Sets the storage account connection string
        String connectionString = System.getenv("blobtriggercurtistest_STORAGE");

        // Sets the source and destination container names
        String sourceContainerName = "samples-workitems";

        // Create BlobServiceClient for the source container
        BlobServiceClient sourceBlobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Get a reference to the source container
        BlobContainerClient sourceContainerClient = sourceBlobServiceClient.getBlobContainerClient(sourceContainerName);

        // Checking if the source blob exists
        Optional<BlobItem> sourceBlob = sourceContainerClient.listBlobs().stream()
                .filter(b -> b.getName().equals(name))
                .findFirst();

        if (sourceBlob.isPresent()) {
            // Copy the blob to the destination container
            destinationBlob.setValue(content);

            // Deletes the source blob
            sourceContainerClient.getBlobClient(name).delete();
        } else {
            context.getLogger().info("Blob not found in source container");
        }
    }
}
