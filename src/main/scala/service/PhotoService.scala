package service

import java.io.{ByteArrayInputStream, InputStream}

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.directives.FileInfo
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{DeleteObjectRequest, ObjectMetadata, PutObjectRequest}
import com.amazonaws.util.IOUtils
import response.Response
import response.Response.{Accepted, Error}

import collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object PhotoService {
  case class UploadPhoto(inputStream: InputStream, userId: String, fileName: String)
  case class GetPhoto(userId: String, fineName: String)
  case class DeletePhoto(userId: String, fineName: String)
  def props(s3Client: AmazonS3, bucketName: String) = Props(new PhotoService(s3Client, bucketName))
}

class PhotoService(s3Client: AmazonS3, bucketName: String) extends Actor with ActorLogging {
  import PhotoService._

  override def receive: Receive = {

    case GetPhoto(userId, fileName) =>
      // Here we generate pre-signed URL to photo

      // Set the presigned URL to expire after one hour.
      val expiration = new java.util.Date()
      // 1000 * 60 * 60 is ONE hour in milliseconds
      val inOneHour = expiration.getTime + 1000 * 60 * 60
      expiration.setTime(inOneHour)

      val objectName = s"$userId/$fileName"

      // check if object exists in AWS first
      if (s3Client.doesObjectExist(bucketName, objectName)) {
        val url: String = s3Client.generatePresignedUrl(bucketName, objectName, expiration).toString
        log.info("Generated a presigned URL: {}", url)

        // TODO: respond with PhotoUrl where status code is 200 and url is `url`
        sender() ! Right(Response.PhotoUrl(200, url))
      } else {
        log.info("Failed to get photo for userId: {}, fileName: {}. Responding with status 404.", userId, fileName)

        // TODO: respond with Error where status code is 404 and message is `Photo not found`
        sender() ! Left(Response.Error(404, "Photo not found"))
      }

    case UploadPhoto(inputStream, userId, fileName) =>
      // TODO: implement this functionality
      // photo object's fullPath inside bucket must be `userId/photoFileName`
      // example: userId = user-2, fileName = photo.jpg => object key inside bucket must be `user-2/photo.jpg`
      val key = userId + "/" + fileName

      // TODO: check that file exists or not
      // If such file exists => respond with Error where status is 409 and message is `Such file already exists`
      // Otherwise => respond with Accepted where status is 200 and message is `OK`
      if (s3Client.doesObjectExist(bucketName, key)) {
        sender() ! Left(Error(409, s"Such file already exists"))
      } else {
        val objectMetadata = new ObjectMetadata()
//        objectMetadata.setContentType("image/jpeg")
        val putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata)

        Try(s3Client.putObject(putObjectRequest)) match {
          case Success(result) =>
            log.info(s"Successfully put key: $key to S3")
            sender() ! Right(Accepted(200, s"OK"))

          case Failure(exception) =>
            log.error(exception, "Error when putting to S3: ")
            sender() ! Left(Error(500, exception.getMessage))

        }
      }

    case DeletePhoto(userId, fileName) =>
      // TODO: implement this functionality
      // Check if such object exists on AWS
      // If exists => respond with Accepted where status code is 200 and message is `OK`
      // If does not exist => respond with Error where status code is 404 and message is `Photo not found`
      val objectName = s"$userId/$fileName"

      // check if object exists in AWS first
      if (s3Client.doesObjectExist(bucketName, objectName)) {
        s3Client.deleteObject(new DeleteObjectRequest(bucketName, objectName))

        sender() ! Right(Response.Accepted(200, "OK"))
      } else {
        log.info("Failed to get photo for userId: {}, fileName: {}. Responding with status 404.", userId, fileName)

        // TODO: respond with Error where status code is 404 and message is `Photo not found`
        sender() ! Left(Response.Error(404, "Photo not found"))
      }

      // photo object's fullPath is the same as in previous methods (GetPhoto and UploadPhoto)

  }
}
