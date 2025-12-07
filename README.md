The Mail Service exposes a REST API for sending emails:
------------------------------------------------------

## Endpoint: POST /api/mails

Request body (JSON):{

senderEmail: sender's email address

recipientEmail: recipient's email address

subject: email subject

body: email content

attachments: list of file paths to attach
}

## Endpoint: POST /api/mails/sync

Sends the email immediately (synchronous) and returns the result.

Request body (JSON):{

senderEmail: sender's email address

recipientEmail: recipient's email address

subject: email subject

body: email content

attachments: list of file paths to attach
}

## Endpoint: GET /api/mails

Returns a list of all mails. Optional query parameter: traite (true/false) to filter by status.
example: http://localhost:8085/api/mails?traite=true

## Endpoint: GET /api/mails/{id}

Returns the details of a specific mail by its ID.



Technique to ensure each email is sent exactly once :
-----------------------------------------------------

The service uses the Inbox/Outbox pattern with two key columns in the mail table:


content_hash: a unique hash computed from the email content (sender, recipient, subject, body and createdAt). It is used to detect and prevent duplicates before inserting into the database. (Including createdAt allows sending the same mail again after an hour)

traite: a boolean indicating whether the email has already been sent. Only emails with traite = false are selected for sending. After a successful send, traite is set to true, ensuring that no email is sent more than once, even in case of a service restart or failure.

The service uses dependency injection and interfaces to achieve loose coupling for email sending. Both MailtrapEmailSender and SmtpEmailSender implement the IEmailSender interface. The actual sender is selected via configuration (EmailSenderConfig) read in application.properties, allowing easy switching betwen mailtrap and smtp without modifying business logic.



