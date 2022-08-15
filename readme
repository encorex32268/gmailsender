## Use

Set Google Double ID Check Password
The SenderPassword is not email password , Is Google Double ID Check password.

	    GMailSender.Builder()
		    .setSenderMail(senderMail)
		    .setSenderPassword(senderPassword)
		    .setReceiverMail(receiverMail)
		    .setSubject(subject)
		    .setBody(body)
		    .setOnSuccessCallBack {
				  Log.d("TAG", " ${it}")
			 }
			  .setOnFailCallBack {
				   Log.d("TAG", " ${it}")
		  	  }
			  .build()
			  .send()


## Gradle

**settings.gradle**

    pluginManagement {
	repositories {
	  gradlePluginPortal()
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }

	 }}
	dependencyResolutionManagement {
	  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	    repositories {
	     google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
	 }}

**build.gradle(:app)**
[GMailSender Version Check](https://jitpack.io/#encorex32268/gmailsender)


    dependencies {
	    implementation 'com.github.encorex32268:gmailsender:1.2'
    }


