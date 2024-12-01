
# Use the python image as a parent image
FROM python:3.12
LABEL authors="Barnabas Somodi"

# Add our files to the working directory
ADD main.py .
ADD db_access.py .
ADD stock_api_access.py .
ADD query_library/ ./query_library/
ADD function_library/ ./function_library/
ADD background_tasks/ ./background_tasks/
ADD yfinance_requests/ ./yfinance_requests/
ADD polygon_request_library/ ./polygon_request_library/

# Expose port 5000
EXPOSE 5000

# Install the required libraries
RUN pip install firebase-admin
RUN pip install flask
RUN pip install flask-cors
RUN pip install polygon-api-client
RUN pip install yfinance
RUN pip install boto3
RUN pip install groq

# Run the application
CMD ["python", "main.py"]


# DEPENDENCIES
# - Docker
# - A .env file containing DB_URL and CERT

# TO BUILD
# Navigate to the directory containing the Dockerfile and run:
# (Dont forget to replace <version_number> with the version number of the image)
# docker build -t act-python-api-v<version_number> .

# TO RUN
# Navigate to the location of the .env file and run:
# (Dont forget to replace <version_number> with the version number of the image)
# docker run --env-file .env -p 5000:5000 --name python-api -d act-python-api-v<version_number>
