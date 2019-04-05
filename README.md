# play-mail plugin

This module for Play! Framework 1 applications allows sending emails with Freemarker templating.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - mail -> mail 1.0.0

repositories:
    - sismicsNexusRaw:
        type: http
        artifact: "https://nexus.sismics.com/repository/sismics/[module]-[revision].zip"
        contains:
            - mail -> *

```
####  Add the routes to your `routes` file

```
# Mail routes
*       /               module:mail
```

####  Send an email

```java
new Mailer();
Mailer.setFrom("contact@app.com");
Mailer.setSubject("Subject");
Mailer.setBodyTemplate("db/email/welcome.ftl");
Mailer.addRecipient("user@domain.com");
Mailer.send(ImmutableMap.of(
        "base_url", Play.configuration.getProperty("application.baseUrl"),
        "user_name", user.name)
);
```

####  Adding attachements

```java
Mailer.addAttachment(new InputStreamAttachement(is, "attachement.pdf", "application/pdf", "Attachement name"));
```
####  Adding messages

You can reuse messages defined in the play `messages` file with the following command:

```java
Mailer.setAddMessages(true);
```

Messages will be available in Freemarker templates under the key:

```
${messages['message.name']}
```

####  Render from templates
Use either / a combination of the following:

```java
Mailer.setSubject("The Subject");
Mailer.setSubjectTemplate("db/email/subject.ftl");
Mailer.setSubjectText("Hello ${name}");
Mailer.setBody("The Body");
Mailer.setBodyTemplate("db/email/body.ftl");
Mailer.setBodyText("Click here: ${link}");
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
