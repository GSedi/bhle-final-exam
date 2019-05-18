# Final Exam project

You need to implement a `photo-service` service  for user profile photos. The `photo-service` saves all photos to Amazon S3. 
When a photo is requested the URL of photo is provided by `photo-service`. User ID is unique. Photo can be of `jpg/png` format. A user can have *multiple* photos.

## Requirements
* Photos must be stored in S3 bucket
* Photos must **not** be saved/stored locally
* `photo-service` must provide pre-signed URLs of photos (described below)
* `photo-service` must serve RESTful GET, POST, DELETE requests (no PUT)
* `photo-service` must pass all POSTMAN tests 

## Bucket structure
All objects (photos) have the following path structure: `userId/fileName`. For example, if `userId = user-123` and `fileName = avatar.png`,
then the object key of photo should be `user-123/avatar.png`. One user *may* have many photos, i.e. objects with keys `user-123/ava.png`, `user-123/profile.jpg`, `user-123/bestImage.jpg` may all exist in bucket.

### URL of photo
When keeping photos on AWS S3 it is convenient to generate photo URL for client than passing the file itself. If we pass the file, REST server needs to 
download the photo from S3 and only then send it to client.
However, in order to access the photo AWS S3 keys are needed. We cannot store the keys on all clients. If all keys are stored on clients, 
our data is not secure. Anyone would have access to our buckets, objects, files, etc.

![REST server](img/final_exam.png)

### Pre-Signed (пре-подписанная) URL
In order to generate an URL that will be accessible on clients (without giving AWS keys to clients), AWS S3 
provides pre-signed URLs. A server where AWS S3 keys are stored, uses its *keys* to generate a pre-signed URL that has expiration time. 
While not expired, such URL is accessible by any client.

When getting a photo from AWS S3, `PhotoService` generates such pre-signed URL that has an expiration period of 1 hour.

### Service responses
The service should have the following responses:
* Accepted `status code = 200`, `message = OK`
* PhotoUrl `status code = 200`, `url = URL to photo (AWS)`
* Error photo not found `status code = 404`, `message = Photo not found`
* Error photo exists `status code = 409`, `message = Such file already exists`


### Testing using Postman
When testing using Postman tool, to upload a photo it should manually be selected as body of POST request.
**Important** When uploading a file, **key** must be set to `photoUpload`
![file upload](img/upload-2.png)

Postman collection for testing is in project directory `./postman/final_exam.postman_collection.json`
You should import it to your Postman tool
![import collection](img/upload-3.png)

You can provide your own photos for testing. You can also use prepared photos in `photos` directory.

### Grading policy

Task: Design and implement a REST API for a backend system that is used for uploading, retrieving, removing and updating user photos. The API should pass all the tests provided for Postman tool. The following technologies and tools must be used in the system: 
* Scala programming language (*2 pts.*)
* Akka toolkit and actor model (*2 pts.*)
* Akka HTTP for serving requests (*2 pts.*)
* Amazon Web Services S3 for storing user photos (*4 pts.*)
* Postman tool for testing REST endpoints (*1 pt.*)
* A bucket with name that contains string “user-data” must be created in AWS S3 when the application starts. If the bucket already exists application must continue running (*5 pts.*)
* An HTTP **GET** endpoint that returns user photo by retrieving it from AWS S3. If the photo does not exist, an appropriate error message must be returned (*8 pts.*)
* An HTTP **POST** endpoint that saves user photo to AWS S3. If such a photo for such user already exists, an appropriate error message must be returned (*8 pts.*)
* An HTTP **DELETE** endpoint that deletes user photo from AWS S3. If a photo does not exist for such user, an appropriate error message must be returned (*8 pts.*)

The application will be tested using tests prepared for Postman tool. The task is considered passed if it passes the tests and code works as described above.


Good luck!
