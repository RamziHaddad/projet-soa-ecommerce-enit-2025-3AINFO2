The Mail Service exposes a REST API for sending emails:
------------------------------------------------------

Endpoint: POST /api/v1/mails

Request body (JSON):

senderEmail: sender's email address

recipientEmail: recipient's email address

subject: email subject

body: email content

attachments: list of file paths to attach



Technique to ensure each email is sent exactly once :
-----------------------------------------------------

The service uses the Inbox/Outbox pattern with two key columns in the mail table:

content_hash: a unique hash computed from the email content (sender, recipient, subject, body). It is used to detect and prevent duplicates before inserting into the database.

traite: a boolean indicating whether the email has already been sent. Only emails with traite = false are selected for sending. After a successful send, traite is set to true, ensuring that no email is sent more than once, even in case of a service restart or failure.
