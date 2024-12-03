from http.client import responses

import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError
import smtplib
import os
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail


def send_email_aws(dest_email, from_email, subject, body):

    try:
        client = boto3.client(
            'ses',
            region_name='eu-west-1')

        response = client.send_email(
            Destination={
                'ToAddresses': [
                    dest_email,
                ],
            },
            Message={
                'Body': {
                    'Text': {
                        'Charset': 'UTF-8',
                        'Data': body,
                    },
                },
                'Subject': {
                    'Charset': 'UTF-8',
                    'Data': subject,
                },
            },
            Source=from_email,
        )
    except NoCredentialsError:
        return {"status": "No AWS credentials found."}
    except PartialCredentialsError:
        return {"status": "Partial AWS credentials found."}
    except Exception as e:
        return {"status": str(e)}


def send_email_smtp(dest_email, subject, body):

    # Get Gmail app password from environment variable
    GMAIL_APP_PASSWORD_ENV = os.getenv("GMAIL_APP_PASSWORD")

    # If GMAIL_APP_PASSWORD_ENV environment variable is not set, use fallback config.txt line 6
    if GMAIL_APP_PASSWORD_ENV is None:
        try:
            with open("config.txt", "r") as f:
                GMAIL_APP_PASSWORD_ENV = f.readlines()[5].strip("\n")
                f.close()

        except FileNotFoundError:
            return {"status": "No Gmail app password found."}

    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login("tradeagently@gmail.com", GMAIL_APP_PASSWORD_ENV)
        message = 'Subject: {}\n\n{}'.format(subject, body)
        server.sendmail("tradeagently@gmail.com", dest_email, message)
        server.quit()

    except Exception as e:
        return {"status": str(e)}

    return {"status": "Success"}


def send_email_sg(dest_email, subject, body):

    # Get SendGrid API key from environment variable
    SENDGRID_API_KEY_ENV = os.getenv("SENDGRID_API_KEY")

    # If SENDGRID_API_KEY_ENV environment variable is not set, use fallback config.txt line 8
    if SENDGRID_API_KEY_ENV is None:
        try:
            with open("../config.txt", "r") as f:
                SENDGRID_API_KEY_ENV = f.readlines()[7].strip("\n")
                f.close()
        except FileNotFoundError:
            return {"status": "No SendGrid API key found."}

    try:
        message = Mail(
            from_email="pricealert@smtp.tradeagently.com",
            to_emails=dest_email,
            subject=subject,
            plain_text_content=body)

        sg = SendGridAPIClient(SENDGRID_API_KEY_ENV)

        response = sg.send(message)

    except Exception as e:
        return {"status": str(e)}

    return {"status": "Success"}
