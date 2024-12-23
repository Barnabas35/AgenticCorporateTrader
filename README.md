
# Agentic Corporate Trader
This project includes a back-end connected website and mobile application that enables end-users to trade assets such as stocks and crypto currencies. Besides common web and mobile functionalities it features AI asset reports, client asset management and an asset price alert notification system.





## Authors

- [Barnabas Somodi](https://www.github.com/Barnabas35)
- [Luis Giliberti](https://github.com/Luisgiliberti)
- [Milosz Momot](https://github.com/Mentosiak)



## Features

Common:
- Register (Google & Password)
- Login (Google & Password)
- Delete Account
- Support Ticket System
- User Review System
- Contact Phone Number

Trading Related:
- Search Stocks
- Search Crypto Currencies
- Add Client
- Delete Client
- View User Assets
- View Asset Price
- View Asset Details
- View Asset History
- View Asset Description
- Add Balance
- View Balance
- Buy Asset
- Sell Asset
- View Asset Report (Non-AI)
- Create Price Alert
- View Price Alerts

Special Features:
- AI Subscription System
- AI Asset Report
- AI Accounting
- Download Asset Reports


## Environment Variables

To run this project, you will need to add the following environment variables to your .env file.

`GROQ_API_KEY`

`OPENAI_API_KEY`

`LLAMA_URL` Ollama Server URL

`DB_URL` Firebase Database URL

`STORAGE_URL` Firebase Storage URL

`CERT` Firebase Certificate Base64 Encoded

`POLYGON_API_KEY`

`GMAIL_APP_PASSWORD`

`STRIPE_API_KEY`

`STRIPE_WEBHOOK_SECRET`


## Deployment

To deploy the API build the dockerfile at backend/python-api/Dockerfile and host it. AWS ECS recommended.  
Map ports 80:80.
```bash
  docker build -t act-python-api .
```

To deploy the website, host on AWS Amplify for best results. Or use the command below and host it yourself.
```bash
  npx expo export --platform web
```

To deploy Ollama, build the dockerfile at backend/ollama-server/Dockerfile and host it. AWS ECS recommended.  
Map ports 11434:11434.
```bash
  docker build -t act-ollama-server .
```

To compile the mobile application, use Android Studio.

![StackNDeployment](https://github.com/Barnabas35/AgenticCorporateTrader/blob/main/github/FrameWorkNDeployment.png)


## API Reference

Navigate to backend/python-api/requests-info.txt  
There are 45+ API references.
