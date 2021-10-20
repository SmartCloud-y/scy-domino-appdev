# Setting up scy-domino-ext

Enabling JVM "java options file":

## NOTES.INI configuration [EDIT]

    JavaOptionsFile=C:\Program Files (x86)\HCL\Notes\<customfolder>\java.options

## Create the JVM "options file" [ADD or EDIT]

Add to your server filesystem the above file with the following content:

    -Djava.security.properties=C:\Program Files (x86)\HCL\Notes\<customfolder>\java.security-ext.properties

This file is not ov

## Create the security.properties overrides file [ADD]

This will override java.security.properties file telling domino to override security properties with the specified file

    policy.url.3=nsfpolicy:///global

This will instruct the JVM to override the third policy URL and load from our custom provider.
Default value in domino java.security is:

    policy.url.3=file:${user.home}/.java.policy