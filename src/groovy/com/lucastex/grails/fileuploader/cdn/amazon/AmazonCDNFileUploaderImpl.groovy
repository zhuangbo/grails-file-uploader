package com.lucastex.grails.fileuploader.cdn.amazon

import org.jclouds.ContextBuilder
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.aws.s3.blobstore.options.AWSS3PutObjectOptions
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.domain.Blob
import org.jclouds.http.HttpRequest
import org.jclouds.s3.domain.AccessControlList
import org.jclouds.s3.domain.CannedAccessPolicy
import org.jclouds.s3.domain.S3Object
import org.jclouds.s3.domain.AccessControlList.Permission
import org.jclouds.s3.domain.internal.MutableObjectMetadataImpl
import org.jclouds.s3.domain.internal.S3ObjectImpl

import com.lucastex.grails.fileuploader.cdn.CDNFileUploader

class AmazonCDNFileUploaderImpl extends CDNFileUploader {

    AWSS3Client client

    AmazonCDNFileUploaderImpl(String accessKey, String accessSecret) {
        this.accessKey = accessKey
        this.accessSecret = accessSecret
    }

    @Override
    boolean authenticate() {
        context = ContextBuilder.newBuilder("aws-s3")
                .credentials(accessKey, accessSecret)
                .buildView(BlobStoreContext.class)
        println "Context created ${context.class}"

        blobStore = context.getBlobStore()
        println "Blobstore ${blobStore.class}"

        // Storing wrapped api of S3Client with apache jcloud
        client = context.unwrap().getApi()

        return false
    }

    @Override
    void close() {
        context.close()
    }

    @Override
    boolean containerExists(String name) {
        client.bucketExists(name)
    }

    @Override
    boolean createContainer(String name) {
        blobStore.createContainerInLocation(null, name)
    }

    @Override
    void deleteFile(String containerName, String fileName) {
        blobStore.removeBlob(containerName, fileName)
    }

    S3ObjectImpl getObject(String containerName, String fileName) {
        client.getObject(containerName, fileName, null)
    }

    @Override
    String getPermanentURL(String containerName, String fileName) {
        getObject(containerName, fileName).metadata.uri
    }

    /**
     * @param containerName Name of the bucket
     * @param fileName Name of the object in bucket
     * @param expiration expiration time in seconds for pre-signed URl.
     *        For example: 60 * 60 // For 1 hour.
     *
     * @see http://docs.aws.amazon.com/AmazonS3/latest/dev/ShareObjectPreSignedURLJavaSDK.html
     */
    @Override
    String getTemporaryURL(String containerName, String fileName, long expiration) {
        HttpRequest request = context.signer.signGetBlob(containerName, fileName, expiration)
        request.endpoint.toString()
    }

    @Override
    boolean makeFilePublic(String containerName, String fileName) {
        AccessControlList acl = new AccessControlList()
        acl.addPermission(new URI("http://acs.amazonaws.com/groups/global/AllUsers"), Permission.READ)
        getObject(containerName, fileName).setAccessControlList(acl)
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName, boolean makePublic) {
        String eTag

        if(makePublic) {
            AWSS3PutObjectOptions fileOptions = new AWSS3PutObjectOptions()
            fileOptions.withAcl(CannedAccessPolicy.PUBLIC_READ)

            MutableObjectMetadataImpl mutableObjectMetadata = new MutableObjectMetadataImpl()
            mutableObjectMetadata.setKey(fileName)

            S3Object newFileToUpload = new S3ObjectImpl(mutableObjectMetadata)
            newFileToUpload.setPayload(file)

            client.putObject(containerName, newFileToUpload, fileOptions)
        } else {
            Blob newFileToUpload = blobStore.blobBuilder(fileName)
                    .payload(file)
                    .build()
            eTag = blobStore.putBlob(containerName, newFileToUpload)
        }

        return true
    }
}