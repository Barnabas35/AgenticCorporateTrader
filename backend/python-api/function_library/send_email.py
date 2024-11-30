import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError
import smtplib


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

    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login("tradeagently@gmail.com", "kjlnxgxcvhwefmep")
        message = 'Subject: {}\n\n{}'.format(subject, body)
        server.sendmail("tradeagently@gmail.com", dest_email, message)
        server.quit()

    except Exception as e:
        return {"status": str(e)}

    return {"status": "Success"}
